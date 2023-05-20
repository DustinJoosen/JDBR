package com.syter6.jdbr.repositories;

import com.syter6.jdbr.BaseRepository;
import com.syter6.jdbr.models.Product;

import java.util.ArrayList;

public class ProductRepository extends BaseRepository<Product> {
	public ProductRepository() {
		super("product");
	}

	@Override
	protected Product generate(ArrayList<String> values) {
		return new Product(Integer.parseInt(values.get(0)), values.get(1));
	}
}
