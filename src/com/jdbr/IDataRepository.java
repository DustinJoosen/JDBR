package com.jdbr;

import java.util.ArrayList;

public interface IDataRepository<T> {
	ArrayList<T> getAll();
	ArrayList<T> getAll(String query);
	T getBy(String column, String value);
	T getById(int primary_key);
	T getById(String primary_key);
	boolean create(T data);
	boolean updateField(String primary_key, String column, String new_value, ColumnType column_type);
	boolean update(T data);
	boolean delete(T data);
}
