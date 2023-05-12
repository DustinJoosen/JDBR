package com.jdbr.repositories;

import com.jdbr.BaseRepository;
import com.jdbr.Column;
import com.jdbr.ColumnType;
import com.jdbr.models.Product;

import java.util.ArrayList;

public class ProductRepository extends BaseRepository<Product> {
	public static String table_name = "product";
	public static Column[] columns = new Column[] {
		new Column("num", ColumnType.INT),
		new Column("name", ColumnType.STRING)
	};

	public ProductRepository() {
		super(ProductRepository.table_name, ProductRepository.columns);
	}

	@Override
	protected Product generate(ArrayList<String> values) {
		return new Product(Integer.parseInt(values.get(0)), values.get(1));
	}
}
