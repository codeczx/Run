package com.czx.run.model;

public class LoginResult{
	private int resultCode;
	private String message;
	private String token;

	public LoginResult(int resultCode, String message, String token) {
		super();
		this.resultCode = resultCode;
		this.message = message;
		this.token = token;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
	
}
