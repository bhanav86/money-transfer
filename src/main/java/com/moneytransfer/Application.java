package com.moneytransfer;

import com.moneytransfer.dao.DAOFactory;
import com.moneytransfer.service.AccountService;
import com.moneytransfer.service.ServiceExceptionMapper;
import com.moneytransfer.service.TransactionService;
import com.moneytransfer.service.UserService;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Main Class
 */
public class Application {

	private static Logger LOGGER = Logger.getLogger(Application.class);

	public static void main(String[] args) throws Exception {
		// Initialize H2 database with demo data
		LOGGER.info("Initialize money transfer in memory database");
		DAOFactory h2DaoFactory = DAOFactory.getDAOFactory();
		h2DaoFactory.populateTestData();
		LOGGER.info("Initialisation of money transfer in memory database complete");
		// Host service on jetty
		startService();
	}

	private static void startService() throws Exception {
		LOGGER.info("Initialising Jetty service");
		Server server = new Server(8080);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
		servletHolder.setInitParameter("jersey.config.server.provider.classnames",
				UserService.class.getCanonicalName() + "," + AccountService.class.getCanonicalName() + ","
						+ ServiceExceptionMapper.class.getCanonicalName() + ","
						+ TransactionService.class.getCanonicalName());
		try {
			server.start();
			server.join();
		} finally {
			server.destroy();
		}
		LOGGER.info("Initialisation of Jetty server complete");
	}

}
