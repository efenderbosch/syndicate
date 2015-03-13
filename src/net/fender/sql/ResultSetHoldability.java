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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple enum wrapper around ResultSet's HOLD_CURSORS_OVER_COMMIT and
 * CLOSE_CURSORS_AT_COMMIT.
 * 
 * @author Eric Fenderbosch
 */
public enum ResultSetHoldability {

	HOLD_CURSORS_OVER_COMMIT(ResultSet.HOLD_CURSORS_OVER_COMMIT),
	CLOSE_CURSORS_AT_COMMIT(ResultSet.CLOSE_CURSORS_AT_COMMIT);

	private int holdability;
	private static final Map<Integer, ResultSetHoldability> map = new HashMap<Integer, ResultSetHoldability>();
	static {
		for (ResultSetHoldability resultSetHoldability : values()) {
			map.put(resultSetHoldability.holdability, resultSetHoldability);
		}
	}

	/**
	 * @param holdability
	 */
	private ResultSetHoldability(int holdability) {
		this.holdability = holdability;
	}

	/**
	 * @return
	 */
	public int getHoldability() {
		return holdability;
	}

	/**
	 * @param holdability
	 * @return
	 */
	public static ResultSetHoldability getForHoldability(int holdability) {
		return map.get(holdability);
	}

}
