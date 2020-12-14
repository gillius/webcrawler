package org.gillius.webcrawler.resourceloader

import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState
import org.junit.jupiter.api.Test

/**
 * Tests the {@link ResolvingResourceLoader} class.
 */
class TestResolvingResourceLoader {
	private static final URL_A   = new URL("http://example.com/a")
	private static final URL_B   = new URL("http://example.com/b")
	private static final URL_C   = new URL("http://example.com/c")
	private static final URL_EXT = new URL("http://example.com/E")

	@Test
	void "Loader executes without hitting external links or running in cycles"() {
		ResolvingResourceLoader loader = new ResolvingResourceLoader(TestResolvingResourceLoader::loadResource, 10)
		def resA = loader.loadResource(URL_A)

		//Now we need to validate that the Resource matches what getLinks does
		assert resA.state == ResourceState.Exists && resA.url == URL_A
		assert resA.links.size() == 1

		def resB = resA.links[0]
		assert resB.state == ResourceState.Exists && resB.url == URL_B
		assert resB.links*.url == [URL_C, URL_EXT]
		assert resB.links*.state == [ResourceState.Exists, ResourceState.External]

		def resC = resB.links[0]
		assert resC.state == ResourceState.Exists && resC.url == URL_C
		assert resC.links == [resA] //Check that we actually got the cached copy
	}

	private static Resource loadResource(URL url) {
		assert url != URL_EXT

		new Resource(state: ResourceState.Exists, url: url, links: getLinks(url))
	}

	private static List<Resource> getLinks(URL url) {
		switch (url) {
			case URL_A:
				return [new Resource(state: ResourceState.Unresolved, url: URL_B)]

			case URL_B:
				return [
						new Resource(state: ResourceState.Unresolved, url: URL_C),
						new Resource(state: ResourceState.External, url: URL_EXT), //External Link
				]

			case URL_C:
				return [new Resource(state: ResourceState.Unresolved, url: URL_A)] //creates a cycle
			default:
				assert false : "Invalid URL $url"
		}
	}

	@Test
	void "Loader does not traverse more than maxDepth"() {
		ResolvingResourceLoader loader = new ResolvingResourceLoader(TestResolvingResourceLoader::loadInfiniteResource, 4)

		def res1 = loader.loadResource(new URL("http://example.com/1"))
		def res2 = res1.links[0]
		assert res2.url == new URL("http://example.com/2") && res2.state == ResourceState.Exists
		def res3 = res2.links[0]
		assert res3.url == new URL("http://example.com/3") && res3.state == ResourceState.Exists
		def res4 = res3.links[0]
		assert res4.url == new URL("http://example.com/4") && res4.state == ResourceState.Exists
		def res5 = res4.links[0]
		assert res5.url == new URL("http://example.com/5") && res5.state == ResourceState.Unresolved && res5.links.empty
	}

	private static Resource loadInfiniteResource(URL url) {
		int page = url.path[1..-1] as int //trim the leading /

		return new Resource(state: ResourceState.Exists, url: url, links: [
		    new Resource(state: ResourceState.Unresolved, url: new URL("http://example.com/${page+1}"))
		])
	}
}
