package net.fender.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import net.fender.crypto.CryptoUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Eric Fenderbosch
 */
public class Test {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		CryptoUtil.setSystemPropertyAlgorithm("myKey", "TripleDES");
		CryptoUtil.setSystemPropertyKey("myKey", "kNLvsG7CsgkOgGg2FGvdApY97zYUHYUR");

		ApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
		int threads = (Integer) context.getBean("testThreads");
		int iterations = (Integer) context.getBean("testIterations");

		// Hashtable env = new Hashtable();
		// env.put(Context.INITIAL_CONTEXT_FACTORY,
		// "com.caucho.naming.InitialContextFactoryImpl");
		// env.put(Context.PROVIDER_URL, "tcp://127.0.0.1:8080");
		// Context jndi = new InitialContext(env);
		// DataSource ds = (DataSource)
		// jndi.lookup("roundRobinLoadBalancingDataSource");
		// Runner r = new Runner(ds, iterations);
		// System.out.println("starting jndi data source " + ds);
		// long s = System.currentTimeMillis();
		// r.run();
		// System.out.println("jndi data source total " +
		// (System.currentTimeMillis() - s));

		Map<String, DataSource> dataSources = context.getBeansOfType(DataSource.class);
		for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
			String name = entry.getKey();
			DataSource dataSource = entry.getValue();
			if (dataSource.getClass().getName().equals("net.fender.sql.DriverDataSource")) {
				// no pooling... VERY SLOW, just skip it
				continue;
			}
			System.out.println("starting " + name + " " + dataSource);
			Thread[] runners = new Thread[threads];
			Runner runner = new Runner(dataSource, iterations);
			for (int i = 0; i < threads; i++) {
				runners[i] = new Thread(runner);
			}
			long start = System.currentTimeMillis();
			for (int i = 0; i < threads; i++) {
				runners[i].start();
			}
			for (int i = 0; i < threads; i++) {
				runners[i].join();
			}
			System.out.println(name + " total " + (System.currentTimeMillis() - start));
		}
	}

	private static class Runner implements Runnable {
		private static final Log log = LogFactory.getLog(Runner.class);
		private DataSource dataSource;
		private int iterations;

		public Runner(DataSource dataSource, int iterations) {
			this.dataSource = dataSource;
			this.iterations = iterations;
		}

		public void run() {
			for (int i = 0; i < iterations; i++) {
				Connection connection = null;
				Statement statement = null;
				ResultSet resultSet = null;
				try {
					connection = dataSource.getConnection();
					statement = connection.createStatement();
					resultSet = statement.executeQuery("select 2 from dual");
					if (resultSet.next()) {
						int result = resultSet.getInt(1);
						log.debug("result[" + i + "] = " + result);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					JdbcUtils.close(resultSet, statement, connection);
				}
			}
		}
	}
}
