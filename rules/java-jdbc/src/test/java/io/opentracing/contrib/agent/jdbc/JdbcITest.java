package io.opentracing.contrib.agent.jdbc;

/**
 * Created by jam01 on 9/13/18.
 */

import io.opentracing.Scope;
import io.opentracing.contrib.agent.common.OTAgentTestBase;
import io.opentracing.mock.MockSpan;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author malafeev
 * @author Jose Montoya
 */
public class JdbcITest extends OTAgentTestBase {

	@Test
	public void test() throws Exception {
		try (Scope ignored = getTracer().buildSpan("jdbc-test").startActive(true)) {

			Connection connection = DriverManager.getConnection("jdbc:h2:mem:jdbc");
			Statement statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE employer (id INTEGER)");
			connection.close();

			List<MockSpan> spans = getTracer().finishedSpans();
			assertEquals(1, spans.size());
		}

	}

}