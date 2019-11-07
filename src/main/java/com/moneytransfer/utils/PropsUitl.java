package com.moneytransfer.utils;

import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropsUitl {

	private static Properties properties = new Properties();

	static Logger LOGGER = Logger.getLogger(PropsUitl.class);

	public static void loadConfig(String fileName) {
		if (fileName == null) {
			LOGGER.warn("loadConfig: config file name cannot be null");
		} else {
			try {
				LOGGER.info("loadConfig(): Loading config file: " + fileName);
				final InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
				properties.load(fis);

			} catch (FileNotFoundException fne) {
				LOGGER.error("loadConfig(): file name not found " + fileName, fne);
			} catch (IOException ioe) {
				LOGGER.error("loadConfig(): error when reading the config " + fileName, ioe);
			}
		}

	}

	public static String getStringProperty(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			value = System.getProperty(key);
		}
		return value;
	}

	/**
	 * @param            key: property key
	 * @param defaultVal the default value if the key not present in config file
	 * @return string property based on lookup key
	 */
	public static String getStringProperty(String key, String defaultVal) {
		String value = getStringProperty(key);
		return value != null ? value : defaultVal;
	}

	public static int getIntegerProperty(String key, int defaultVal) {
		String valueStr = getStringProperty(key);
		if (valueStr == null) {
			return defaultVal;
		} else {
			try {
				return Integer.parseInt(valueStr);

			} catch (Exception e) {
				LOGGER.warn("getIntegerProperty(): cannot parse integer from properties file for: " + key
						+ "fail over to default value: " + defaultVal, e);
				return defaultVal;
			}
		}
	}

	static {
		String configFileName = System.getProperty("application.properties");

		if (configFileName == null) {
			configFileName = "application.properties";
		}
		loadConfig(configFileName);

	}

}
