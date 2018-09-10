package io.opentracing.contrib.agent.concurrent;

import io.opentracing.contrib.agent.common.OTAgentTestBase;
import io.opentracing.mock.MockSpan;
import org.junit.Before;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavol Loffay
 * @author Jose Montoya
 */
public abstract class AbstractConcurrentTest extends OTAgentTestBase {

	protected CountDownLatch countDownLatch = new CountDownLatch(0);

	@Before
	@Override
	public void init() {
		super.init();
		countDownLatch = new CountDownLatch(1);
	}

	protected void assertParentSpan(MockSpan parent) {
		for (MockSpan child : getTracer().finishedSpans()) {
			if (child == parent) {
				continue;
			}

			if (parent == null) {
				assertEquals(0, child.parentId());
			} else {
				assertEquals(parent.context().traceId(), child.context().traceId());
				assertEquals(parent.context().spanId(), child.parentId());
			}
		}
	}

	protected Thread createThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		return thread;
	}

	protected <V> Thread createThread(FutureTask<V> futureTask) {
		Thread thread = new Thread(futureTask);
		return thread;
	}

	class TestRunnable implements Runnable {
		@Override
		public void run() {
			try {
				getTracer().buildSpan("childRunnable")
						.startActive(true)
						.close();
			} finally {
				countDownLatch.countDown();
			}
		}
	}

	class TestCallable implements Callable<Void> {
		@Override
		public Void call() throws Exception {
			try {
				getTracer().buildSpan("childCallable")
						.startActive(true)
						.close();
			} finally {
				countDownLatch.countDown();
			}
			return null;
		}
	}
}
