package com.moneytransfer.dao;

import com.moneytransfer.dao.DAOFactory;
import com.moneytransfer.exception.MoneyTransferException;
import com.moneytransfer.model.User;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestUserDAO {

	private static Logger LOGGER = Logger.getLogger(TestUserDAO.class);

	private static final DAOFactory h2DaoFactory = DAOFactory.getDAOFactory();

	@BeforeClass
	public static void setup() {
		// prepare test database and test data by executing sql script demo.sql
		LOGGER.debug("setting up test database and sample data....");
		h2DaoFactory.populateTestData();
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testGetAllUsers() throws MoneyTransferException {
		List<User> allUsers = h2DaoFactory.getUserDAO().getAllUsers();
		assertTrue(allUsers.size() > 1);
	}

	@Test
	public void testGetUserById() throws MoneyTransferException {
		User u = h2DaoFactory.getUserDAO().getUserById(2L);
		assertTrue(u.getUserName().equals("ronaldo"));
	}

	@Test
	public void testGetNonExistingUserById() throws MoneyTransferException {
		User u = h2DaoFactory.getUserDAO().getUserById(500L);
		assertTrue(u == null);
	}

	@Test
	public void testExistingUserByName() throws MoneyTransferException {
		User u = h2DaoFactory.getUserDAO().getUserByName("ronaldo");
		assertTrue(u.getUserName().equals("ronaldo"));
	}

	@Test
	public void testGetNonExistingUserByName() throws MoneyTransferException {
		User u = h2DaoFactory.getUserDAO().getUserByName("abcdeftg");
		assertTrue(u == null);
	}

	@Test
	public void testCreateUser() throws MoneyTransferException {
		User u = new User("liandre", "liandre@gmail.com");
		long id = h2DaoFactory.getUserDAO().createUser(u);
		User uAfterInsert = h2DaoFactory.getUserDAO().getUserById(id);
		assertTrue(uAfterInsert.getUserName().equals("liandre"));
		assertTrue(u.getEmailAddress().equals("liandre@gmail.com"));
	}

	@Test
	public void testUpdateUser() throws MoneyTransferException {
		User u = new User(1L, "test2", "test2@gmail.com");
		int rowCount = h2DaoFactory.getUserDAO().updateUser(1L, u);

		// assert one row(user) updated
		assertTrue(rowCount == 1);
		User user = h2DaoFactory.getUserDAO().getUserById(1L);
		assertFalse(user.getEmailAddress().equals("yanglu@gmail.com"));
		assertTrue(user.getEmailAddress().equals("test2@gmail.com"));
	}

	@Test
	public void testUpdateNonExistingUser() throws MoneyTransferException {
		User u = new User(500L, "test2", "test2@gmail.com");
		int rowCount = h2DaoFactory.getUserDAO().updateUser(500L, u);
		// assert one row(user) updated
		assertTrue(rowCount == 0);
	}

	@Test
	public void testDeleteUser() throws MoneyTransferException {
		int rowCount = h2DaoFactory.getUserDAO().deleteUser(1L);
		// assert one row(user) deleted
		assertTrue(rowCount == 1);
		// assert user no longer there
		assertTrue(h2DaoFactory.getUserDAO().getUserById(1L) == null);
	}

	@Test
	public void testDeleteNonExistingUser() throws MoneyTransferException {
		int rowCount = h2DaoFactory.getUserDAO().deleteUser(500L);
		// assert no row(user) deleted
		assertTrue(rowCount == 0);
	}
}