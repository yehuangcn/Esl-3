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
package com.freeswitch.netty.channel.socket;

import com.freeswitch.netty.channel.ChannelException;
import com.freeswitch.netty.channel.DefaultServerChannelConfig;
import com.freeswitch.netty.util.internal.ConversionUtil;

import java.net.ServerSocket;
import java.net.SocketException;

/**
 * The default {@link ServerSocketChannelConfig} implementation.
 */
public class DefaultServerSocketChannelConfig extends DefaultServerChannelConfig implements ServerSocketChannelConfig {

    private final ServerSocket socket;
    private volatile int backlog;

    /**
     * Creates a new instance.
     */
    public DefaultServerSocketChannelConfig(ServerSocket socket) {
        if (socket == null) {
            throw new NullPointerException("socket");
        }
        this.socket = socket;
    }

    @Override
    public boolean setOption(String key, Object value) {
        if (super.setOption(key, value)) {
            return true;
        }

        if ("receiveBufferSize".equals(key)) {
            setReceiveBufferSize(ConversionUtil.toInt(value));
        } else if ("reuseAddress".equals(key)) {
            setReuseAddress(ConversionUtil.toBoolean(value));
        } else if ("backlog".equals(key)) {
            setBacklog(ConversionUtil.toInt(value));
        } else {
            return false;
        }
        return true;
    }

    public boolean isReuseAddress() {
        try {
            return socket.getReuseAddress();
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setReuseAddress(boolean reuseAddress) {
        try {
            socket.setReuseAddress(reuseAddress);
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public int getReceiveBufferSize() {
        try {
            return socket.getReceiveBufferSize();
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        try {
            socket.setReceiveBufferSize(receiveBufferSize);
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        socket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        if (backlog < 0) {
            throw new IllegalArgumentException("backlog: " + backlog);
        }
        this.backlog = backlog;
    }
}
