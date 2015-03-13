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

import net.fender.pool.BasePoolableObjectFactory;

/**
 * @author Eric Fenderbosch
 * @param <T>
 */
public class PoolableConnectionFactory<T extends Connection> extends BasePoolableObjectFactory<T> {

	private ConnectionFactory connectionFactory;

	/**
	 * @param connectionFactory
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	@SuppressWarnings("unchecked")
	public T makeObject() throws Exception {
		return (T) connectionFactory.getConnection();
	}
}
