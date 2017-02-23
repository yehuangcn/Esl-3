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
import com.freeswitch.esl.client.internal.AbstractEslClientHandler;
import com.freeswitch.esl.client.internal.IEslProtocolListener;
import com.freeswitch.esl.transport.CommandResponse;
import com.freeswitch.esl.transport.event.EslEvent;
import com.freeswitch.esl.transport.message.EslHeaders.Value;
import com.freeswitch.netty.channel.ChannelHandlerContext;
import com.freeswitch.netty.handler.execution.ExecutionHandler;
import com.freeswitch.esl.transport.message.EslMessage;

/**
 * End users of the handler {@link EslClient} should not need to use this class.
 * <p>
 * Specialised {@link AbstractEslClientHandler} that implements the connection
 * logic for an 'Inbound' FreeSWITCH Event Socket connection. The
 * responsibilities for this class are:
 * <ul>
 * <li>To handle the auth request that the FreeSWITCH server will send
 * immediately following a new connection when mode is Inbound.
 * <li>To signal the observing {@link IEslProtocolListener} (expected to be the
 * Inbound client implementation) when ESL events are received.
 * </ul>
 * Note: implementation requirement is that an {@link ExecutionHandler} is
 * placed in the processing pipeline prior to this handler. This will ensure
 * that each incoming message is processed in its own thread (although still
 * guaranteed to be processed in the order of receipt).
 * 
 * @author david varnes
 */
public class EslClientHandler extends AbstractEslClientHandler {
	private final String password;
	private final IEslProtocolListener listener;

	public EslClientHandler(String password, IEslProtocolListener listener) {
		this.password = password;
		this.listener = listener;
	}

	protected void handleEslEvent(ChannelHandlerContext ctx, EslEvent event) {
		log.trace("Received event: [{}]", event);
		listener.eventReceived(event);
	}

	protected void handleAuthRequest(ChannelHandlerContext ctx) {
//		log.debug("Auth requested, sending [auth {}]", "*****");
		EslMessage response = sendSyncSingleLineCommand(ctx.getChannel(), "auth " + password);
//		log.debug("Auth response [{}]", response);
		if (response.getContentType().equals(Value.COMMAND_REPLY)) {
			CommandResponse commandResponse = new CommandResponse("auth " + password, response);
			listener.authResponseReceived(commandResponse);
		} else {
			log.error("Bad auth response message [{}]", response);
			throw new IllegalStateException("Incorrect auth response");
		}
	}

	@Override
	protected void handleDisconnectionNotice() {
		log.debug("Received disconnection notice");
		listener.disconnected();
	}

}
