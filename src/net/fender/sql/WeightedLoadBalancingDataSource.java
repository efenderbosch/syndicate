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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Uses relative weighting to randomly select a DataSource. Use the smallest
 * integers possible to achieve the correct weighting. Don't use "200, 300, 500"
 * as it will produce the same distribution as "2, 3, 5" but allocate more
 * memory and be less efficient. For "2, 3, 5" the 1st DataSource will be
 * selected ~20% (2 / 2 + 3 + 5) of the time, the 2nd DataSource will be
 * selected ~30% (3 / 2 + 3 + 5) of the time and the 3rd DataSource will be
 * selected ~50% (5 / 2 + 3 + 5) of the time. If all DataSources should have the
 * same weighting, use a RoundRobinLoadBalancingDataSource instead.
 * 
 * @author Eric Fenderbosch
 */
public class WeightedLoadBalancingDataSource extends LoadBalancingDataSource {

	private static final Log log = LogFactory.getLog(WeightedLoadBalancingDataSource.class);

	private final Random random = new Random();
	private Collection<DataSource> dataSources;
	private String weights;
	private DataSource[] buckets;
	private int size;

	@Override
	protected DataSource getNextDataSource() {
		int bucket = random.nextInt(size);
		DataSource dataSource = buckets[bucket];
		log.debug(bucket + " " + dataSource);
		return dataSource;
	}

	@Override
	public void setDataSources(Collection<DataSource> dataSources) {
		this.dataSources = dataSources;
		size = dataSources.size();
	}

	@Override
	public void setTimesToRetry(int timesToRetry) {
		this.timesToRetry = timesToRetry;
	}

	/**
	 * A comma separated list of integer weights in the same order as
	 * dataSources.
	 * 
	 * @param weights
	 */
	public void setWeights(String weights) {
		this.weights = weights;
	}

	@Override
	public void init() throws Exception {
		super.init();
		if (dataSources == null || weights == null) {
			throw new IllegalStateException("dataSources and weights must not be null");
		}
		StringTokenizer parser = new StringTokenizer(weights, ",");
		if (dataSources.size() != parser.countTokens()) {
			throw new IllegalStateException("dataSources and weights must be the same size");
		}
		List<DataSource> temp = new ArrayList<DataSource>();
		int bucket = 0;
		Iterator<DataSource> d = dataSources.iterator();
		while (parser.hasMoreTokens()) {
			int weight = Integer.parseInt(parser.nextToken().trim());
			DataSource dataSource = d.next();
			for (int j = bucket; j < bucket + weight; j++) {
				temp.add(dataSource);
			}
			bucket += weight;
		}
		size = temp.size();
		buckets = temp.toArray(new DataSource[size]);
	}
}
