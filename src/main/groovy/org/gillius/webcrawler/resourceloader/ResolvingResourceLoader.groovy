package org.gillius.webcrawler.resourceloader

import groovy.transform.CompileStatic
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * A ResourceLoader that delegates to another loader, and if that loader returns any unresolved links, runs the loader
 * on those links, potentially in parallel depending on the implementation of the provided ExecutorService. Caching is
 * performed to handle any potential cycles in the graph. The default executor if one is not provided will run all
 * requests in the calling thread (not in parallel).
 */
@CompileStatic
class ResolvingResourceLoader implements ResourceLoader, FutureResourceLoader {
	private final static Logger log = LoggerFactory.getLogger(ResolvingResourceLoader)

	private final ResourceLoader directLoader
	private final CachingAsyncResourceLoader asyncLoader

	ResolvingResourceLoader(ResourceLoader loader, ExecutorService executor = new ImmediateExecutorService()) {
		directLoader = loader
		asyncLoader = new CachingAsyncResourceLoader(executor, this)
	}

	@Override
	Resource loadResource(URL url) throws IOException {
		//We have to put even the initial request through the asyncLoader so that it gets into the cache
		def futureResource = asyncLoader.loadFutureResource(url).get()
		return futureResource.resolve()
	}

	FutureResource loadFutureResource(URL url) {
		log.info("Loading URL {}", url)
		Resource ret = directLoader.loadResource(url)

		//Schedule all of the links for loading
		List<Future<FutureResource>> futureResources = ret.links.collect { link ->
			if (link.state == ResourceState.Unresolved) {
				return asyncLoader.loadFutureResource(link.url)
			} else {
				return CompletableFuture.completedFuture(new FutureResource(link, null))
			}
		}

		return new FutureResource(ret, futureResources)
	}
}
