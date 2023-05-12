package com.syter6.jdbr.repositories;

import com.syter6.jdbr.BaseRepository;
import com.syter6.jdbr.Column;
import com.syter6.jdbr.ColumnType;
import com.syter6.jdbr.models.Product;

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
