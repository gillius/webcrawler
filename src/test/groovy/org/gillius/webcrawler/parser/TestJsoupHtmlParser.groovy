package org.gillius.webcrawler.parser


import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestJsoupHtmlParser {
	private static URL BASE_URL = new URL("http://example.com/")
	private Resource res

	@BeforeEach
	void setUp() {
		res = new JsoupHtmlParser().parse(TestJsoupHtmlParser.getResourceAsStream("/simple-site/index.html"),
		                                  BASE_URL)
	}

	@Test
	void "Parser uses the given base URL"() {
		assert res.url == BASE_URL
	}

	@Test
	void "Parser loads the document's title"() {
		assert res.title == "My Simple Blog"
	}

	@Test
	void "Parser loads all linked resources as unresolved"() {
		assert res.links*.state.every { it == ResourceState.Unresolved }
	}

	@Test
	void "Parser resolves relative links based on the base URL"() {
		assert new URL("http://example.com/entry1.html") in res.links*.url
	}

	@Test
	void "Parser handles absolute URIs"() {
		assert new URL("https://gillius.org/m3/guistart.jpg") in res.links*.url
	}

	@Test
	void "Parser does not mark anything with an error"() {
		assert res.error == null
		assert res.links*.error.every { it == null }
	}

	@Test
	void "Parser finds all stylesheets, images, links, scripts"() {
		assert res.links.size() == 6
	}
}
