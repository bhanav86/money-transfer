package com.moneytransfer.dao.impl;

import com.moneytransfer.dao.UserDAO;
import com.moneytransfer.exception.MoneyTransferException;
import com.moneytransfer.model.User;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDAOImpl implements UserDAO {
	
    private static Logger LOGGER = Logger.getLogger(UserDAOImpl.class);
    private final static String SQL_GET_USER_BY_ID = "SELECT * FROM User WHERE UserId = ? ";
    private final static String SQL_GET_ALL_USERS = "SELECT * FROM User";
    private final static String SQL_GET_USER_BY_NAME = "SELECT * FROM User WHERE UserName = ? ";
    private final static String SQL_INSERT_USER = "INSERT INTO User (UserName, EmailAddress) VALUES (?, ?)";
    private final static String SQL_UPDATE_USER = "UPDATE User SET UserName = ?, EmailAddress = ? WHERE UserId = ? ";
    private final static String SQL_DELETE_USER_BY_ID = "DELETE FROM User WHERE UserId = ? ";
    
    /**
     * Find all users
     */
    public List<User> getAllUsers() throws MoneyTransferException {
    	LOGGER.info("Before: get all users");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<User>();
        try {
            conn = H2DAOFactoryImpl.getConnection();
            stmt = conn.prepareStatement(SQL_GET_ALL_USERS);
            rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(this.getUser(rs));
            }
            LOGGER.info("After: get all users");
            return users;
        } catch (SQLException e) {
            throw new MoneyTransferException("Error reading user data", e);
        } finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }
    }
    
    /**
     * Find user by userId
     */
    public User getUserById(long userId) throws MoneyTransferException {
    	LOGGER.info("Before: get user by id");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User user = null;
        try {
            conn = H2DAOFactoryImpl.getConnection();
            stmt = conn.prepareStatement(SQL_GET_USER_BY_ID);
            stmt.setLong(1, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                user = this.getUser(rs);
            }
            LOGGER.info("After: get user by id");
            return user;
        } catch (SQLException e) {
            throw new MoneyTransferException("Error reading user data", e);
        } finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }
    }
    
    /**
     * Find user by userName
     */
    public User getUserByName(String userName) throws MoneyTransferException {
    	LOGGER.info("Before: get user by name");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User user = null;
        try {
            conn = H2DAOFactoryImpl.getConnection();
            stmt = conn.prepareStatement(SQL_GET_USER_BY_NAME);
            stmt.setString(1, userName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                user = this.getUser(rs);
            }
            LOGGER.info("After: get user by name");
            return user;
        } catch (SQLException e) {
            throw new MoneyTransferException("Error reading user data", e);
        } finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }
    }
    
    /**
     * Save User
     */
    public long createUser(User user) throws MoneyTransferException {
    	LOGGER.info("Before: create user");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = H2DAOFactoryImpl.getConnection();
            stmt = conn.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getUserName());
            stmt.setString(2, user.getEmailAddress());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.error("insertUser(): Creating user failed, no rows affected." + user);
                throw new MoneyTransferException("Users Cannot be created");
            }
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
            	LOGGER.info("After: create user");
                return generatedKeys.getLong(1);
            } else {
                LOGGER.error("insertUser():  Creating user failed, no ID obtained." + user);
                throw new MoneyTransferException("Users Cannot be created");
            }
        } catch (SQLException e) {
            LOGGER.error("Error Inserting User :" + user);
            throw new MoneyTransferException("Error creating user data", e);
        } finally {
            DbUtils.closeQuietly(conn,stmt,generatedKeys);
        }

    }
    
    /**
     * Update User
     */
    public int updateUser(Long userId, User user) throws MoneyTransferException {
    	LOGGER.info("Before: udpate user");
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = H2DAOFactoryImpl.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_USER);
            stmt.setString(1, user.getUserName());
            stmt.setString(2, user.getEmailAddress());
            stmt.setLong(3, userId);
            LOGGER.info("After: udpate user");
            return stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error Updating User :" + user);
            throw new MoneyTransferException("Error update user data", e);
        } finally {
            DbUtils.closeQuietly(conn);
            DbUtils.closeQuietly(stmt);
        }
    }
    
    /**
     * Delete User
     */
    public int deleteUser(long userId) throws MoneyTransferException {
    	LOGGER.info("Before: delete user");
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = H2DAOFactoryImpl.getConnection();
            stmt = conn.prepareStatement(SQL_DELETE_USER_BY_ID);
            stmt.setLong(1, userId);
            LOGGER.info("After: delete user");
            return stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error Deleting User :" + userId);
            throw new MoneyTransferException("Error Deleting User ID:"+ userId, e);
        } finally {
            DbUtils.closeQuietly(conn);
            DbUtils.closeQuietly(stmt);
        }
    }
    
    private User getUser(ResultSet rs) throws SQLException {
    	return new User(rs.getLong("UserId"), rs.getString("UserName"), rs.getString("EmailAddress"));
    }

}
