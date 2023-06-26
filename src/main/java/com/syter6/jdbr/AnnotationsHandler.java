package com.syter6.jdbr;

import com.syter6.jdbr.annotions.AutoIncrement;
import com.syter6.jdbr.annotions.DatabaseColumn;
import com.syter6.jdbr.annotions.PrimaryKey;
import com.syter6.jdbr.annotions.Required;

import java.lang.reflect.Field;

public class AnnotationsHandler<T> {

	private final BaseRepository<T> repos;

	public AnnotationsHandler(BaseRepository<T> repos) {
		this.repos = repos;
	}

	/**
	 *
	 * Assigns the first attribute of the generic type T that has the PrimaryKey annotation, to the pk attribute.
	 *
	 */
	public void assignPK() {
		// Loop through all fields of the generic type T.
		for (Field f: this.repos.clazz.getDeclaredFields()) {
			var annotation = f.getAnnotation(PrimaryKey.class);
			if (annotation == null) {
				continue;
			}

			// It's a PK. Find the matching columnDefinition.
			for (ColumnDefinition def: this.repos.columnDefinitions) {
				if (def.attributeName.equals(f.getName())) {
					def.isPrimaryKey = true;
					this.repos.pk = def;

					return;
				}
			}

		}

		// Either no PK was set, or it didn't match any columns of the generic type T.
		// So just have it be the first column in the sql table.
		this.repos.pk = this.repos.columnDefinitions.get(0);
	}


	/**
	 *
	 * Assign the attribute name to any attribute with a DatabaseColumn annotation.
	 *
	 */
	public void assignAttributeNames() {
		// Loop through all fields of the generic type T.
		for (Field f: this.repos.clazz.getDeclaredFields()) {
			var annotation = f.getAnnotation(DatabaseColumn.class);
			if (annotation == null) {
				continue;
			}

			String attributeName = f.getName();		// ORM
			String columnName = annotation.name();	// SQL

			for (ColumnDefinition def : this.repos.columnDefinitions) {
				if (def.columnName.equals(columnName)) {
					def.attributeName = attributeName;
					break;
				}
			}
		}
	}


	/**
	 *
	 * Assigns the AutoIncrement to any ColumnDefinition where the attribute has the AutoIncrement annotation
	 *
	 */
	public void assignAutoIncrements() {
		// Loop through all fields of the generic type T.
		for (Field f: this.repos.clazz.getDeclaredFields()) {
			var annotation = f.getAnnotation(AutoIncrement.class);
			if (annotation == null) {
				continue;
			}

			for (ColumnDefinition def : this.repos.columnDefinitions) {
				if (!def.attributeName.equals(f.getName())) {
					continue;
				}

				def.isAutoIncremented = true;
			}
		}
	}

	public void assignRequired() {
		// Loop through all fields of the generic type T.
		for (Field f: this.repos.clazz.getDeclaredFields()) {
			var annotation = f.getAnnotation(Required.class);
			if (annotation == null) {
				continue;
			}

			for (ColumnDefinition def : this.repos.columnDefinitions) {
				if (!def.attributeName.equals(f.getName())) {
					continue;
				}

				def.isRequired = true;
			}
		}
	}

}
