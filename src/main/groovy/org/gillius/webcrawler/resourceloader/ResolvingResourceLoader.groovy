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
 * <p>A ResourceLoader that delegates to another loader, and if that loader returns any unresolved links, runs the loader
 * on those links, potentially in parallel depending on the implementation of the provided ExecutorService. Caching is
 * performed to handle any potential cycles in the graph. The default executor if one is not provided will run all
 * requests in the calling thread (not in parallel).
 *
 * <p>The loader also will only document link hierarchies up to "maxLevels" deep as specified in the constructor. The
 * purpose of this parameter is NOT to fix issues with cycles (i.e. A -> B -> C -> A). Instead it is to fix issues with
 * dynamic link generation where you might be led down a very deep path, i.e. a site showing a huge database and
 * people -> people?from=100 -> people?from=200 -> people?from=300, etc.
 */
@CompileStatic
class ResolvingResourceLoader implements ResourceLoader {
	private final static Logger log = LoggerFactory.getLogger(ResolvingResourceLoader)

	private final ResourceLoader directLoader
	private final CachingAsyncResourceLoader asyncLoader
	private final int maxDepth

	ResolvingResourceLoader(ResourceLoader loader, int maxDepth, ExecutorService executor = new ImmediateExecutorService()) {
		directLoader = loader
		asyncLoader = new CachingAsyncResourceLoader(executor, new FRL())
		this.maxDepth = maxDepth
	}

	@Override
	Resource loadResource(URL url) throws IOException {
		//We have to put even the initial request through the asyncLoader so that it gets into the cache
		def futureResource = asyncLoader.loadFutureResource(url, 1).get()
		Resource ret = futureResource.resolve()

		//Once the resource is returned we know all async tasks are done. Give a second chance at resolving anything
		//maxDepth missed by checking the cache.
		new SecondChanceResolver().secondChanceResolve(ret)

		return ret
	}

	private class SecondChanceResolver {
		private final	Set<Resource> seen = Collections.newSetFromMap(new IdentityHashMap<Resource, Boolean>())

		private void secondChanceResolve(Resource res) {
			if (!seen.add(res)) return //eliminate cycles

			if (res.links.any{it.state == ResourceState.Unresolved}) {
				res.links = res.links.collect {link ->
					if (link.state == ResourceState.Unresolved) {
						def entry = asyncLoader.getCachedEntry(link.url)
						return entry ? entry.get().resolve() : link
					} else {
						return link
					}
				}
			}

			res.links.each(this.&secondChanceResolve)
		}
	}

	private class FRL implements FutureResourceLoader {
		FutureResource loadFutureResource(URL url, def state) {
			log.info("(depth={}) Loading URL {}", state, url)
			Resource ret = directLoader.loadResource(url)

			//Schedule all of the links for loading, if we are not beyond maxDepth
			List<Future<FutureResource>> futureResources
			Integer depth = (Integer) state //as this class is private, we can assume depth is our Integer passed in
			if (depth < maxDepth) {
				futureResources = ret.links.collect { link ->
					if (link.state == ResourceState.Unresolved) {
						return asyncLoader.loadFutureResource(link.url, depth + 1)
					} else {
						return CompletableFuture.completedFuture(new FutureResource(link, null))
					}
				}
			} else {
				futureResources = Collections.emptyList()
			}

			return new FutureResource(ret, futureResources)
		}
	}
}
