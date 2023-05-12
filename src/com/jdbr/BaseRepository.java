package com.jdbr;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public abstract class BaseRepository<T> implements IDataRepository<T>  {

	protected String table_name;
	protected Column[] columns;

	protected Connection conn;

	public BaseRepository(String table_name, Column[] columns) {
		this.table_name = table_name;
		this.columns = columns;

		this.connectToDatabase();
	}

	public void closeConnection() {
		try {
			this.conn.close();
		} catch (SQLException ex) {
			System.out.println("Error occurred when closing the connection");
			System.out.println(ex.getMessage());
		}
	}

	private void connectToDatabase() {
		String url = DatabaseAuth.URL;
		String username = DatabaseAuth.USERNAME;
		String password = DatabaseAuth.PASSWORD;

		try {
			this.conn = DriverManager.getConnection(url, username, password);
			this.conn.setAutoCommit(false);
		} catch (SQLException ex) {
			System.out.println("exception when connecting to the database");
			System.out.println(ex.getMessage());
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
	protected abstract T generate(ArrayList<String> values);

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

		try {
			Statement statement = this.conn.createStatement();

			ResultSet result_set = statement.executeQuery(query);

			// output.
			ArrayList<T> models = new ArrayList<>();

			// loop through all results
			while (result_set.next()) {

				// will hold all columns
				ArrayList<String> values = new ArrayList<>();

				// add all columns.
				for (Column column : this.columns) {
					values.add(result_set.getString(column.name));
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

		try {
			query = String.format("SELECT * FROM %s WHERE %s = ?", this.table_name, column);
			PreparedStatement statement = this.conn.prepareStatement(query);
			statement.setString(1, value);

			ResultSet result_set = statement.executeQuery();

			result_set.next();

			ArrayList<String> values = new ArrayList<>();

			// add all columns.
			for (Column col : this.columns) {
				values.add(result_set.getString(col.name));
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
		return this.getBy(this.columns[0].name, String.valueOf(primary_key));
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
		return this.getBy(this.columns[0].name, primary_key);
	}

	/**
	 *
	 * Creates a new record in the database from the data.
	 *
	 * @param  data		a model with the data to be inserted in the database.
	 * @return 			a boolean, indicating whether the update has worked.
	 */
	@Override
	public boolean create(T data) {
		if (data == null) {
			return false;
		}

		try {
			// Building the query.
			StringBuilder query = new StringBuilder("INSERT INTO " + this.table_name + " (");

			// Id, Name, Description) VALUES (
			for (int i = 0; i < this.columns.length; i++) {
				query.append(this.columns[i].name);

				if (i != this.columns.length - 1) {
					query.append(", ");
				}
			}
			query.append(") VALUES (");

			Class<?> c = data.getClass();

			// ?, ?, ?, ?);
			for (int i = 0; i < this.columns.length; i++) {
				query.append("?");
				if (i != this.columns.length - 1) {
					query.append(", ");
				}
			}
			query.append(");");

			// Preparing statement
			PreparedStatement statement = this.conn.prepareStatement(query.toString());

			int i = 0;
			for (Column column: this.columns) {
				Field field = c.getDeclaredField(column.name);
				field.setAccessible(true);

				// Get the value out of the column
				var val = field.get(data);
				String value = "";

				// In case of an integer.
				if (column.type == ColumnType.INT && val instanceof Integer ival) {
					if (ival != 0) {
						// Integer, but it has a value
						value = val.toString();
					} else {
						// Integer, but no value given
						if (i == 0) {
							// PK->generate
							value = String.valueOf(this.generateIntPK());
						} else {
							// Default column value
							value = column.getEmptyValue();
						}
					}
				} else {		// All other types
					if (val != null) {
						// A value was given
						value = val.toString();
					} else {
						// Default column value
						value = column.getEmptyValue();
					}
				}


				switch (column.type) {
					case INT -> statement.setInt(++i, Integer.parseInt(value));
					case BOOL -> statement.setBoolean(++i, value.equals("true"));
					case DOUBLE -> statement.setDouble(++i, Double.parseDouble(value));
					case DATE -> statement.setDate(++i, Date.valueOf(LocalDate.parse(value)));
					default -> statement.setString(++i, value);
				}
			}

			// Executing the array
			int success = statement.executeUpdate();

			// save the changes.
			this.conn.commit();
			statement.close();

			return success != 0;

		} catch (SQLException | NoSuchFieldException | IllegalAccessException ex) {
			System.out.println(ex.getMessage());
			return false;
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
	public boolean updateField(String primary_key, String column, String new_value, ColumnType column_type) {
		try {
			String query = "UPDATE " + this.table_name + " SET " + column + " = ? WHERE " + this.columns[0].name + " = ?";

			PreparedStatement statement = this.conn.prepareStatement(query);
			statement.setString(2, primary_key);

			switch (column_type) {
				case INT -> statement.setInt(1, Integer.parseInt(new_value));
				case BOOL -> statement.setBoolean(1, new_value.equals("1"));
				case DOUBLE -> statement.setDouble(1, Double.parseDouble(new_value));
				default -> statement.setString(1, new_value);
			}

			int success = statement.executeUpdate();

			// save the changes.
			this.conn.commit();
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

		try {
			// Building the query.
			StringBuilder query = new StringBuilder("UPDATE " + this.table_name + " SET ");
			Class<?> c = data.getClass();

			// start at 1 because the first item is primary key.
			for (int i = 1; i < this.columns.length; i++) {
				Field field = c.getDeclaredField(this.columns[i].name);
				field.setAccessible(true);

				String value = field.get(data).toString();

				// boolean handling
				if (this.columns[i].type == ColumnType.BOOL) {
					value = value.equals("true") ? "1" : "0";
				}

				query.append(this.columns[i].name).append(" = '").append(value).append("'");

				if (i != this.columns.length - 1) {
					query.append(", ");
				}
			}

			Field pk = c.getDeclaredField(this.columns[0].name);
			pk.setAccessible(true);

			query.append(" WHERE ").append(this.columns[0].name).append(" = '").append(pk.get(data)).append("'");

			// Executing the query
			Statement statement = this.conn.createStatement();

			int success = statement.executeUpdate(query.toString());

			// save the changes.
			this.conn.commit();
			statement.close();

			return success != 0;

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
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

		try {
			// Retrieving the primary key value.
			Class<?> c = data.getClass();

			Field primary_key = c.getDeclaredField(this.columns[0].name);
			primary_key.setAccessible(true);

			String pk_val = primary_key.get(data).toString();

			// Building the SQL query
			String query = String.format("DELETE FROM %s WHERE %s = ?", this.table_name, this.columns[0].name);

			// Executing the query
			PreparedStatement statement = this.conn.prepareStatement(query);
			statement.setString(1, pk_val);

			int success = statement.executeUpdate();

			// save the changes.
			this.conn.commit();
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

			String[] column_data = new String[this.columns.length];

			for (int j = 0; j < this.columns.length; j++) {
				try {
					Field field = c.getDeclaredField(this.columns[j].name);
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
		for (i = 0; i < this.columns.length; i++) {
			header.append(String.format("| %-25s", this.columns[i].name));
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

	protected int generateIntPK() {
		Column pk = this.columns[0];

		if (pk.type != ColumnType.INT) {
			return -1;
		}

		try {
			String query = "SELECT MAX(" + pk.name + ") FROM " + this.table_name;
			Statement statement = this.conn.createStatement();

			ResultSet result_set = statement.executeQuery(query);
			result_set.next();

			int biggest_val = result_set.getInt(1);
			return biggest_val + 1;

		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
			return -1;
		}

	}
}
