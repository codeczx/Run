package com.czx.run.model;

import java.util.List;


public class RecordResult {
	private int resultCode;
	private String message;
	private List<RunRecord> records;
	
	public RecordResult() {
		super();
	}

	public RecordResult(int resultCode, String message, List<RunRecord> records) {
		super();
		this.resultCode = resultCode;
		this.message = message;
		this.records = records;
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

	public List<RunRecord> getRecords() {
		return records;
	}

	public void setRecords(List<RunRecord> records) {
		this.records = records;
	}
	
	
}
