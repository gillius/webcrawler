package org.gillius.webcrawler

import org.junit.jupiter.api.Test

/**
 * Tests the {@link UrlUtil} class.
 */
class TestUrlUtil {
	@Test
	void "The last component of a url of a subresource is used as title"() {
		assert UrlUtil.getTitleFromUrlPath(new URL("http://example.com/some/file.jpg")) == "file.jpg"
	}

	@Test
	void "The last component of the url of a top-level resource is used as title"() {
		assert UrlUtil.getTitleFromUrlPath(new URL("http://example.com/some/file.jpg")) == "file.jpg"
	}

	@Test
	void "The title of the root resource is a single slash"() {
		assert UrlUtil.getTitleFromUrlPath(new URL("http://example.com/")) == "/"
	}

	@Test
	void "www_example_com is external to example_com"() {
		assert UrlUtil.isExternal(new URL("http://www.example.com/"), new URL("http://example.com"))
	}

	@Test
	void "Two example_com URLs are not considered external to each other"() {
		assert !UrlUtil.isExternal(new URL("http://example.com/some/path"), new URL("http://example.com/other/path"))
	}

	@Test
	void "Two example_com URLs are not considered external to each other even if protocol is https on one and http on the other"() {
		assert !UrlUtil.isExternal(new URL("https://example.com/some/path"), new URL("http://example.com/other/path"))
		assert !UrlUtil.isExternal(new URL("http://example.com/some/path"), new URL("https://example.com/other/path"))
	}
}
