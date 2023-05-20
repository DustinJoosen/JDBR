package com.syter6.jdbr.models;

import java.time.LocalDate;

public class User {
	public String email;
	public String name;
	public int clearance;
	public LocalDate birthDate;
	public double grade;
	public boolean verified;

	public User() {}

	public User(String email, String name, int clearance, LocalDate birthDate, double grade, boolean verified) {
		this.email = email;
		this.name = name;
		this.clearance = clearance;
		this.birthDate = birthDate;
		this.grade = grade;
		this.verified = verified;
	}

	@Override
	public String toString() {
		return this.email + " (" + this.name + ")";
	}
}
