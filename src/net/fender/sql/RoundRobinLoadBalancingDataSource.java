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

import java.util.Collection;

import javax.sql.DataSource;

import net.fender.collections.RoundRobinLoopingIterator;

/**
 * @author Eric Fenderbosch
 */
public class RoundRobinLoadBalancingDataSource extends LoadBalancingDataSource {

	private RoundRobinLoopingIterator<DataSource> dataSources;

	@Override
	public void setDataSources(Collection<DataSource> dataSources) {
		this.dataSources = new RoundRobinLoopingIterator<DataSource>(dataSources);
		setTimesToRetry(dataSources.size());
	}

	@Override
	protected DataSource getNextDataSource() {
		return dataSources.next();
	}

}
