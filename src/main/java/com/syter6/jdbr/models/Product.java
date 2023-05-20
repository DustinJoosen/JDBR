package com.syter6.jdbr.models;

import com.syter6.jdbr.annotions.AutoIncrement;
import com.syter6.jdbr.annotions.PrimaryKey;

public class Product {

	@PrimaryKey
	@AutoIncrement
	public int num;

	public String name;

	public Product(int num, String name) {
		this.num = num;
		this.name = name;
	}

	public Product() {

	}

	@Override
	public String toString() {
		return "#" + this.num + ": " + this.name;
	}
}
