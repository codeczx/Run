package com.czx.run.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class RunRecord implements Serializable{

	private String email;
	private Timestamp date;
	private float distance;
	private float calorie;
	private float runTime;
	private String pointsKey;
	private String picKey;
	private String address;
	
	
	
	
	public RunRecord() {
		super();
	}







	public RunRecord(String email, Timestamp date, float distance, float calorie, float runTime, String pointsKey,
			String picKey, String address) {
		super();
		this.email = email;
		this.date = date;
		this.distance = distance;
		this.calorie = calorie;
		this.runTime = runTime;
		this.pointsKey = pointsKey;
		this.picKey = picKey;
		this.address = address;
	}







	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}



	public Timestamp getDate() {
		return date;
	}



	public void setDate(Timestamp date) {
		this.date = date;
	}



	public float getDistance() {
		return distance;
	}



	public void setDistance(float distance) {
		this.distance = distance;
	}



	public float getCalorie() {
		return calorie;
	}



	public void setCalorie(float calorie) {
		this.calorie = calorie;
	}



	public float getRunTime() {
		return runTime;
	}



	public void setRunTime(float runTime) {
		this.runTime = runTime;
	}



	public String getPointsKey() {
		return pointsKey;
	}



	public void setPointsKey(String pointsKey) {
		this.pointsKey = pointsKey;
	}



	public String getPicKey() {
		return picKey;
	}



	public void setPicKey(String picKey) {
		this.picKey = picKey;
	}







	public String getAddress() {
		return address;
	}







	public void setAddress(String address) {
		this.address = address;
	}
	
	
	
	
	
	
}
