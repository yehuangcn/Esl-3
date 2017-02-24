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
package com.freeswitch.netty.channel;

import com.freeswitch.netty.handler.execution.ExecutionHandler;

import java.util.concurrent.Executor;

/**
 * Handles or intercepts an upstream {@link ChannelEvent}, and sends a
 * {@link ChannelEvent} to the next handler in a {@link ChannelPipeline}.
 * <p>
 * The most common use case of this interface is to intercept an I/O event
 * generated by I/O workers to transform the received messages or execute the
 * relevant business logic.
 * <p>
 * <h3>{@link SimpleChannelUpstreamHandler}</h3>
 * <p>
 * In most cases, you will get to use a {@link SimpleChannelUpstreamHandler} to
 * implement an upstream handler because it provides an individual handler
 * method for each event type. You might want to implement this interface
 * directly though if you want to handle various types of events in more generic
 * way.
 * <p>
 * <h3>Firing an event to the next handler</h3>
 * <p>
 * You can forward the received event upstream or downstream. In most cases,
 * {@link ChannelUpstreamHandler} will send the event upstream (i.e. handler)
 * although it is legal to send the event downstream (i.e. server):
 * <p>
 * <pre>
 * // Sending the event upstream (handler)
 * void handleUpstream({@link ChannelHandlerContext} ctx, {@link ChannelEvent} e) throws Exception {
 *     ...
 *     ctx.sendUpstream(e);
 *     ...
 * }
 *
 * // Sending the event downstream (server)
 * void handleDownstream({@link ChannelHandlerContext} ctx, {@link ChannelEvent} e) throws Exception {
 *     ...
 *     ctx.sendDownstream(new {@link DownstreamMessageEvent}(...));
 *     ...
 * }
 * </pre>
 * <p>
 * <h4>Using the helper class to send an event</h4>
 * <p>
 * You will also find various helper methods in {@link Channels} to be useful to
 * generate and send an artificial or manipulated event.
 * <p>
 * <h3>State management</h3>
 * <p>
 * Please refer to {@link ChannelHandler}.
 * <p>
 * <h3>Thread safety</h3>
 * <p>
 * {@link #handleUpstream(ChannelHandlerContext, ChannelEvent) handleUpstream}
 * will be invoked sequentially by the same thread (i.e. an I/O thread) and
 * therefore a handler does not need to worry about being invoked with a new
 * upstream event before the previous upstream event is finished.
 * <p>
 * This does not necessarily mean that there's a dedicated thread per
 * {@link Channel}; the I/O thread of some transport can serve more than one
 * {@link Channel} (e.g. NIO transport), while the I/O thread of other
 * transports can serve only one (e.g. OIO transport).
 * <p>
 * However, if you add an {@link ExecutionHandler} to a {@link ChannelPipeline},
 * this behavior changes depending on what {@link Executor} was employed to
 * dispatch the events. Please refer to {@link ExecutionHandler} for more
 * information.
 *
 * @apiviz.exclude ^org\.jboss\.netty\.handler\..*$
 */
public interface ChannelUpstreamHandler extends ChannelHandler {

    /**
     * Handles the specified upstream event.
     *
     * @param ctx the context object for this handler
     * @param e   the upstream event to process or intercept
     */
    void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception;
}
