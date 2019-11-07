package com.moneytransfer.dao;

import com.moneytransfer.exception.MoneyTransferException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * Account DAO
 */
public interface AccountDAO {

	/**
	 * Get all accounts
	 * 
	 * @return all accounts
	 * @throws MoneyTransferException
	 */
	List<Account> getAllAccounts() throws MoneyTransferException;

	/**
	 * Get account by account id.
	 * 
	 * @param accountId
	 * @return account associated to that account id
	 * @throws MoneyTransferException
	 */
	Account getAccountById(long accountId) throws MoneyTransferException;

	/**
	 * Create account
	 * 
	 * @param account
	 * @return account id
	 * @throws MoneyTransferException
	 */
	long createAccount(Account account) throws MoneyTransferException;

	/**
	 * Delete account
	 * 
	 * @param accountId
	 * @return account id
	 * @throws MoneyTransferException
	 */
	int deleteAccountById(long accountId) throws MoneyTransferException;

	/**
	 * Update account balance
	 * 
	 * @param accountId
	 * @param deltaAmount amount
	 * @return number of rows updated
	 * @throws MoneyTransferException
	 */
	int updateAccountBalance(long accountId, BigDecimal deltaAmount) throws MoneyTransferException;

	/**
	 * Transfer amount between accounts
	 * 
	 * @param userTransaction
	 * @return number of rows updated
	 * @throws MoneyTransferException
	 */
	int transferAccountBalance(Transaction userTransaction) throws MoneyTransferException;
}
