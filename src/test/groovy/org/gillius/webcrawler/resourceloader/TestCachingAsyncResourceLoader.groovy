package org.gillius.webcrawler.resourceloader

import org.gillius.webcrawler.model.Resource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * Tests the {@link CachingAsyncResourceLoader}.
 */
class TestCachingAsyncResourceLoader {
	private static final ExecutorService executorService = new ImmediateExecutorService()

	private static final URL_A = new URL("http://example.com/a")
	private static final URL_B = new URL("http://example.com/b")

	private CachingAsyncResourceLoader loader

	private Object lastState

	@BeforeEach
	void setUp() {
		loader = new CachingAsyncResourceLoader(executorService, this::loadResource)
		lastState = null
	}

	@Test
	void "When called on two different URLs, two different futures are returned"() {
		def a = loader.loadFutureResource(URL_A, null)
		def b = loader.loadFutureResource(URL_B, null)

		assert !a.is(b)
	}

	@Test
	void "When called on the same URL, the same future is returned"() {
		def a = loader.loadFutureResource(URL_A, null)
		def a2 = loader.loadFutureResource(URL_A, null)

		assert a.is(a2)
	}

	@Test
	void "When called, the Future will resolve"() {
		assert loader.loadFutureResource(URL_A, null).get().resolve().url == URL_A
	}

	@Test
	void "When called, the state will be passed through"() {
		loader.loadFutureResource(URL_A, 1).get().resolve().url
		assert lastState == 1
	}

	@Test
	void "When called on the same URL and resource has not finished loading, the same future is returned without blocking"() {
		loader = new CachingAsyncResourceLoader(new NeverExecutorService(), TestCachingAsyncResourceLoader::loadResource)

		def a = loader.loadFutureResource(URL_A, null)
		def a2 = loader.loadFutureResource(URL_A, null)

		assert a.is(a2) && !a.done
	}

	private FutureResource loadResource(URL url, Object state) {
		lastState = state
		return new FutureResource(new Resource(url: url), null)
	}

	/**
	 * Never shuts down and never runs any tasks.
	 */
	private static class NeverExecutorService extends AbstractExecutorService {
		void shutdown() {}
		List<Runnable> shutdownNow() { return null }
		boolean isShutdown() { return false }
		boolean isTerminated() { return false }
		boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException { return false }
		void execute(Runnable command) {}
	}
}
