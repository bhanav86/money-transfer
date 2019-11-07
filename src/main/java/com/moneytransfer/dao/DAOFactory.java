package com.moneytransfer.dao;

import com.moneytransfer.dao.impl.H2DAOFactoryImpl;

/**
 * DAO factory
 */
public interface DAOFactory {

	/**
	 * User DAO object
	 * 
	 * @return User DAO object
	 */
	UserDAO getUserDAO();

	/**
	 * Account DAO object
	 * 
	 * @return Account DAO object
	 */
	AccountDAO getAccountDAO();

	/**
	 * Populate data
	 */
	void populateTestData();

	/**
	 * In memory DAO object
	 * 
	 * @return In memory DAO object
	 */
	static DAOFactory getDAOFactory() {
		return new H2DAOFactoryImpl();
	}
}
