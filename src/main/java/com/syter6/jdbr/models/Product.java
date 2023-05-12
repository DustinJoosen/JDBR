package com.syter6.jdbr.models;

public class Product {
	public int num;
	public String name;

	public Product(int num, String name) {
		this.num = num;
		this.name = name;
	}

	@Override
	public String toString() {
		return "#" + this.num + ": " + this.name;
	}
}
