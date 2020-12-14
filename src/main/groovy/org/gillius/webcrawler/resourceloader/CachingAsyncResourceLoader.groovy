package org.gillius.webcrawler.resourceloader

import groovy.transform.CompileStatic

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * <p>Similar to {@link FutureResourceLoader}, but it returns futures instead. Using the supplied Executor and ResourceLoader,
 * it defers resources to be loaded in the background. Additionally, it also provides caching of the futures, so that
 * multiple requests for the same URL result in only one scheduled future. This means for the same URL, the same
 * Future is returned.
 *
 * <p>No attempt is made to determine if two different URLs point to the actual same content (such as http://example.org/info
 * and http://example.org/info.html). Doing so requires at least a HEAD request anyway, which defeats much of the
 * benefit. URLs are considered equal via the {@link URL#equals(java.lang.Object)} method.
 *
 * <p>While the caching helps improve performance, the main benefit to caching is that is handles the case of cycles,
 * specifically the guarantee that calling loadResource on the same URL returns the same Future, even if called before
 * the initial request completes.
 *
 * <p>The cache has no limit to its size, although it can be cleared.
 */
@CompileStatic
class CachingAsyncResourceLoader {
	private final ExecutorService executor
	private final FutureResourceLoader resourceLoader

	private final Map<URL, Future<FutureResource>> cache = new ConcurrentHashMap<>()

	CachingAsyncResourceLoader(ExecutorService executor, FutureResourceLoader resourceLoader) {
		this.executor = executor
		this.resourceLoader = resourceLoader
	}

	Future<FutureResource> loadFutureResource(URL url, Object state) {
		CompletableFuture<FutureResource> future = new CompletableFuture<>()
		Future<FutureResource> ret = cache.computeIfAbsent(url, it -> future)

		if (future.is(ret)) {
			//We're the winning thread to submit this task.
			//We can't submit in the computeIfAbsent because it would lead to IllegalStateException: recursive update
			executor.submit {
				try {
					future.complete(resourceLoader.loadFutureResource(url, state))
				} catch (Throwable e) {
					future.completeExceptionally(e)
				}
			}
		}

		return ret
	}

	void clearCache() {
		cache.clear()
	}
}
