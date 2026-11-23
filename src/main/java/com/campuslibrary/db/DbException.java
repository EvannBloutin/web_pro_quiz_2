package com.campuslibrary.db;

/** Unchecked wrapper for SQL errors. */
public class DbException extends RuntimeException {
    public DbException(Throwable cause) { super(cause); }
}
