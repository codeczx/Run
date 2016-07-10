package com.czx.run.model;

public class CommonResult {
	private int resultCode;
	private String message;
	
	
	
	public CommonResult() {
		super();
	}

	public CommonResult(int resultCode, String message) {
		super();
		this.resultCode = resultCode;
		this.message = message;
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
	
}
