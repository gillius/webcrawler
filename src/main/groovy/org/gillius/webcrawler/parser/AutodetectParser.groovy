package org.gillius.webcrawler.parser

import groovy.transform.CompileStatic
import org.gillius.webcrawler.HtmlDetector
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState

import java.util.function.Function

/**
 * The AutodetectParser determines if the input is HTML or not and if it is, parses the full document with a parser
 * which can assume the content is likely HTML. If the content does not appear to be HTML the InputStream is closed.
 * In either case, the given InputStream is closed.
 */
@CompileStatic
class AutodetectParser implements Parser {
	/**
	 * The detector to use to check if data is HTML.
	 */
	Function<InputStream, Boolean> htmlDetector = HtmlDetector::isLikelyHtml as Function

	private final Parser htmlParser

	AutodetectParser(Parser htmlParser) {
		this.htmlParser = htmlParser
	}

	@Override
	Resource parse(InputStream is, URL baseUrl) {
		try (def bis = new BufferedInputStream(is)) {
			if (htmlDetector.apply(bis)) {
				return htmlParser.parse(bis, baseUrl)

			} else {
				return new Resource (
						url: baseUrl,
						state: ResourceState.Exists,
						title: baseUrl.toString(),
				)
			}
		}
	}
}
