/*
 * Copyright 2010 david varnes.
 *
 * Licensed under the Apache License, version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.freeswitch.esl.client.handler;

import com.freeswitch.esl.client.EslClient;
import com.freeswitch.esl.client.internal.debug.ExecutionHandler;
import com.freeswitch.esl.transport.message.EslFrameDecoder;
import com.freeswitch.netty.channel.ChannelHandler;
import com.freeswitch.netty.channel.ChannelPipeline;
import com.freeswitch.netty.channel.ChannelPipelineFactory;
import com.freeswitch.netty.channel.Channels;
import com.freeswitch.netty.handler.codec.string.StringEncoder;
import com.freeswitch.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/**
 * End users of the {@link EslClient} should not need to use this class.
 * <p>
 * Convenience factory to assemble a Netty processing pipeline for handler
 * clients.
 * 
 * @author david varnes
 */
public class EslPipelineFactory implements ChannelPipelineFactory {
	private final ChannelHandler handler;

	public EslPipelineFactory(ChannelHandler handler) {
		this.handler = handler;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("encoder", new StringEncoder());
		pipeline.addLast("decoder", new EslFrameDecoder(8192));
		// Add an executor to ensure separate thread for each upstream message
		// from here
		pipeline.addLast("executor", new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));

		// now the handler client logic
		pipeline.addLast("clientHandler", handler);

		return pipeline;
	}
}
