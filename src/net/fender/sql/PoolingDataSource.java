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

import net.fender.pool.ObjectPool;
import net.fender.pool.PoolExhaustedException;

/**
 * TODO Implement ability to grow to a max size
 * 
 * @author Eric Fenderbosch
 */
public class PoolingDataSource extends ManagedConnectionDataSource {

	private ObjectPool<Connection> connectionPool;
	private int timesToRetry;

	/**
	 * @param connectionPool
	 */
	public void setConnectionPool(ObjectPool<Connection> connectionPool) {
		this.connectionPool = connectionPool;
		timesToRetry = connectionPool.getSize();
	}

	@Override
	protected void close(ManagedConnection managedConnection) throws SQLException {
		connectionPool.returnObject(managedConnection.getConnection());
	}

	@Override
	protected ManagedConnection getManagedConnection() throws SQLException {
		ManagedConnection managedConnection = null;
		SQLException exceptionToThrow = null;
		int tries = 0;
		while (managedConnection == null && tries <= timesToRetry) {
			Connection connection = null;
			try {
				connection = connectionPool.borrowObject();
				managedConnection = new ManagedConnection(connection, this);
				if (validateConnectionOnAquire) {
					validateConnection(managedConnection);
				}
			} catch (PoolExhaustedException e) {
				throw new SQLException(e);
			} catch (SQLException e) {
				connectionPool.invalidateObject(connection);
				if (exceptionToThrow == null) {
					exceptionToThrow = e;
				} else {
					exceptionToThrow.setNextException(e);
				}
			} catch (Exception e) {
				connectionPool.invalidateObject(connection);
				if (exceptionToThrow == null) {
					exceptionToThrow = new SQLException(e);
				} else {
					SQLException nested = new SQLException(e);
					exceptionToThrow.setNextException(nested);
				}
			}
		}
		if (managedConnection != null) {
			return managedConnection;
		}
		throw exceptionToThrow;
	}
}


