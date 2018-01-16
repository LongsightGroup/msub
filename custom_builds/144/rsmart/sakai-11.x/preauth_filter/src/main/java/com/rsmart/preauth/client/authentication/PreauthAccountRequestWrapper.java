package com.rsmart.preauth.client.authentication;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Http Request Wrapper to add the user id to the request  
 * 
 * @author Earle Nietzel
 *
 */
public class PreauthAccountRequestWrapper extends HttpServletRequestWrapper {

	final private String user;
	
	public PreauthAccountRequestWrapper(HttpServletRequest request, String user) {
		super(request);
		this.user = user;
	}

	@Override
	public Principal getUserPrincipal() {
		if (this.user == null) {
			return super.getUserPrincipal();
		}
		
		return new Principal() {
			
			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return user;
			}
		};
	}

	@Override
	public String getRemoteUser() {
		if (this.user == null) {
			return super.getRemoteUser();
		}
		return this.user;
	}
}
