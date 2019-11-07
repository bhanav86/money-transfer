package com.moneytransfer.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.moneytransfer.dao.AccountDAO;
import com.moneytransfer.exception.MoneyTransferException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.Transaction;
import com.moneytransfer.utils.MoneyTransferUtil;

public class AccountDAOImpl implements AccountDAO {

	private static Logger LOGGER = Logger.getLogger(AccountDAOImpl.class);
	
	private final static String SQL_GET_ACC_BY_ID = "SELECT * FROM Account WHERE AccountId = ? ";
	private final static String SQL_LOCK_ACC_BY_ID = "SELECT * FROM Account WHERE AccountId = ? FOR UPDATE";
	private final static String SQL_CREATE_ACC = "INSERT INTO Account (UserName, Balance, CurrencyCode) VALUES (?, ?, ?)";
	private final static String SQL_UPDATE_ACC_BALANCE = "UPDATE Account SET Balance = ? WHERE AccountId = ? ";
	private final static String SQL_GET_ALL_ACC = "SELECT * FROM Account";
	private final static String SQL_DELETE_ACC_BY_ID = "DELETE FROM Account WHERE AccountId = ?";

	/**
	 * Get all accounts.
	 */
	public List<Account> getAllAccounts() throws MoneyTransferException {
		LOGGER.info("Before: get all accounts");
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Account> allAccounts = new ArrayList<Account>();
		try {
			conn = H2DAOFactoryImpl.getConnection();
			stmt = conn.prepareStatement(SQL_GET_ALL_ACC);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Account acc = this.getAccount(rs);
				allAccounts.add(acc);
			}
			LOGGER.info("After: get all accounts");
			return allAccounts;
		} catch (SQLException e) {
			throw new MoneyTransferException("getAccountById(): Error reading account data", e);
		} finally {
			DbUtils.closeQuietly(conn, stmt, rs);
		}
	}

	/**
	 * Get account by id
	 */
	public Account getAccountById(long accountId) throws MoneyTransferException {
		LOGGER.info("Before: get account by id");
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Account acc = null;
		try {
			conn = H2DAOFactoryImpl.getConnection();
			stmt = conn.prepareStatement(SQL_GET_ACC_BY_ID);
			stmt.setLong(1, accountId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				acc = this.getAccount(rs);
			}
			LOGGER.info("After: get account by id");
			return acc;
		} catch (SQLException e) {
			throw new MoneyTransferException("getAccountById(): Error reading account data", e);
		} finally {
			DbUtils.closeQuietly(conn, stmt, rs);
		}

	}

	/**
	 * Create account
	 */
	public long createAccount(Account account) throws MoneyTransferException {
		LOGGER.info("Before: create account");
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet generatedKeys = null;
		try {
			conn = H2DAOFactoryImpl.getConnection();
			stmt = conn.prepareStatement(SQL_CREATE_ACC);
			stmt.setString(1, account.getUserName());
			stmt.setBigDecimal(2, account.getBalance());
			stmt.setString(3, account.getCurrencyCode());
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				LOGGER.error("createAccount(): Creating account failed, no rows affected.");
				throw new MoneyTransferException("Account Cannot be created");
			}
			generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
				LOGGER.info("After: create account");
				return generatedKeys.getLong(1);
			} else {
				LOGGER.error("Creating account failed, no ID obtained.");
				throw new MoneyTransferException("Account Cannot be created");
			}
		} catch (SQLException e) {
			LOGGER.error("Error Inserting Account  " + account);
			throw new MoneyTransferException("createAccount(): Error creating user account " + account, e);
		} finally {
			DbUtils.closeQuietly(conn, stmt, generatedKeys);
		}
	}

	/**
	 * Delete account by id
	 */
	public int deleteAccountById(long accountId) throws MoneyTransferException {
		LOGGER.info("Before: delete account");
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = H2DAOFactoryImpl.getConnection();
			stmt = conn.prepareStatement(SQL_DELETE_ACC_BY_ID);
			stmt.setLong(1, accountId);
			LOGGER.info("After: delete account");
			return stmt.executeUpdate();
		} catch (SQLException e) {
			throw new MoneyTransferException("deleteAccountById(): Error deleting user account Id " + accountId, e);
		} finally {
			DbUtils.closeQuietly(conn);
			DbUtils.closeQuietly(stmt);
		}
	}

	/**
	 * Update account balance
	 */
	public int updateAccountBalance(long accountId, BigDecimal deltaAmount) throws MoneyTransferException {
		LOGGER.info("Before: update balance");
		Connection conn = null;
		PreparedStatement lockStmt = null;
		PreparedStatement updateStmt = null;
		ResultSet rs = null;
		Account targetAccount = null;
		int updateCount = -1;
		try {
			conn = H2DAOFactoryImpl.getConnection();
			conn.setAutoCommit(false);
			
			synchronized (this) {
				// lock account for writing:
				lockStmt = conn.prepareStatement(SQL_LOCK_ACC_BY_ID);
				lockStmt.setLong(1, accountId);
				rs = lockStmt.executeQuery();
				if (rs.next()) {
					targetAccount = this.getAccount(rs);
				}

				if (targetAccount == null) {
					throw new MoneyTransferException("updateAccountBalance(): fail to lock account : " + accountId);
				}
				
				// update account upon success locking
				BigDecimal balance = targetAccount.getBalance().add(deltaAmount);
				if (balance.compareTo(MoneyTransferUtil.zeroAmount) < 0) {
					throw new MoneyTransferException("Not sufficient Fund for account: " + accountId);
				}

				updateStmt = conn.prepareStatement(SQL_UPDATE_ACC_BALANCE);
				updateStmt.setBigDecimal(1, balance);
				updateStmt.setLong(2, accountId);
				updateCount = updateStmt.executeUpdate();
				conn.commit();
				LOGGER.info("After: update balance");
			}
			return updateCount;
		} catch (SQLException se) {
			// rollback transaction if exception occurs
			LOGGER.error("updateAccountBalance(): User Transaction Failed, rollback initiated for: " + accountId, se);
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException re) {
				throw new MoneyTransferException("Fail to rollback transaction", re);
			}
		} finally {
			DbUtils.closeQuietly(conn);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(lockStmt);
			DbUtils.closeQuietly(updateStmt);
		}
		return updateCount;
	}

	/**
	 * Transfer balance between two accounts.
	 */
	@SuppressWarnings("resource")
	public int transferAccountBalance(Transaction transaction) throws MoneyTransferException {
		LOGGER.info("Before: transfer balance");
		int result = -1;
		Connection conn = null;
		PreparedStatement lockStmt = null;
		PreparedStatement updateStmt = null;
		ResultSet rs = null;
		Account fromAccount = null;
		Account toAccount = null;

		try {
			conn = H2DAOFactoryImpl.getConnection();
			conn.setAutoCommit(false);
			synchronized (this) {
				// lock the credit and debit account for writing:
				lockStmt = conn.prepareStatement(SQL_LOCK_ACC_BY_ID);
				lockStmt.setLong(1, transaction.getFromAccountId());
				rs = lockStmt.executeQuery();
				if (rs.next()) {
					fromAccount = this.getAccount(rs);
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("transferAccountBalance from Account: " + fromAccount);
				}
				lockStmt = conn.prepareStatement(SQL_LOCK_ACC_BY_ID);
				lockStmt.setLong(1, transaction.getToAccountId());
				rs = lockStmt.executeQuery();
				if (rs.next()) {
					toAccount = this.getAccount(rs);
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("transferAccountBalance to Account: " + toAccount);
				}

				// check locking status
				if (fromAccount == null || toAccount == null) {
					throw new MoneyTransferException("Fail to lock both accounts for write");
				}

				// check transaction currency
				if (!fromAccount.getCurrencyCode().equals(transaction.getCurrencyCode())) {
					throw new MoneyTransferException(
							"Fail to transfer Fund, transaction ccy are different from source/destination");
				}

				// check ccy is the same for both accounts
				if (!fromAccount.getCurrencyCode().equals(toAccount.getCurrencyCode())) {
					throw new MoneyTransferException(
							"Fail to transfer Fund, the source and destination account are in different currency");
				}

				// check enough fund in source account
				BigDecimal fromAccountLeftOver = fromAccount.getBalance().subtract(transaction.getAmount());
				if (fromAccountLeftOver.compareTo(MoneyTransferUtil.zeroAmount) < 0) {
					throw new MoneyTransferException("Not enough Fund from source Account ");
				}
				// proceed with update
				updateStmt = conn.prepareStatement(SQL_UPDATE_ACC_BALANCE);
				updateStmt.setBigDecimal(1, fromAccountLeftOver);
				updateStmt.setLong(2, transaction.getFromAccountId());
				updateStmt.addBatch();
				updateStmt.setBigDecimal(1, toAccount.getBalance().add(transaction.getAmount()));
				updateStmt.setLong(2, transaction.getToAccountId());
				updateStmt.addBatch();
				int[] rowsUpdated = updateStmt.executeBatch();
				result = rowsUpdated[0] + rowsUpdated[1];
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Number of rows updated for the transfer : " + result);
				}
				// If there is no error, commit the transaction
				conn.commit();
				LOGGER.info("After: transfer balance");
			}
		} catch (SQLException se) {
			// rollback transaction if exception occurs
			LOGGER.error(
					"transferAccountBalance(): User Transaction Failed, rollback initiated for: " + transaction,
					se);
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException re) {
				throw new MoneyTransferException("Fail to rollback transaction", re);
			}
		} finally {
			DbUtils.closeQuietly(conn);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(lockStmt);
			DbUtils.closeQuietly(updateStmt);
		}
		return result;
	}

	private Account getAccount(ResultSet rs) throws SQLException {
		return new Account(rs.getLong("AccountId"), rs.getString("UserName"), rs.getBigDecimal("Balance"),
				rs.getString("CurrencyCode"));
	}

}
