package com.syter6.jdbr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Column {
	public String name;
	public ColumnType type;
	public boolean foreign_key;

	public Column(String name) {
		this(name, ColumnType.STRING);
	}

	public Column(String name, ColumnType type) {
		this(name, type, false);
	}

	public Column(String name, ColumnType type, boolean foreign_key) {
		this.name = name;
		this.type = type;
		this.foreign_key = foreign_key;
	}

	// When the value is empty, this should be inserted:
	public String getEmptyValue() {
		var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		return switch (this.type) {
			case INT -> "0";
			case BOOL -> "false";
			case DATE -> LocalDate.now().format(formatter);
			case DOUBLE -> "0.0";
			default -> "null";
		};


	}
}

