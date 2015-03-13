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
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Eric Fenderbosch
 */
public abstract class LoadBalancingDataSource extends ManagedConnectionDataSource {

	private static final Log log = LogFactory.getLog(LoadBalancingDataSource.class);

	protected int timesToRetry = 0;

	/**
	 * @param dataSources
	 */
	public abstract void setDataSources(Collection<DataSource> dataSources);

	@Override
	protected ManagedConnection getManagedConnection() throws SQLException {
		ManagedConnection managedConnection = null;
		SQLException exceptionToThrow = new SQLException();
		int tries = 0;
		while (managedConnection == null && tries <= timesToRetry) {
			DataSource dataSource = getNextDataSource();
			try {
				Connection connection = dataSource.getConnection();
				managedConnection = new ManagedConnection(connection, this);
				if (validateConnectionOnAquire) {
					validateConnection(managedConnection);
				}
			} catch (SQLException e) {
				log.warn("connection failure " + e.getMessage());
				exceptionToThrow.setNextException(e);
				tries++;
			}
		}
		if (managedConnection == null) {
			throw exceptionToThrow;
		}
		return managedConnection;
	}

	@Override
	protected void close(ManagedConnection managedConnection) throws SQLException {
		JdbcUtils.close(managedConnection.getConnection());
	}

	/**
	 * sub-classes may implement round robin, FIFO, LIFO, etc.
	 * 
	 * @return
	 */
	protected abstract DataSource getNextDataSource();

	/**
	 * @param timesToRetry
	 */
	protected void setTimesToRetry(int timesToRetry) {
		this.timesToRetry = timesToRetry;
	}

}
