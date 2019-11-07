package com.moneytransfer.service;

import com.moneytransfer.dao.DAOFactory;
import com.moneytransfer.exception.MoneyTransferException;
import com.moneytransfer.model.User;

import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserService {

	private final DAOFactory daoFactory = DAOFactory.getDAOFactory();

	private static Logger LOGGER = Logger.getLogger(UserService.class);

	/**
	 * Find by userName
	 * 
	 * @param userName
	 * @return user
	 * @throws MoneyTransferException
	 */
	@GET
	@Path("/{userName}")
	public User getUserByName(@PathParam("userName") String userName) throws MoneyTransferException {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Request Received for get User by Name " + userName);
		final User user = daoFactory.getUserDAO().getUserByName(userName);
		if (user == null) {
			throw new WebApplicationException("User Not Found", Response.Status.NOT_FOUND);
		}
		return user;
	}

	/**
	 * Find by all
	 * 
	 * @param userName
	 * @return users
	 * @throws MoneyTransferException
	 */
	@GET
	@Path("/all")
	public List<User> getAllUsers() throws MoneyTransferException {
		return daoFactory.getUserDAO().getAllUsers();
	}

	/**
	 * Create User
	 * 
	 * @param user
	 * @return user
	 * @throws MoneyTransferException
	 */
	@POST
	@Path("/create")
	public User createUser(User user) throws MoneyTransferException {
		if (daoFactory.getUserDAO().getUserByName(user.getUserName()) != null) {
			throw new WebApplicationException("User name already exist", Response.Status.BAD_REQUEST);
		}
		final long uId = daoFactory.getUserDAO().createUser(user);
		return daoFactory.getUserDAO().getUserById(uId);
	}

	/**
	 * Find by User Id
	 * 
	 * @param userId
	 * @param user
	 * @return response
	 * @throws MoneyTransferException
	 */
	@PUT
	@Path("/{userId}")
	public Response updateUser(@PathParam("userId") long userId, User user) throws MoneyTransferException {
		final int updateCount = daoFactory.getUserDAO().updateUser(userId, user);
		if (updateCount == 1) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	/**
	 * Delete by User Id
	 * 
	 * @param userId
	 * @return response
	 * @throws MoneyTransferException
	 */
	@DELETE
	@Path("/{userId}")
	public Response deleteUser(@PathParam("userId") long userId) throws MoneyTransferException {
		int deleteCount = daoFactory.getUserDAO().deleteUser(userId);
		if (deleteCount == 1) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}
}
