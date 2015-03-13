/* 
 * Copyright 2008 - 2009 Eric Fenderbosch
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.fender.sql;

import java.security.Key;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import net.fender.crypto.CryptoUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Supports encrypted user name and password to appease DBAs and SOX404
 * auditors. This can keep plain text user names and passwords for production
 * databases out of configuration files and source control. To use this feature,
 * set the "encryptedUser" and/or "encryptedPassword" properties instead of
 * "user" and "password". Then use CryptoUtil either as a POJO or statically
 * configured w/ System properties. Use KeyGenerator to create a key and encrypt
 * user names and passwords.
 * 
 * @author Eric Fenderbosch
 */
public class DriverConnectionFactory implements ConnectionFactory {

	private static final Log log = LogFactory.getLog(DriverConnectionFactory.class);

	private String driverClassName;
	private Driver driver;
	private String url;
	private Properties properties;
	private Boolean autoCommit;
	private ResultSetHoldability holdability;
	private Boolean readOnly;
	private TransactionIsolation transactionIsolation;
	private String catalog;
	private boolean testOnCreate = true;
	private CryptoUtil cryptoUtil;
	private Key key;
	private String systemPropertyKeyName;

	/**
	 * @param driverClassName
	 */
	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	/**
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @param properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * @param autoCommit
	 */
	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	/**
	 * @param holdability
	 */
	public void setHoldability(ResultSetHoldability holdability) {
		this.holdability = holdability;
	}

	/**
	 * @param readOnly
	 * @throws SQLException
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		this.readOnly = readOnly;
	}

	/**
	 * @param level
	 */
	public void setTransactionIsolation(TransactionIsolation level) {
		transactionIsolation = level;
	}

	/**
	 * @param catalog
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	/**
	 * Create and release a connection when this factory is created? Default is
	 * true.
	 * 
	 * @param testOnCreate
	 */
	public void setTestOnCreate(boolean testOnCreate) {
		this.testOnCreate = testOnCreate;
	}

	/**
	 * Use (setCryptoUtil and setKey) or setSystemPropertyKeyName(). See
	 * CryptoUtil
	 * 
	 * @param cryptoUtil
	 */
	public void setCryptoUtil(CryptoUtil cryptoUtil) {
		this.cryptoUtil = cryptoUtil;
	}

	/**
	 * Use (setCryptoUtil and setKey) or setSystemPropertyKeyName(). See
	 * CryptoUtil
	 * 
	 * @param cryptoUtil
	 */
	public void setKey(Key key) {
		this.key = key;
	}

	/**
	 * Use (setCryptoUtil and setKey) or setSystemPropertyKeyName(). See
	 * CryptoUtil
	 * 
	 * @param cryptoUtil
	 */
	public void setSystemPropertyKeyName(String systemPropertyKeyName) {
		this.systemPropertyKeyName = systemPropertyKeyName;
	}

	/**
	 * @throws Exception
	 */
	public void init() throws Exception {
		try {
			driver = DriverManager.getDriver(url);
		} catch (SQLException e) {
			// no suitable driver
		}
		if (driver == null) {
			driver = (Driver) Class.forName(driverClassName).newInstance();
			log.debug(driver);
			DriverManager.registerDriver(driver);
		}
		String encryptedUser = properties.getProperty("encryptedUser");
		if (encryptedUser != null) {
			String user = null;
			if (cryptoUtil == null) {
				user = CryptoUtil.decryptUsingSystemPropertyKey(systemPropertyKeyName, encryptedUser);
			} else {
				user = cryptoUtil.decrypt(key, encryptedUser);
			}
			properties.setProperty("user", user);
			properties.remove("encryptedUser");
		}
		String encryptedPassword = properties.getProperty("encryptedPassword");
		if (encryptedPassword != null) {
			String password = null;
			if (cryptoUtil == null) {
				password = CryptoUtil.decryptUsingSystemPropertyKey(systemPropertyKeyName, encryptedPassword);
			} else {
				password = cryptoUtil.decrypt(key, encryptedPassword);
			}
			properties.setProperty("password", password);
			properties.remove("encryptedPassword");
		}
		if (testOnCreate) {
			Connection connection = getConnection();
			JdbcUtils.close(connection);
		}
	}

	public Connection getConnection() throws SQLException {
		// don't log properties as it contains plain text user name and password
		log.debug("creating connection to " + driver + " " + url);
		Connection connection = driver.connect(url, properties);
		if (autoCommit != null) {
			connection.setAutoCommit(autoCommit);
		}
		if (holdability != null) {
			connection.setHoldability(holdability.getHoldability());
		}
		if (readOnly != null) {
			connection.setReadOnly(readOnly);
		}
		if (transactionIsolation != null) {
			connection.setTransactionIsolation(transactionIsolation.getLevel());
		}
		if (catalog != null) {
			connection.setCatalog(catalog);
		}
		return connection;
	}
}
