package com.czx.run.model;

import com.czx.run.model.User;

public class UserResult {
	
	private String resultCode;
	private String message;
	private User user;

	

	public UserResult(String resultCode, String message, User user) {
		super();
		this.resultCode = resultCode;
		this.message = message;
		this.user = user;
	}
	
	
	public String getResultCode() {
		return resultCode;
	}


	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	
}
