package io.opentracing.contrib.agent.concurrent;

import io.opentracing.mock.MockSpan;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavol Loffay
 * @author Jose Montoya
 */
public class ExecutorServiceITest extends AbstractConcurrentTest {
	private static final int NUMBER_OF_THREADS = 4;

	@Test
	public void testExecuteRunnable() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo").startManual();
		getTracer().scopeManager().activate(parentSpan, true);
		executorService.execute(new TestRunnable());

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(1, getTracer().finishedSpans().size());
	}

	@Test
	public void testSubmitRunnable() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo").startManual();
		getTracer().scopeManager().activate(parentSpan, true);
		executorService.submit(new TestRunnable());

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(1, getTracer().finishedSpans().size());
	}

	@Test
	public void testSubmitRunnableTyped() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo").startManual();
		getTracer().scopeManager().activate(parentSpan, true);
		executorService.submit(new TestRunnable(), new Object());

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(1, getTracer().finishedSpans().size());
	}

	@Test
	public void testSubmitCallable() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo").startManual();
		getTracer().scopeManager().activate(parentSpan, true);
		executorService.submit(new TestCallable());

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(1, getTracer().finishedSpans().size());
	}

	@Test
	public void testInvokeAll() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo").startManual();
		getTracer().scopeManager().activate(parentSpan, true);
		countDownLatch = new CountDownLatch(2);
		executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()));

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(2, getTracer().finishedSpans().size());
	}

	@Test
	public void testInvokeAllTimeUnit() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo").startManual();
		getTracer().scopeManager().activate(parentSpan, true);
		countDownLatch = new CountDownLatch(2);
		executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()), 1, TimeUnit.SECONDS);

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(2, getTracer().finishedSpans().size());
	}

	@Test
	public void testInvokeAnyTimeUnit() throws InterruptedException, ExecutionException, TimeoutException {
		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo").startManual();
		getTracer().scopeManager().activate(parentSpan, true);
		executorService.invokeAny(Arrays.asList(new TestCallable()), 1, TimeUnit.SECONDS);

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(1, getTracer().finishedSpans().size());
	}

	@Test
	public void testInvokeAny() throws InterruptedException, ExecutionException {
		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		MockSpan parentSpan = getTracer().buildSpan("foo").startManual();
		getTracer().scopeManager().activate(parentSpan, true);
		executorService.invokeAny(Arrays.asList(new TestCallable()));

		countDownLatch.await();
		assertParentSpan(parentSpan);
		assertEquals(1, getTracer().finishedSpans().size());
	}
}
