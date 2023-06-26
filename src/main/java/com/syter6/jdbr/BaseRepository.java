package com.syter6.jdbr;


import com.syter6.jdbr.connectors.IConnectAble;
import com.syter6.jdbr.connectors.MySqlConnector;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.Supplier;

public abstract class BaseRepository<T> implements IDataRepository<T>, AutoCloseable  {

	protected String table_name;

	protected ArrayList<ColumnDefinition> columnDefinitions;
	protected ColumnDefinition pk;

	protected Supplier<T> supplier;
	protected Class<?> clazz;

	protected static IConnectAble defaultConnector;
	protected IConnectAble connector;

	public BaseRepository(String table_name, Supplier<T> supplier) {
		this(table_name, supplier, (BaseRepository.defaultConnector != null)
				? BaseRepository.defaultConnector
				: new MySqlConnector(DatabaseAuth.URL, DatabaseAuth.USERNAME, DatabaseAuth.PASSWORD)
		);
	}

	public BaseRepository(String table_name, Supplier<T> supplier, IConnectAble connector) {
		this.connector = connector;
		this.connector.open();

		this.table_name = table_name;
		this.supplier = supplier;

		this.clazz = this.supplier.get().getClass();

		this.columnDefinitions = this.getColumnDefinitions();

		// Annotations
		AnnotationsHandler<T> handler = new AnnotationsHandler<T>(this);
		handler.assignAttributeNames();
		handler.assignPK();
		handler.assignAutoIncrements();
		handler.assignRequired();
	}

	public static void setConnector(IConnectAble connector) {
		if (connector == null) {
			return;
		}

		BaseRepository.defaultConnector = connector;
	}

	private ArrayList<ColumnDefinition> getColumnDefinitions() {
		String query = "SHOW COLUMNS FROM " + this.table_name;
		ArrayList<ColumnDefinition> columnDefinitions = new ArrayList<>();

		try (Connection conn = this.connector.open()) {
			Statement statement = conn.createStatement();
			ResultSet result_set = statement.executeQuery(query);

			while (result_set.next()) {
				columnDefinitions.add(new ColumnDefinition(
						result_set.getString("field"),
						ColumnDefinition.typeFrom(result_set.getString("type"))));
			}

			statement.close();
			return columnDefinitions;

		} catch (SQLException ex) {
			System.out.println("===EXCEPTION===\nUsed SQL query (not always the problem):");
			return null;
		}
	}

	/**
	 *
	 * An abstract method. It's implementations recieve an ArrayList of values.
	 * The method is expected to generate a new object, and assign everything to the correct attribute.
	 *
	 * @param  values  	a list of all values used to instantiate the new object
	 * @return      	the newly generated object.
	 */
	private T generate(ArrayList<String> values) {
		T generic_obj = this.supplier.get();

		// For datetimes
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		try {
			int i = 0;
			for (ColumnDefinition def: this.columnDefinitions) {

				// Get the field of this def (taking casing in consideration)
				Field field = null;

				for (Field f: this.clazz.getDeclaredFields()) {
					if (f.getName().toLowerCase().equals(def.attributeName.toLowerCase())) {
						field = f;
						break;
					}
				}

				assert field != null : "field is not found";
				field.setAccessible(true);

				try {
					switch (def.type) {
						case STRING -> field.set(generic_obj, values.get(i++));
						case INT -> field.set(generic_obj, Integer.parseInt(values.get(i++)));
						case DATE -> field.set(generic_obj, LocalDate.parse(values.get(i++), formatter));
						case BOOL -> field.set(generic_obj, values.get(i++).equals("1"));
						case DOUBLE -> field.set(generic_obj, Double.parseDouble(values.get(i++)));
					};
				} catch (NumberFormatException | NullPointerException ex) {
					field.set(generic_obj, 0);
				}
			}

			return generic_obj;

		} catch (IllegalAccessException ex) {
			System.out.println(ex.getMessage());
			return null;
		}
	}

	/**
	 *
	 * Executes a `SELECT *` SQL query on the given table.
	 * Converts retrieved data to the given type.
	 *
	 * @return      An ArrayList of all found objects.
	 */
	@Override
	public ArrayList<T> getAll() {
		return this.getAll("SELECT * FROM " + this.table_name);
	}

	/**
	 *
	 * Executes a `SELECT *` SQL query on the given table.
	 * Converts retrieved data to the given type.
	 *
	 * @param 	query	The query to be done on the database
	 * @return      	An ArrayList of all found objects.
	 */
	@Override
	public ArrayList<T> getAll(String query) {

		try (var conn = this.connector.open()) {
			Statement statement = conn.createStatement();

			ResultSet result_set = statement.executeQuery(query);

			// output.
			ArrayList<T> models = new ArrayList<>();

			// loop through all results
			while (result_set.next()) {

				// will hold all columns
				ArrayList<String> values = new ArrayList<>();

				// add all columns.
				for (ColumnDefinition columnDefinition : this.columnDefinitions) {
					values.add(result_set.getString(columnDefinition.columnName));
				}

				// use values to generate model T.
				T model = this.generate(values);
				models.add(model);
			}

			return models;

		} catch (SQLException ex) {
			System.out.println("===EXCEPTION===\nUsed SQL query (not always the problem):");
			System.out.println(query);
			System.out.println(ex.getMessage());
			return null;
		}
	}

	/**
	 *
	 * Searches for the first record where the given column has the given value.
	 *
	 * @param column		The column in the database table to search for.
	 * @param value			The value to check for in the given column.
	 * @return      		If found, the record, converted to a model. Otherwise null.
	 */
	@Override
	public T getBy(String column, String value) {
		String query = "";

		try (var conn = this.connector.open()) {
			query = String.format("SELECT * FROM %s WHERE %s = ?", this.table_name, column);
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, value);

			ResultSet result_set = statement.executeQuery();

			result_set.next();

			ArrayList<String> values = new ArrayList<>();

			// add all columns.
			for (ColumnDefinition col : this.columnDefinitions) {
				values.add(result_set.getString(col.columnName));
			}

			// use values to generate model T.
			statement.close();
			return this.generate(values);

		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
			return null;
		}
	}


	/**
	 *
	 * Searches for the first record where the primary key (first item in records) matches the given primary key.
	 *
	 * @param  primary_key  The primary key used to search for the model.
	 * @return      		If found, the model. Otherwise null.
	 */
	@Override
	public T getById(int primary_key) {
		return this.getBy(this.pk.columnName, String.valueOf(primary_key));
	}

	/**
	 *
	 * Searches for the first record where the primary key (first item in records) matches the given primary key.
	 *
	 * @param  primary_key  The primary key used to search for the model.
	 * @return      		If found, the model. Otherwise null.
	 */
	@Override
	public T getById(String primary_key) {
		return this.getBy(this.pk.columnName, primary_key);
	}

	/**
	 *
	 * Creates a new record in the database from the data.
	 *
	 * @param  data		a model with the data to be inserted in the database.
	 * @return 			a boolean, indicating whether the update has worked.
	 */
	@Override
	public ActionObjectResult<T> create(T data) {
		if (data == null) {
			return new ActionObjectResult<>(null, false);
		}

		try (var conn = this.connector.open()) {
			var used_columns = new ArrayList<ColumnDefinition>();

			// Assign all columns that are going to be used in the sql query. (all required + all that have data).
			for (ColumnDefinition def : this.columnDefinitions) {

				// If it is an autoincrement primary key, don't add it and let the db handle it.
				if (def.isPrimaryKey && def.isAutoIncremented) {
					continue;
				}

				// Required columns are ALWAYS in the query.
				if (def.isRequired) {
					used_columns.add(def);
					continue;
				}

				// Check whether the field contains actual data.
				Object object = this.clazz.getField(def.attributeName).get(data);

				// It's a non-required field. Only add it to the query if it contains data. Otherwise it'd be pointless.
				if (this.doesThisObjectContainInfo(object, def.type)) {
					used_columns.add(def);
				}
			}

			// INSERT INTO table_name (name, desc)
			StringBuilder query = new StringBuilder("INSERT INTO " + this.table_name + " (");
			int i = 0;
			for (ColumnDefinition def : used_columns) {
				query.append("`").append(def.columnName).append("`");
				if (i++ < used_columns.size() - 1) {
					query.append(", ");
				} else {
					query.append(") ");
				}
			}

			// VALUES (?, ?);
			query.append("VALUES (").append("?, ".repeat(used_columns.size() - 1)).append("?);");

			// Prepare the values into the statement.
			PreparedStatement statement = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);

			i = 0;
			for (ColumnDefinition def : used_columns) {
				Field field = this.clazz.getDeclaredField(def.attributeName);
				field.setAccessible(true);

				String value = field.get(data).toString();

				switch (def.type) {
					case INT -> statement.setInt(++i, Integer.parseInt(value));
					case DOUBLE -> statement.setDouble(++i, Double.parseDouble(value));
					case BOOL -> statement.setBoolean(++i, Boolean.parseBoolean(value));
					case DATE -> statement.setDate(++i, Date.valueOf(LocalDate.parse(value)));
					default -> statement.setString(++i, value);
				}
			}

			// Executing the statement and saving the changes
			boolean success = statement.executeUpdate() != 0;

			// Getting the used primary key (if any)
			int generatedPk = -1;

			ResultSet keys = statement.getGeneratedKeys();
			if (keys.next()) {
				generatedPk = keys.getInt(1);

				// Add the primary key to the model
				Field field = this.clazz.getDeclaredField(this.pk.attributeName);
				field.setAccessible(true);
				field.set(data, generatedPk);
			}

			// Saving and closing all.
			conn.commit();
			statement.close();

			return new ActionObjectResult<>(data, success, generatedPk);

		} catch (NoSuchFieldException | IllegalAccessException | SQLException ex) {
			System.out.println("===FAILURE===");
			return new ActionObjectResult<>(data, false, ex);
		}
	}


	/**
	 *
	 * Updates a single column.
	 *
	 * @param  primary_key		The primary key of the record to be updated
	 * @param  column			The column to be updated
	 * @param  new_value		The new value of the column.
	 * @return 			a boolean, indicating whether the update has worked.
	 */
	@Override
	public boolean updateField(String primary_key, String column, String new_value, ColumnDefinitionType column_type) {
		try (var conn = this.connector.open()) {
			String query = "UPDATE " + this.table_name + " SET " + column + " = ? WHERE " + this.pk.columnName + " = ?";

			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(2, primary_key);

			switch (column_type) {
				case INT -> statement.setInt(1, Integer.parseInt(new_value));
				case BOOL -> statement.setBoolean(1, new_value.equals("1"));
				case DOUBLE -> statement.setDouble(1, Double.parseDouble(new_value));
				default -> statement.setString(1, new_value);
			}

			int success = statement.executeUpdate();

			// save the changes.
			conn.commit();
			statement.close();

			return success != 0;

		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}

	/**
	 *
	 * Updates a record, based on the primary key of the data.
	 *
	 * @param  data		a model with the data to be updated.
	 * @return 			a boolean, indicating whether the update has worked.
	 */
	public boolean update(T data) {
		if (data == null) {
			return false;
		}

		try (var conn = this.connector.open()) {
			// Building the query.
			StringBuilder query = new StringBuilder("UPDATE " + this.table_name + " SET ");
			Class<?> c = data.getClass();

			// start at 1 because the first item is primary key.
			for (int i = 1; i < this.columnDefinitions.size(); i++) {
				Field field = c.getDeclaredField(this.columnDefinitions.get(i).attributeName);
				field.setAccessible(true);

				String value = field.get(data).toString();

				// boolean handling
				if (this.columnDefinitions.get(i).type == ColumnDefinitionType.BOOL) {
					value = value.equals("true") ? "1" : "0";
				}

				query.append(this.columnDefinitions.get(i).columnName).append(" = '").append(value).append("'");

				if (i != this.columnDefinitions.size() - 1) {
					query.append(", ");
				}
			}

			Field pk = c.getDeclaredField(this.pk.attributeName);
			pk.setAccessible(true);

			query.append(" WHERE ").append(this.pk.columnName).append(" = '").append(pk.get(data)).append("'");

			// Executing the query
			Statement statement = conn.createStatement();

			int success = statement.executeUpdate(query.toString());

			// save the changes.
			conn.commit();
			statement.close();

			return success != 0;

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}

	/**
	 *
	 * Deletes all records of the table.
	 *
	 * @return 			a boolean, indicating whether the update has worked.
	 */
	@Override
	public boolean truncate() {
		for (T model: this.getAll()) {
			if (!this.delete(model)) {
				return false;
			}
		}

		return true;
	}

	/**
	 *
	 * Deletes the given record. It uses the primary key of the data.
	 *
	 * @param  data	the model to be deleted
	 * @return 			a boolean, indicating whether the update has worked.
	 */
	@Override
	public boolean delete(T data) {
		if (data == null) {
			return false;
		}

		try (var conn = this.connector.open()) {
			// Retrieving the primary key value.
			Class<?> c = data.getClass();

			Field primary_key = c.getDeclaredField(this.pk.attributeName);
			primary_key.setAccessible(true);

			String pk_val = primary_key.get(data).toString();

			// Building the SQL query
			String query = String.format("DELETE FROM %s WHERE %s = ?", this.table_name, this.pk.columnName);

			// Executing the query
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, pk_val);

			int success = statement.executeUpdate();

			// save the changes.
			conn.commit();
			statement.close();

			return success != 0;

		} catch (SQLException | NoSuchFieldException | IllegalAccessException ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}

	/**
	 *
	 * Displays all records in a table.
	 */
	public void printAll() {
		var records = this.getAll();
		if (records == null || records.size() == 0) {
			System.out.println("There are no records in this table");
			return;
		}

		System.out.println("Data retrieved from table [" + this.table_name + "] (" + records.size() + " records)");

		String[][] table = new String[records.size()][];


		Class<?> c = records.get(0).getClass();

		// Save data
		int i = 0;
		for (T record: records) {

			String[] column_data = new String[this.columnDefinitions.size()];

			for (int j = 0; j < this.columnDefinitions.size(); j++) {
				try {
					Field field = c.getDeclaredField(this.columnDefinitions.get(j).attributeName);
					field.setAccessible(true);

					var val = field.get(record);
					if (val == null) {
						continue;
					}

					String value = val.toString();

					// Strip too long text
					if (value.length() >= 20) {
						value = value.substring(0, 20) + "...";
					}

					column_data[j] = value;
				} catch (NoSuchFieldException | IllegalAccessException ex) {
					continue;
				}
			}

			table[i++] = column_data;
		}

		// Head
		StringBuilder header = new StringBuilder();

		for (ColumnDefinition def: this.columnDefinitions) {
			header.append(String.format("| %-25s", def.columnName));
		}
		System.out.println(header + "|");

		// Body
		for (i = 0; i < table.length; i++) {
			String[] row = table[i];

			StringBuilder body = new StringBuilder();
			for (String col: row) {
				body.append(String.format("| %-25s", col));
			}

			System.out.println(body + "|");
		}
	}


	private boolean doesThisObjectContainInfo(Object obj, ColumnDefinitionType type) {
		try {
			return switch (type) {
				case INT -> ((int) obj) != 0;
				case BOOL -> true;
				case DOUBLE -> ((double) obj) != 0.0;
				case DATE -> obj != null;
				case STRING -> obj != null && !obj.equals("");
			};
		} catch (NullPointerException ex) {
			return false;
		}
	}

	@Override
	public void close() {
		try {
			this.connector.close();
		} catch (Exception ex) {
			System.out.println("An exception occured when attempting to close the database connection");
		}
	}
}
