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

import static com.freeswitch.netty.channel.Channels.fireChannelOpen;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.freeswitch.netty.channel.AbstractServerChannel;
import com.freeswitch.netty.channel.ChannelException;
import com.freeswitch.netty.channel.ChannelFactory;
import com.freeswitch.netty.channel.ChannelPipeline;
import com.freeswitch.netty.channel.ChannelSink;
import com.freeswitch.netty.channel.socket.DefaultServerSocketChannelConfig;
import com.freeswitch.netty.channel.socket.ServerSocketChannel;
import com.freeswitch.netty.channel.socket.ServerSocketChannelConfig;
import com.freeswitch.netty.logging.InternalLogger;
import com.freeswitch.netty.logging.InternalLoggerFactory;

class OioServerSocketChannel extends AbstractServerChannel implements ServerSocketChannel {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioServerSocketChannel.class);

	final ServerSocket socket;
	final Lock shutdownLock = new ReentrantLock();
	private final ServerSocketChannelConfig config;

	OioServerSocketChannel(ChannelFactory factory, ChannelPipeline pipeline, ChannelSink sink) {

		super(factory, pipeline, sink);

		try {
			socket = new ServerSocket();
		} catch (IOException e) {
			throw new ChannelException("Failed to open a server socket.", e);
		}

		try {
			socket.setSoTimeout(1000);
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e2) {
				if (logger.isWarnEnabled()) {
					logger.warn("Failed to close a partially initialized socket.", e2);
				}
			}
			throw new ChannelException("Failed to set the server socket timeout.", e);
		}

		config = new DefaultServerSocketChannelConfig(socket);

		fireChannelOpen(this);
	}

	public ServerSocketChannelConfig getConfig() {
		return config;
	}

	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) socket.getLocalSocketAddress();
	}

	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	public boolean isBound() {
		return isOpen() && socket.isBound();
	}

	@Override
	protected boolean setClosed() {
		return super.setClosed();
	}
}
