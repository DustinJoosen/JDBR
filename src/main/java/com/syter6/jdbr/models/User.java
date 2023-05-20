package com.syter6.jdbr.models;

import com.syter6.jdbr.annotions.DatabaseColumn;
import com.syter6.jdbr.annotions.PrimaryKey;

import java.time.LocalDate;

public class User {

	@PrimaryKey
	@DatabaseColumn(name = "email")
	public String emailadres;

	public String name;
	public int clearance;
	public LocalDate birthDate;
	public double grade;
	public boolean verified;

	public User() {}

	public User(String email, String name, int clearance, LocalDate birthDate, double grade, boolean verified) {
		this.emailadres = email;
		this.name = name;
		this.clearance = clearance;
		this.birthDate = birthDate;
		this.grade = grade;
		this.verified = verified;
	}

	@Override
	public String toString() {
		return this.emailadres + " (" + this.name + ")";
	}
}
