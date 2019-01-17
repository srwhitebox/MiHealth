package com.mihealth.websocket;

import java.net.HttpCookie;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.mihealth.db.model.UserModel;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.net.XcCookies;

@Controller
public class QueryHandler extends TextWebSocketHandler{
	@Autowired
	private SocketSessionManager sessionManager;
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		super.afterConnectionEstablished(session);
		sessionManager.add(session);		
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessionManager.remove(session);
		super.afterConnectionClosed(session, status);
	}

	@Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
		if (message.getPayloadLength() == 0)
			return;
		
		final String textMessage = message.getPayload();
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		super.handleBinaryMessage(session, message);	}

}
