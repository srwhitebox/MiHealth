package com.mihealth.security;

import java.security.Principal;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketSession;

import com.mihealth.db.model.UserModel;

public class UserPrincipal implements Principal{
	private UserModel user;
	
	public UserPrincipal(UserModel user){
		this.setUser(user);
	}
	
	@Override
	public String getName() {
		return user == null ? null : user.getUserUid();
	}

	public UserModel getUser() {
		return user;
	}

	public void setUser(UserModel user) {
		this.user = user;
	}
	
	public static UserModel getUser(Authentication authentication){
		if (authentication == null)
			return null;
		return ((UserPrincipal)authentication.getPrincipal()).getUser();
	}
	
	public static UserModel getUser(WebSocketSession session) {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)session.getPrincipal();
		if (token == null)
			return null;
		UserPrincipal principal = (UserPrincipal) token.getPrincipal();
		return principal == null ? null : principal.getUser();
	}
}
