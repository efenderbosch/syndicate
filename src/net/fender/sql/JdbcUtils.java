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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Null-safe util to close various SQL objects, suppressing all Exceptions.
 * 
 * @author Eric Fenderbosch
 */
public class JdbcUtils {

	/**
	 * Private constructor for static util
	 */
	private JdbcUtils() {
		//
	}

	/**
	 * @param resultSet
	 */
	public static void close(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException ignore) {
				// ignore
			}
		}
	}

	/**
	 * @param resultSet
	 */
	public static void closeWithCascade(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException ignore) {
				// ignore
			}
			Statement statement = null;
			try {
				statement = resultSet.getStatement();
			} catch (SQLException ignore) {
				// ignore
			}
			closeWithCascade(statement);
		}

	}

	/**
	 * @param statement
	 */
	public static void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException ignore) {
				// ignore
			}
		}
	}

	/**
	 * @param statement
	 */
	public static void closeWithCascade(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException ignore) {
				// ignore
			}
			Connection connection = null;
			try {
				connection = statement.getConnection();
			} catch (SQLException ignore) {
				// ignore
			}
			close(connection);
		}
	}

	/**
	 * @param connection
	 */
	public static void close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException ignore) {
				// ignore
			}
		}
	}

	/**
	 * @param resultSet
	 * @param statement
	 */
	public static void close(ResultSet resultSet, Statement statement) {
		close(resultSet);
		close(statement);
	}

	/**
	 * @param statement
	 * @param connection
	 */
	public static void close(Statement statement, Connection connection) {
		close(statement);
		close(connection);
	}

	/**
	 * @param resultSet
	 * @param statement
	 * @param connection
	 */
	public static void close(ResultSet resultSet, Statement statement, Connection connection) {
		close(resultSet);
		close(statement);
		close(connection);
	}

}
