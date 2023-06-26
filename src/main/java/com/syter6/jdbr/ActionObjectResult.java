package com.syter6.jdbr;

public class ActionObjectResult<T> {
	public T createdObject;
	public boolean succeeded;
	public Exception exception;
	public int generatedPrimaryKey;

	public ActionObjectResult(T createdObject, boolean succeeded) {
		this(createdObject, succeeded, null);
	}

	public ActionObjectResult(T createdObject, boolean succeeded, Exception exception) {
		this(createdObject, succeeded, exception, -1);
	}

	public ActionObjectResult(T createdObject, boolean succeeded, int pk) {
		this(createdObject, succeeded, null, pk);
	}

	public ActionObjectResult(T createdObject, boolean succeeded, Exception exception, int pk) {
		this.createdObject = createdObject;
		this.succeeded = succeeded;
		this.exception = exception;
		this.generatedPrimaryKey = pk;
	}
}
