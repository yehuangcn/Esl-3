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
package com.freeswitch.netty.handler.codec.string;

import com.freeswitch.netty.buffer.ChannelBuffer;
import com.freeswitch.netty.channel.Channel;
import com.freeswitch.netty.channel.ChannelHandler.Sharable;
import com.freeswitch.netty.channel.ChannelHandlerContext;
import com.freeswitch.netty.channel.ChannelPipeline;
import com.freeswitch.netty.channel.MessageEvent;
import com.freeswitch.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import com.freeswitch.netty.handler.codec.frame.Delimiters;
import com.freeswitch.netty.handler.codec.frame.FrameDecoder;
import com.freeswitch.netty.handler.codec.oneone.OneToOneDecoder;

import java.nio.charset.Charset;

/**
 * Decodes a received {@link ChannelBuffer} into a {@link String}. Please note
 * that this decoder must be used with a proper {@link FrameDecoder} such as
 * {@link DelimiterBasedFrameDecoder} if you are using a stream-based transport
 * such as TCP/IP. A typical setup for a text-based line protocol in a TCP/IP
 * socket would be:
 * <p>
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 *
 * // Decoders
 * pipeline.addLast("frameDecoder", new {@link DelimiterBasedFrameDecoder}(80, {@link Delimiters#lineDelimiter()}));
 * pipeline.addLast("stringDecoder", new {@link StringDecoder}(CharsetUtil.UTF_8));
 *
 * // Encoder
 * pipeline.addLast("stringEncoder", new {@link StringEncoder}(CharsetUtil.UTF_8));
 * </pre>
 * <p>
 * and then you can use a {@link String} instead of a {@link ChannelBuffer} as a
 * message:
 * <p>
 * <pre>
 * void messageReceived({@link ChannelHandlerContext} ctx, {@link MessageEvent} e) {
 *     String msg = (String) e.getMessage();
 *     ch.write("Did you say '" + msg + "'?\n");
 * }
 * </pre>
 *
 * @apiviz.landmark
 */
@Sharable
public class StringDecoder extends OneToOneDecoder {

    // TODO Use CharsetDecoder instead.
    private final Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public StringDecoder() {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public StringDecoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof ChannelBuffer)) {
            return msg;
        }
        return ((ChannelBuffer) msg).toString(charset);
    }
}
