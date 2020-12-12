package org.gillius.webcrawler.resourceloader

import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import java.time.Duration

/**
 * Tests the {@link ResolvingResourceLoader} class.
 */
class TestResolvingResourceLoader {
	private static final URL_A   = new URL("http://example.com/a")
	private static final URL_B   = new URL("http://example.com/b")
	private static final URL_C   = new URL("http://example.com/c")
	private static final URL_EXT = new URL("http://example.com/E")

	private ResolvingResourceLoader loader

	@BeforeEach
	void setUp() {
		loader = new ResolvingResourceLoader(TestResolvingResourceLoader::loadResource)
	}

	@Test
	void "Loader executes without hitting external links or running in cycles"() {
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
}
