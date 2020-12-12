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

	@BeforeEach
	void setUp() {
		loader = new CachingAsyncResourceLoader(executorService, TestCachingAsyncResourceLoader::loadResource)
	}

	@Test
	void "When called on two different URLs, two different futures are returned"() {
		def a = loader.loadFutureResource(URL_A)
		def b = loader.loadFutureResource(URL_B)

		assert !a.is(b)
	}

	@Test
	void "When called on the same URL, the same future is returned"() {
		def a = loader.loadFutureResource(URL_A)
		def a2 = loader.loadFutureResource(URL_A)

		assert a.is(a2)
	}

	@Test
	void "When called, the Future will resolve"() {
		assert loader.loadFutureResource(URL_A).get().resolve().url == URL_A
	}

	@Test
	void "When called on the same URL and resource has not finished loading, the same future is returned without blocking"() {
		loader = new CachingAsyncResourceLoader(new NeverExecutorService(), TestCachingAsyncResourceLoader::loadResource)

		def a = loader.loadFutureResource(URL_A)
		def a2 = loader.loadFutureResource(URL_A)

		assert a.is(a2) && !a.done
	}

	private static FutureResource loadResource(URL url) {
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
