package com.syter6.jdbr.repositories;

import com.syter6.jdbr.BaseRepository;
import com.syter6.jdbr.models.Product;

public class ProductRepository extends BaseRepository<Product> {
	public ProductRepository() {
		super("product", Product::new);
	}
}
