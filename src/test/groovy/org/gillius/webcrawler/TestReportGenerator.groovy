package org.gillius.webcrawler

import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests the {@link ReportGenerator}.
 */
class TestReportGenerator {
	private Resource root

	@BeforeEach
	void setUp() {
		root = new Resource(
				url: url("root"),
				state: ResourceState.Exists,
				html: true,
		)

		root.links = [
		    new Resource(
				    url: url("a"),
				    state: ResourceState.Exists,
				    html: true,
				    links: [
				        new Resource(
						        url: url("b"),
						        state: ResourceState.Exists,
						        html: true,
						        links: [root]
				        ),
						    new Resource(
								    url: url("img"),
								    state: ResourceState.Exists,
								)
				    ]
		    ),
				new Resource(
						url: new URL("https://gillius.org/"),
						state: ResourceState.External,
				),
				new Resource(
						url: url("error"),
						state: ResourceState.Error,
				)
		]
	}

	@Test
	void "Sitemap report generator generates a list of internal HTML links"() {
		assert ReportGenerator.generateSitemapString(root).readLines() == [
				"http://example.com/root",
				"http://example.com/a",
				"http://example.com/b",
		]
	}

	@Test
	void "URL list generator generates a list of all URLs"() {
		assert ReportGenerator.generateUrlsListString(root).readLines() == [
				'http://example.com/root (HTML), 3 outgoing links',
				'http://example.com/a (HTML), 2 outgoing links',
				'http://example.com/b (HTML), 1 outgoing links',
				'http://example.com/img',
				'https://gillius.org/ (External)',
				'http://example.com/error (Error)',
		]
	}

	private static URL url(String path) {
		return new URL("http://example.com/" + path)
	}
}
