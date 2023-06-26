package com.syter6.jdbr.connectors;

import java.sql.Connection;

public interface IConnectAble extends AutoCloseable {
	public Connection open();
	public void close();
	public boolean isClosed();
}
