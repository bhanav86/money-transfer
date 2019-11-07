package com.moneytransfer.dao.impl;

import com.moneytransfer.dao.AccountDAO;
import com.moneytransfer.dao.DAOFactory;
import com.moneytransfer.dao.UserDAO;
import com.moneytransfer.utils.PropsUitl;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.h2.tools.RunScript;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * H2 DAO
 */
public class H2DAOFactoryImpl implements DAOFactory {
	private static final String h2_driver = PropsUitl.getStringProperty("h2_driver");
	private static final String h2_connection_url = PropsUitl.getStringProperty("h2_connection_url");
	private static final String h2_user = PropsUitl.getStringProperty("h2_user");
	private static final String h2_password = PropsUitl.getStringProperty("h2_password");
	private static Logger LOGGER = Logger.getLogger(H2DAOFactoryImpl.class);

	private final UserDAOImpl userDAO = new UserDAOImpl();
	private final AccountDAOImpl accountDAO = new AccountDAOImpl();

	public H2DAOFactoryImpl() {
		// init: load driver
		DbUtils.loadDriver(h2_driver);
	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(h2_connection_url, h2_user, h2_password);

	}

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public AccountDAO getAccountDAO() {
		return accountDAO;
	}

	@Override
	public void populateTestData() {
		LOGGER.info("Populating User Table and data");
		Connection conn = null;
		try {
			conn = H2DAOFactoryImpl.getConnection();
			RunScript.execute(conn, new FileReader("src/main/resources/demo.sql"));
		} catch (SQLException e) {
			LOGGER.error("populateTestData(): Error populating user data: ", e);
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			LOGGER.error("populateTestData(): Error finding test script file ", e);
			throw new RuntimeException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

}
