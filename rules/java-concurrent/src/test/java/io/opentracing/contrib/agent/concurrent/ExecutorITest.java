package io.opentracing.contrib.agent.concurrent;

import io.opentracing.mock.MockSpan;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavol Loffay
 * @author Jose Montoya
 */
public class ExecutorITest extends AbstractConcurrentTest {

	@Test
	public void testExecute() throws InterruptedException {
		Executor executor = Executors.newFixedThreadPool(10);

		MockSpan parentSpan = getTracer().buildSpan("foo").startManual();
		getTracer().scopeManager().activate(parentSpan, true);
		executor.execute(new TestRunnable());

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(1, getTracer().finishedSpans().size());

	}
}
