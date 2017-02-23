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
package com.freeswitch.netty.channel.socket.nio;

import static com.freeswitch.netty.channel.Channels.fireChannelOpen;

import java.nio.channels.SocketChannel;

import com.freeswitch.netty.channel.Channel;
import com.freeswitch.netty.channel.ChannelFactory;
import com.freeswitch.netty.channel.ChannelPipeline;
import com.freeswitch.netty.channel.ChannelSink;

final class NioAcceptedSocketChannel extends NioSocketChannel {

	final Thread bossThread;

	NioAcceptedSocketChannel(ChannelFactory factory, ChannelPipeline pipeline, Channel parent, ChannelSink sink, SocketChannel socket, NioWorker worker, Thread bossThread) {

		super(parent, factory, pipeline, sink, socket, worker);

		this.bossThread = bossThread;

		setConnected();

		fireChannelOpen(this);
	}
}
