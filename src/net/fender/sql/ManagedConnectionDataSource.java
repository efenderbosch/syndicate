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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Eric Fenderbosch
 */
public abstract class ManagedConnectionDataSource implements DataSource {

	private static final Log log = LogFactory.getLog(ManagedConnectionDataSource.class);

	protected boolean validateConnectionOnAquire = false;
	protected String validationQuery;
	private String jndiName;
	private Properties jndiProperties;

	/**
	 * default is false
	 * 
	 * @param validateConnectionOnAquire
	 */
	public void setValidateConnectionOnAquire(boolean validateConnectionOnAquire) {
		this.validateConnectionOnAquire = validateConnectionOnAquire;
	}

	/**
	 * If no validationQuery is set, then the validation check returns
	 * !isClosed(). This query should be fast and cheap and return at least one
	 * (ideally only one) row, something like "select 1 from dual" for Oracle or
	 * MySQL. If this DataSource delegates to other
	 * non-ManagedConnectionDataSources (like from a JEE container), they should
	 * have validation turned off to avoid multiple execution of validation
	 * queries.
	 * 
	 * @param validationQuery
	 */
	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	/**
	 * If jndiName is set, this DataSource will attempt to bind itself in a JNDI
	 * Context. Tested w/ Resin 3.1.
	 * 
	 * @param jndiName
	 */
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	/**
	 * This is exposed as a Map as well as Properties because Spring doesn't
	 * allow "ref" in a prop which some users may find restrictive.
	 * 
	 * @param jndiProperties
	 */
	public void setJndiProperties(Map<String, String> jndiProperties) {
		this.jndiProperties = new Properties();
		for (Map.Entry<String, String> entry : jndiProperties.entrySet()) {
			this.jndiProperties.setProperty(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @param jndiProperties
	 */
	public void setJndiProperties(Properties jndiProperties) {
		this.jndiProperties = jndiProperties;
	}

	/**
	 * Sub-classes that implement init() should make sure to call super.init()
	 * to ensure JNDI publication.
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		if (jndiName != null) {
			Context context = null;
			try {
				if (jndiProperties == null) {
					context = new InitialContext();
				} else {
					context = new InitialContext(jndiProperties);
				}
				context.rebind(jndiName, this);
			} finally {
				if (context != null) {
					try {
						context.close();
					} catch (NamingException ignore) {
						// ignore
					}
				}
			}
		}
	}

	public final Connection getConnection() throws SQLException {
		ManagedConnection managedConnection = getManagedConnection();
		if (validateConnectionOnAquire) {
			validateConnection(managedConnection);
		}
		return managedConnection;
	}

	/**
	 * throws UnsupportedOperationException
	 */
	public final Connection getConnection(String username, String password) throws SQLException {
		// return getManagedConnection(username, password);
		throw new UnsupportedOperationException();
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	protected abstract ManagedConnection getManagedConnection() throws SQLException;

	/**
	 * @param managedConnection
	 * @throws SQLException
	 */
	protected abstract void close(ManagedConnection managedConnection) throws SQLException;

	/**
	 * Tests if managedConnection != null, wrappedConnection != null &&
	 * !isClosed(), then runs validation query. Closes the connection on
	 * SQLException.
	 * 
	 * @param managedConnection
	 * @throws InvalidConnectionException
	 */
	protected void validateConnection(ManagedConnection managedConnection) throws InvalidConnectionException {
		if (managedConnection == null) {
			throw new InvalidConnectionException("managedConnection is null");
		}
		// only validate the base connection
		if (managedConnection.isValid()) {
			return;
		}
		managedConnection.setValid(false);
		Connection connection = managedConnection.getConnection();
		if (connection == null) {
			throw new InvalidConnectionException("wrapped connection is null");
		}
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (connection.isClosed()) {
				throw new InvalidConnectionException("wrapped connection is closed");
			}
			// if no validation query, then assume it is ok
			if (StringUtils.isBlank(validationQuery)) {
				managedConnection.setValid(true);
				return;
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(validationQuery);
			if (resultSet.next()) {
				log.debug("connection validation returned " + resultSet.getObject(1));
				managedConnection.setValid(true);
			}
		} catch (SQLException e) {
			JdbcUtils.close(connection);
			throw new InvalidConnectionException(e);
		} finally {
			JdbcUtils.close(resultSet, statement);
		}
	}

	/**
	 * throws UnsupportedOperationException
	 */
	public PrintWriter getLogWriter() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * throws UnsupportedOperationException
	 */
	public int getLoginTimeout() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * throws UnsupportedOperationException
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * throws UnsupportedOperationException
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
		// throw new UnsupportedOperationException();
	}

	/**
	 * throws UnsupportedOperationException
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// return null;
		throw new UnsupportedOperationException();
	}

}
