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

import java.sql.SQLException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This DataSource recycles a single connection. Basically this is a special
 * pool of size one, just simpler to configure than a PoolingDataSource &
 * ObjectPool. If the connection is in use, then the request will block for a
 * configurable time and then fail on time out.
 * 
 * @author Eric Fenderbosch
 */
public class SingleConnectionDataSource extends DriverDataSource {

	private static final Log log = LogFactory.getLog(SingleConnectionDataSource.class);

	private Semaphore connectionSemaphore = new Semaphore(1, true);
	private ManagedConnection managedConnection = null;
	private long lockTimeout = -1;
	private TimeUnit lockTimeUnit = TimeUnit.MILLISECONDS;

	/**
	 * Optional length of time to wait on borrowObject. <= 0 means block
	 * 
	 * @param lockTimeout
	 */
	public void setLockTimeout(long lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	/**
	 * Optional unit of time to wait on borrowObject default is MILLISECONDS.
	 * 
	 * @param lockTimeUnit
	 */
	public void setTimeUnit(TimeUnit lockTimeUnit) {
		this.lockTimeUnit = lockTimeUnit;
	}

	@Override
	public void init() throws Exception {
		super.init();
		managedConnection = super.getManagedConnection();
	}

	@Override
	protected ManagedConnection getManagedConnection() throws SQLException {
		boolean aquired = false;
		try {
			aquired = connectionSemaphore.tryAcquire(lockTimeout, lockTimeUnit);
		} catch (InterruptedException e) {
			//
		}
		if (aquired) {
			try {
				if (validateConnectionOnAquire) {
					try {
						validateConnection(managedConnection);
					} catch (InvalidConnectionException e) {
						// maybe this old connection timed out, retry one time
						managedConnection = super.getManagedConnection();
					}
				}
				return managedConnection;
			} catch (SQLException e) {
				close(managedConnection);
				throw e;
			}
		} else {
			throw new SQLException("Timeout (" + lockTimeout + " " + lockTimeUnit
					+ ") waiting for available connection.");
		}
	}

	/**
	 * Just return it to our pool of one.
	 */
	@Override
	protected void close(ManagedConnection closedConnection) throws SQLException {
		if (log.isDebugEnabled()) {
			log.debug("releasing connection " + closedConnection);
		}
		if (managedConnection == closedConnection) {
			connectionSemaphore.release();
		}
	}
}
