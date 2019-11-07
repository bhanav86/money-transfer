package com.moneytransfer.service;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.moneytransfer.dao.DAOFactory;
import com.moneytransfer.exception.MoneyTransferException;
import com.moneytransfer.model.Transaction;
import com.moneytransfer.utils.MoneyTransferUtil;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionService {

	private final DAOFactory daoFactory = DAOFactory.getDAOFactory();
	
	/**
	 * Transfer fund between two accounts.
	 * @param transaction
	 * @return response
	 * @throws MoneyTransferException
	 */
	@POST
	public Response transferFund(Transaction transaction) throws MoneyTransferException {

		String currency = transaction.getCurrencyCode();
		if (MoneyTransferUtil.INSTANCE.validateCcyCode(currency)) {
			int updateCount = daoFactory.getAccountDAO().transferAccountBalance(transaction);
			if (updateCount == 2) {
				return Response.status(Response.Status.OK).build();
			} else {
				// transaction failed
				throw new WebApplicationException("Transaction failed", Response.Status.BAD_REQUEST);
			}

		} else {
			throw new WebApplicationException("Currency Code Invalid ", Response.Status.BAD_REQUEST);
		}

	}

}
