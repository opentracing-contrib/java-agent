package io.opentracing.contrib.agent.concurrent;

import io.opentracing.Scope;
import io.opentracing.mock.MockSpan;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavol Loffay
 * @author Jose Montoya
 */
public class ScheduledExecutorServiceITest extends AbstractConcurrentTest {
	private static final int NUMBER_OF_THREADS = 4;

	@Test
	public void scheduleRunnableTest() throws InterruptedException {
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo-1").start();
		try (Scope scope = getTracer().scopeManager().activate(parentSpan, true)) {
			executorService.schedule(new TestRunnable(), 300, TimeUnit.MILLISECONDS);
			countDownLatch.await();
			assertParentSpan(parentSpan);
			assertEquals(1, getTracer().finishedSpans().size());
		}

	}

	@Test
	public void scheduleCallableTest() throws InterruptedException {
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo-2").start();
		try (Scope scope = getTracer().scopeManager().activate(parentSpan, true)) {
			executorService.schedule(new TestCallable(), 300, TimeUnit.MILLISECONDS);
			countDownLatch.await();
			assertParentSpan(parentSpan);
			assertEquals(1, getTracer().finishedSpans().size());
		}
	}

	@Test
	public void scheduleAtFixedRateTest() throws InterruptedException {
		countDownLatch = new CountDownLatch(2);
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo-3").start();
		try (Scope scope = getTracer().scopeManager().activate(parentSpan, true)) {
			executorService.scheduleAtFixedRate(new TestRunnable(), 0, 300, TimeUnit.MILLISECONDS);

			countDownLatch.await();
			executorService.shutdown();
			assertParentSpan(parentSpan);
			assertEquals(2, getTracer().finishedSpans().size());
		}
	}

	@Test
	public void scheduleWithFixedDelayTest() throws InterruptedException {
		countDownLatch = new CountDownLatch(2);
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo-4").start();
		try (Scope scope = getTracer().scopeManager().activate(parentSpan, true)) {
			executorService.scheduleWithFixedDelay(new TestRunnable(), 0, 300, TimeUnit.MILLISECONDS);
			countDownLatch.await();
			executorService.shutdown();
			assertParentSpan(parentSpan);
			assertEquals(2, getTracer().finishedSpans().size());
		}
	}
}
