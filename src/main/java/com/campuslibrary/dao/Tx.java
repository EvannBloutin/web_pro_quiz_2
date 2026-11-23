package com.campuslibrary.dao;

import com.campuslibrary.db.Db;
import com.campuslibrary.db.DbException;
import com.campuslibrary.util.ServiceException;

/** Runs a transaction and lets business errors (ServiceException) pass through unchanged. */
final class Tx {
    private Tx() {}
    static <T> T run(Db.Work<T> w) throws ServiceException {
        try {
            return Db.tx(w);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }
}
