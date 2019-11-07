package com.moneytransfer.dao;

import com.moneytransfer.dao.AccountDAO;
import com.moneytransfer.dao.DAOFactory;
import com.moneytransfer.dao.impl.H2DAOFactoryImpl;
import com.moneytransfer.exception.MoneyTransferException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.Transaction;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertTrue;

public class TestAccountBalance {

	private static Logger LOGGER = Logger.getLogger(TestAccountDAO.class);
	private static final DAOFactory h2DaoFactory = DAOFactory.getDAOFactory();
	private static final int THREADS_COUNT = 100;

	@BeforeClass
	public static void setup() {
		// prepare test database and test data, Test data are initialised from
		// src/main/resources/demo.sql
		h2DaoFactory.populateTestData();
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testAccountSingleThreadSameCcyTransfer() throws MoneyTransferException {

		final AccountDAO accountDAO = h2DaoFactory.getAccountDAO();

		BigDecimal transferAmount = new BigDecimal(50.01234).setScale(4, RoundingMode.HALF_EVEN);

		Transaction transaction = new Transaction("EUR", transferAmount, 3L, 4L);

		long startTime = System.currentTimeMillis();

		accountDAO.transferAccountBalance(transaction);
		long endTime = System.currentTimeMillis();

		LOGGER.info("TransferAccountBalance finished, time taken: " + (endTime - startTime) + "ms");

		Account accountFrom = accountDAO.getAccountById(3);

		Account accountTo = accountDAO.getAccountById(4);

		LOGGER.debug("Account From: " + accountFrom);

		LOGGER.debug("Account From: " + accountTo);

		assertTrue(
				accountFrom.getBalance().compareTo(new BigDecimal(449.9877).setScale(4, RoundingMode.HALF_EVEN)) == 0);
		assertTrue(accountTo.getBalance().equals(new BigDecimal(550.0123).setScale(4, RoundingMode.HALF_EVEN)));

	}

	@Test
	public void testAccountMultiThreadedTransfer() throws InterruptedException, MoneyTransferException {
		final AccountDAO accountDAO = h2DaoFactory.getAccountDAO();
		// transfer a total of 200USD from 100USD balance in multi-threaded
		// mode, expect half of the transaction fail
		final CountDownLatch latch = new CountDownLatch(THREADS_COUNT);
		final AtomicInteger atomicInteger = new AtomicInteger();
		for (int i = 0; i < THREADS_COUNT; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Transaction transaction = new Transaction("USD",
								new BigDecimal(2).setScale(4, RoundingMode.HALF_EVEN), 1L, 2L);
						accountDAO.transferAccountBalance(transaction);
					} catch (Exception e) {
						atomicInteger.incrementAndGet();
						LOGGER.error("Error occurred during transfer ", e);
					} finally {
						latch.countDown();
					}
				}
			}).start();
		}

		latch.await();

		Account accountFrom = accountDAO.getAccountById(1);

		Account accountTo = accountDAO.getAccountById(2);
		
		LOGGER.debug("Account From: " + accountFrom);

		LOGGER.debug("Account From: " + accountTo);
		
		assertTrue(atomicInteger.get() == THREADS_COUNT / 2);

		assertTrue(accountFrom.getBalance().equals(new BigDecimal(0).setScale(4, RoundingMode.HALF_EVEN)));
		assertTrue(accountTo.getBalance().equals(new BigDecimal(300).setScale(4, RoundingMode.HALF_EVEN)));

	}

	@Test
	public void testTransferFailOnDBLock() throws MoneyTransferException, SQLException {
		BigDecimal originalBalance = new BigDecimal(500).setScale(4, RoundingMode.HALF_EVEN);
		final String SQL_LOCK_ACC = "SELECT * FROM Account WHERE AccountId = 5 FOR UPDATE";
		
		Connection conn = null;
		PreparedStatement lockStmt = null;
		ResultSet rs = null;
		Account fromAccount = null;

		try {
			conn = H2DAOFactoryImpl.getConnection();
			conn.setAutoCommit(false);
			// lock account for writing:
			lockStmt = conn.prepareStatement(SQL_LOCK_ACC);
			rs = lockStmt.executeQuery();
			if (rs.next()) {
				fromAccount = new Account(rs.getLong("AccountId"), rs.getString("UserName"),
						rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Locked Account: " + fromAccount);
			}
			if (fromAccount == null) {
				throw new MoneyTransferException("Locking error during test, SQL = " + SQL_LOCK_ACC);
			}
			// after lock account 5, try to transfer from account 6 to 5
			// default h2 timeout for acquire lock is 1sec
			BigDecimal transferAmount = new BigDecimal(50).setScale(4, RoundingMode.HALF_EVEN);

			Transaction transaction = new Transaction("GBP", transferAmount, 6L, 5L);
			h2DaoFactory.getAccountDAO().transferAccountBalance(transaction);
			conn.commit();
		} catch (Exception e) {
			LOGGER.error("Exception occurred, initiate a rollback");
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException re) {
				LOGGER.error("Fail to rollback transaction", re);
			}
		} finally {
			DbUtils.closeQuietly(conn);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(lockStmt);
		}

		// now inspect account 3 and 4 to verify no transaction occurred
		assertTrue(h2DaoFactory.getAccountDAO().getAccountById(6).getBalance().equals(originalBalance));
		assertTrue(h2DaoFactory.getAccountDAO().getAccountById(5).getBalance().equals(originalBalance));
	}

}
