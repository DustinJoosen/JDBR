package com.syter6.jdbr;

public class ColumnDefinition {
	public String attributeName;
	public String columnName;

	public ColumnDefinitionType type;
	public boolean isPrimaryKey;
	public boolean isAutoIncremented;
	public boolean isRequired;

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
