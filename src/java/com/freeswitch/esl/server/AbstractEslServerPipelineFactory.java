package com.freeswitch.esl.server;

import com.freeswitch.esl.client.internal.debug.ExecutionHandler;
import com.freeswitch.esl.transport.message.EslFrameDecoder;
import com.freeswitch.netty.channel.ChannelPipeline;
import com.freeswitch.netty.channel.ChannelPipelineFactory;
import com.freeswitch.netty.channel.Channels;
import com.freeswitch.netty.handler.codec.string.StringEncoder;
import com.freeswitch.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;


public abstract class AbstractEslServerPipelineFactory implements ChannelPipelineFactory {
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        // Add the text line codec combination first
        pipeline.addLast("encoder", new StringEncoder());
        // Note that server mode requires the decoder to treat many 'headers'
        // as body lines
        pipeline.addLast("decoder", new EslFrameDecoder(8092, true));
        // Add an executor to ensure separate thread for each upstream message
        // from here
        pipeline.addLast("executor", new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));

        // now the server client logic
        pipeline.addLast("EslServerHandler", makeHandler());

        return pipeline;
    }

    protected abstract AbstractEslServerHandler makeHandler();
}
