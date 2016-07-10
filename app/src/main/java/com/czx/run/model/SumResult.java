package com.czx.run.model;

public class SumResult {
	private int resultCode;
	private String message;
	private float sumDistance;
	private float sumTime;
	private float sumRunTime;
	private float sumCalorie;
	
	public SumResult(int resultCode, String message, float sumDistance, float sumTime, float sumRunTime,
			float sumCalorie) {
		super();
		this.resultCode = resultCode;
		this.message = message;
		this.sumDistance = sumDistance;
		this.sumTime = sumTime;
		this.sumRunTime = sumRunTime;
		this.sumCalorie = sumCalorie;
	}
	public SumResult() {
		super();
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
	public float getSumDistance() {
		return sumDistance;
	}
	public void setSumDistance(float sumDistance) {
		this.sumDistance = sumDistance;
	}
	public float getSumTime() {
		return sumTime;
	}
	public void setSumTime(float sumTime) {
		this.sumTime = sumTime;
	}
	public float getSumRunTime() {
		return sumRunTime;
	}
	public void setSumRunTime(float sumRunTime) {
		this.sumRunTime = sumRunTime;
	}
	public float getSumCalorie() {
		return sumCalorie;
	}
	public void setSumCalorie(float sumCalorie) {
		this.sumCalorie = sumCalorie;
	}
	
	
}
