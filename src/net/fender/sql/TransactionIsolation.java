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
import java.util.HashMap;
import java.util.Map;

/**
 * Simple enum wrapper around Connection.TRANSACTION_*.
 * 
 * @author Eric Fenderbosch
 */
public enum TransactionIsolation {

	NONE(Connection.TRANSACTION_NONE),
	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

	private int level;
	private static final Map<Integer, TransactionIsolation> map = new HashMap<Integer, TransactionIsolation>();
	static {
		for (TransactionIsolation transactionIsolation : values()) {
			map.put(transactionIsolation.level, transactionIsolation);
		}
	}

	/**
	 * @param level
	 */
	private TransactionIsolation(int level) {
		this.level = level;
	}

	/**
	 * @return
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level
	 * @return
	 */
	public static TransactionIsolation getForLevel(int level) {
		return map.get(level);
	}
}
