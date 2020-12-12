package org.gillius.webcrawler

import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceError
import org.gillius.webcrawler.model.ResourceState
import org.junit.jupiter.api.Test

/**
 * Tests the {@link WebCrawler} class.
 */
class TestWebCrawler {
	@Test
	void "Test crawling a website that does not exist"() {
		def url = new URL("file:///does-not-exist")
		def result = WebCrawler.crawl(url)

		assert result == new Resource(
				url: url,
				title: "does-not-exist",
				state: ResourceState.Broken,
				error: new ResourceError(404, "File not found")
		)
	}
}
