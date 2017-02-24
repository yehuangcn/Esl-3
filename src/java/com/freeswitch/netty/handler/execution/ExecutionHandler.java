/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.freeswitch.netty.handler.execution;

import com.freeswitch.netty.bootstrap.ServerBootstrap;
import com.freeswitch.netty.channel.*;
import com.freeswitch.netty.channel.ChannelHandler.Sharable;
import com.freeswitch.netty.util.ExternalResourceReleasable;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Forwards an upstream {@link ChannelEvent} to an {@link Executor}.
 * <p>
 * {@link ExecutionHandler} is often used when your {@link ChannelHandler}
 * performs a blocking operation that takes long time or accesses a resource
 * which is not CPU-bound business logic such as DB access. Running such
 * operations in a pipeline without an {@link ExecutionHandler} will result in
 * unwanted hiccup during I/O because an I/O thread cannot perform I/O until
 * your handler returns the control to the I/O thread.
 * <p>
 * In most cases, an {@link ExecutionHandler} is coupled with an
 * {@link OrderedMemoryAwareThreadPoolExecutor} because it guarantees the
 * correct event execution order and prevents an {@link OutOfMemoryError} under
 * load:
 * <p>
 * <pre>
 * public class DatabaseGatewayPipelineFactory implements {@link ChannelPipelineFactory} {
 *
 *     <b>private final {@link ExecutionHandler} executionHandler;</b>
 *
 *     public DatabaseGatewayPipelineFactory({@link ExecutionHandler} executionHandler) {
 *         this.executionHandler = executionHandler;
 *     }
 *
 *     public {@link ChannelPipeline} getPipeline() {
 *         return {@link Channels}.pipeline(
 *                 new DatabaseGatewayProtocolEncoder(),
 *                 new DatabaseGatewayProtocolDecoder(),
 *                 <b>executionHandler, // Must be shared</b>
 *                 new DatabaseQueryingHandler());
 *     }
 * }
 * ...
 *
 * public static void main(String[] args) {
 *     {@link ServerBootstrap} bootstrap = ...;
 *     ...
 *     <b>{@link ExecutionHandler} executionHandler = new {@link ExecutionHandler}(
 *             new {@link OrderedMemoryAwareThreadPoolExecutor}(16, 1048576, 1048576))
 *     bootstrap.setPipelineFactory(
 *             new DatabaseGatewayPipelineFactory(executionHandler));</b>
 *     ...
 *     bootstrap.bind(...);
 *     ...
 *
 *     while (!isServerReadyToShutDown()) {
 *         // ... wait ...
 *     }
 *
 *     bootstrap.releaseExternalResources();
 *     <b>executionHandler.releaseExternalResources();</b>
 * }
 * </pre>
 * <p>
 * Please refer to {@link OrderedMemoryAwareThreadPoolExecutor} for the detailed
 * information about how the event order is guaranteed.
 * <p>
 * <h3>SEDA (Staged Event-Driven Architecture)</h3> You can implement an
 * alternative thread model such as <a href=
 * "http://en.wikipedia.org/wiki/Staged_event-driven_architecture">SEDA</a> by
 * adding more than one {@link ExecutionHandler} to the pipeline.
 * <p>
 * <h3>Using other {@link Executor} implementation</h3>
 * <p>
 * Although it's recommended to use
 * {@link OrderedMemoryAwareThreadPoolExecutor}, you can use other
 * {@link Executor} implementations. However, you must note that other
 * {@link Executor} implementation might break your application because they
 * often do not maintain event execution order nor interact with I/O threads to
 * control the incoming traffic and avoid {@link OutOfMemoryError}.
 *
 * @apiviz.landmark
 * @apiviz.has java.util.concurrent.ThreadPoolExecutor
 */
@Sharable
public class ExecutionHandler implements ChannelUpstreamHandler, ChannelDownstreamHandler, ExternalResourceReleasable {

    private final Executor executor;
    private final boolean handleDownstream;
    private final boolean handleUpstream;

    /**
     * Creates a new instance with the specified {@link Executor}. Specify an
     * {@link OrderedMemoryAwareThreadPoolExecutor} if unsure.
     */
    public ExecutionHandler(Executor executor) {
        this(executor, false, true);
    }

    /**
     * Creates a new instance with the specified {@link Executor}. Specify an
     * {@link OrderedMemoryAwareThreadPoolExecutor} if unsure.
     */
    public ExecutionHandler(Executor executor, boolean handleDownstream, boolean handleUpstream) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }
        if (!handleDownstream && !handleUpstream) {
            throw new IllegalArgumentException("You must handle at least handle one event type");
        }
        this.executor = executor;
        this.handleDownstream = handleDownstream;
        this.handleUpstream = handleUpstream;
    }

    /**
     * Returns the {@link Executor} which was specified with the constructor.
     */
    public Executor getExecutor() {
        return executor;
    }

    /**
     * Shuts down the {@link Executor} which was specified with the constructor
     * and wait for its termination.
     */
    public void releaseExternalResources() {
        Executor executor = getExecutor();
        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdown();
        }
        if (executor instanceof ExternalResourceReleasable) {
            ((ExternalResourceReleasable) executor).releaseExternalResources();
        }
    }

    public void handleUpstream(ChannelHandlerContext context, ChannelEvent e) throws Exception {
        if (handleUpstream) {
            executor.execute(new ChannelUpstreamEventRunnable(context, e, executor));
        } else {
            context.sendUpstream(e);
        }
    }

    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        // check if the read was suspend
        if (!handleReadSuspend(ctx, e)) {
            if (handleDownstream) {
                executor.execute(new ChannelDownstreamEventRunnable(ctx, e, executor));
            } else {
                ctx.sendDownstream(e);
            }
        }
    }

    /**
     * Handle suspended reads
     */
    protected boolean handleReadSuspend(ChannelHandlerContext ctx, ChannelEvent e) {
        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent cse = (ChannelStateEvent) e;
            if (cse.getState() == ChannelState.INTEREST_OPS && (((Integer) cse.getValue()).intValue() & Channel.OP_READ) != 0) {

                // setReadable(true) requested
                boolean readSuspended = ctx.getAttachment() != null;
                if (readSuspended) {
                    // Drop the request silently if MemoryAwareThreadPool has
                    // set the flag.
                    e.getFuture().setSuccess();
                    return true;
                }
            }
        }

        return false;
    }
}
