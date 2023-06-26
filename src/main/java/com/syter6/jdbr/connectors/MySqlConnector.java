package com.syter6.jdbr.connectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlConnector implements IConnectAble {

	private Connection conn;

	private final String url;
	private final String username;
	private final String password;

	public MySqlConnector(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public Connection open() {
		try {
			this.conn = DriverManager.getConnection(this.url, this.username, this.password);
			this.conn.setAutoCommit(false);

			return this.conn;
		} catch (SQLException ex) {
			System.out.println("an exception occured when connecting to the database");
			System.out.println(ex.getMessage());

			return null;
		}
	}


	@Override
	public boolean isClosed() {
		try {
			return this.conn.isClosed();
		} catch (SQLException ex) {
			return false;
		}
	}

	@Override
	public void close() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			System.out.println("An exception occured when attempting to close the database connection");
		}
	}
}
