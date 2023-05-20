package com.syter6.jdbr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ColumnDefinition {
	public String attributeName;
	public String columnName;

	public ColumnDefinitionType type;
	public boolean isPrimaryKey;
	public boolean isAutoIncremented;

	public ColumnDefinition(String columnName, ColumnDefinitionType type) {
		this(columnName, columnName, type);
	}

	public ColumnDefinition(String columnName, String attributeName, ColumnDefinitionType type) {
		this.columnName = columnName;
		this.attributeName = attributeName;

		this.type = type;

		this.isPrimaryKey = false;
		this.isAutoIncremented = false;
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

	@Override
	public String toString() {
		return this.attributeName + " (" + this.type + ")";
	}

	public static ColumnDefinitionType typeFrom(String type) {
		if (type.contains("varchar")) {
			return ColumnDefinitionType.STRING;
		} else if (type.contains("tinyint")) {
			return ColumnDefinitionType.BOOL;
		} else if (type.contains("int")) {
			return ColumnDefinitionType.INT;
		} else if (type.contains("decimal")) {
			return ColumnDefinitionType.DOUBLE;
		} else if (type.contains("datetime")) {
			return ColumnDefinitionType.DATE;
		} else {
			return ColumnDefinitionType.STRING;
		}
	}
}
