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

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simply calls driver.connect(url, properties), returning a new connection
 * every time. close() physically closes the managed connection. There is no
 * pool and thus will be very slow for repeated use. SingleConnectionDataSource
 * is a simple to configure DataSource that provides very primitive pooling.
 * TODO implement backup URL.
 * 
 * @author Eric Fenderbosch
 */
public class DriverDataSource extends ManagedConnectionDataSource {

	private static final Log log = LogFactory.getLog(DriverDataSource.class);

	protected ConnectionFactory connectionFactory;

	/**
	 * @param connectionFactory
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	@Override
	protected ManagedConnection getManagedConnection() throws SQLException {
		Connection connection = connectionFactory.getConnection();
		ManagedConnection managedConnection = new ManagedConnection(connection, this);
		validateConnection(managedConnection);
		return managedConnection;
	}

	@Override
	protected void close(ManagedConnection managedConnection) throws SQLException {
		if (log.isDebugEnabled()) {
			// TODO implement nice toString on managedConnection
			log.debug("closing connection " + managedConnection);
		}
		JdbcUtils.close(managedConnection.getConnection());
	}
}
