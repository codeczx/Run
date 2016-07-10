package com.czx.run.model;

public class User {
	private String name;
	private String password;
	private String email;
	private String photo;
	private float weight;
	private String token;
	
	public User() {
		super();
	}



	public User(String name, String password, String email, String photo, float weight, String token) {
		super();
		this.name = name;
		this.password = password;
		this.email = email;
		this.photo = photo;
		this.weight = weight;
		this.token = token;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
