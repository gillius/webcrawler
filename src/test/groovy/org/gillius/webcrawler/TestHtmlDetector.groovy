package org.gillius.webcrawler

import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets

/**
 * Tests the {@link HtmlDetector}.
 */
class TestHtmlDetector {
	@Test
	void "HtmlDetector detects a well-formed HTML document in UTF-8"() {
		def html = wellFormedHtml.getBytes(StandardCharsets.UTF_8)

		assert HtmlDetector.isLikelyHtml(html)
	}

	@Test
	void "HtmlDetector detects a well-formed HTML document in UTF-16BE"() {
		def baos = new ByteArrayOutputStream(512)
		baos.write(0xFE)
		baos.write(0xFF)
		try (def w = new OutputStreamWriter(baos, StandardCharsets.UTF_16BE)) {
			w.write(wellFormedHtml)
		}

		assert HtmlDetector.isLikelyHtml(baos.toByteArray())
	}

	@Test
	void "HtmlDetector detects a well-formed HTML document in UTF-16LE"() {
		def baos = new ByteArrayOutputStream(512)
		baos.write(0xFF)
		baos.write(0xFE)
		try (def w = new OutputStreamWriter(baos, StandardCharsets.UTF_16LE)) {
			w.write(wellFormedHtml)
		}

		assert HtmlDetector.isLikelyHtml(baos.toByteArray())
	}

	@Test
	void "HtmlDetector does not detect binary data as HTML"() {
		def rnd = new Random(0)
		def html = new byte[512]
		rnd.nextBytes(html)

		assert !HtmlDetector.isLikelyHtml(html)
	}

	@Test
	void "HtmlDetector does not detect a plain text file as HTML"() {
		def html = "123".getBytes(StandardCharsets.UTF_16)

		assert !HtmlDetector.isLikelyHtml(html)
	}

	@Test
	void "HtmlDetector does not detect an XML file as HTML"() {
		def html = "<?xml version='1.0'><root></root>".getBytes(StandardCharsets.UTF_16)

		assert !HtmlDetector.isLikelyHtml(html)
	}

	@Test
	void "HtmlDetector does detect an XHTML file as HTML"() {
		def html = "<?xml version='1.0'><html></html>".getBytes(StandardCharsets.UTF_8)

		assert HtmlDetector.isLikelyHtml(html)
	}

	@Test
	void "HtmlDetector does detect an invalid but parsable file with just a body as HTML"() {
		def html = "<body></body>".getBytes(StandardCharsets.UTF_8)

		assert HtmlDetector.isLikelyHtml(html)
	}

	private static final String wellFormedHtml = """<!DOCTYPE html>
<html lang="en">
<head>
\t<meta charset="UTF-8">
\t<title>My Simple Blog</title>
</head>
<body></body>
</html>"""
}
