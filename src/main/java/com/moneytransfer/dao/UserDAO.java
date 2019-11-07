package com.moneytransfer.dao;

import com.moneytransfer.exception.MoneyTransferException;
import com.moneytransfer.model.User;

import java.util.List;

/**
 * User DAO
 */
public interface UserDAO {

	/**
	 * Get all users
	 * 
	 * @return users
	 * @throws MoneyTransferException
	 */
	List<User> getAllUsers() throws MoneyTransferException;

	/**
	 * Get user by user id
	 * 
	 * @param userId
	 * @return user
	 * @throws MoneyTransferException
	 */
	User getUserById(long userId) throws MoneyTransferException;

	/**
	 * Get user by name
	 * 
	 * @param userName
	 * @return user
	 * @throws MoneyTransferException
	 */
	User getUserByName(String userName) throws MoneyTransferException;

	/**
	 * Create user
	 * 
	 * @param user
	 * @return id
	 * @throws MoneyTransferException
	 */
	long createUser(User user) throws MoneyTransferException;

	/**
	 * Update
	 * 
	 * @param userId
	 * @param user
	 * @return number of users
	 * @throws MoneyTransferException
	 */
	int updateUser(Long userId, User user) throws MoneyTransferException;

	/**
	 * Delete user
	 * 
	 * @param userId
	 * @return number of users
	 * @throws MoneyTransferException
	 */
	int deleteUser(long userId) throws MoneyTransferException;

}
