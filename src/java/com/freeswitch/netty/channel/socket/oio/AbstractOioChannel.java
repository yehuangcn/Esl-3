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
package com.freeswitch.netty.channel.socket.oio;

import com.freeswitch.netty.channel.*;
import com.freeswitch.netty.channel.socket.Worker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

abstract class AbstractOioChannel extends AbstractChannel {
    final Object interestOpsLock = new Object();
    volatile InetSocketAddress remoteAddress;
    volatile Thread workerThread;
    volatile Worker worker;
    private volatile InetSocketAddress localAddress;

    AbstractOioChannel(Channel parent, ChannelFactory factory, ChannelPipeline pipeline, ChannelSink sink) {
        super(parent, factory, pipeline, sink);
    }

    @Override
    protected boolean setClosed() {
        return super.setClosed();
    }

    @Override
    protected int getInternalInterestOps() {
        return super.getInternalInterestOps();
    }

    @Override
    protected void setInternalInterestOps(int interestOps) {
        super.setInternalInterestOps(interestOps);
    }

    @Override
    public ChannelFuture write(Object message, SocketAddress remoteAddress) {
        if (remoteAddress == null || remoteAddress.equals(getRemoteAddress())) {
            return super.write(message, null);
        } else {
            return super.write(message, remoteAddress);
        }
    }

    public boolean isBound() {
        return isOpen() && isSocketBound();
    }

    public boolean isConnected() {
        return isOpen() && isSocketConnected();
    }

    public InetSocketAddress getLocalAddress() {
        InetSocketAddress localAddress = this.localAddress;
        if (localAddress == null) {
            try {
                this.localAddress = localAddress = getLocalSocketAddress();
            } catch (Throwable t) {
                // Sometimes fails on a closed socket in Windows.
                return null;
            }
        }
        return localAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        InetSocketAddress remoteAddress = this.remoteAddress;
        if (remoteAddress == null) {
            try {
                this.remoteAddress = remoteAddress = getRemoteSocketAddress();
            } catch (Throwable t) {
                // Sometimes fails on a closed socket in Windows.
                return null;
            }
        }
        return remoteAddress;
    }

    abstract boolean isSocketBound();

    abstract boolean isSocketConnected();

    abstract boolean isSocketClosed();

    abstract InetSocketAddress getLocalSocketAddress() throws Exception;

    abstract InetSocketAddress getRemoteSocketAddress() throws Exception;

    abstract void closeSocket() throws IOException;

}
