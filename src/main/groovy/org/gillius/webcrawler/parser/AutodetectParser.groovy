package org.gillius.webcrawler.parser

import groovy.transform.CompileStatic
import org.gillius.webcrawler.HtmlDetector
import org.gillius.webcrawler.UrlUtil
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Function

/**
 * The AutodetectParser determines if the input is HTML or not and if it is, parses the full document with a parser
 * which can assume the content is likely HTML. If the content does not appear to be HTML the InputStream is closed.
 * In either case, the given InputStream is closed.
 */
@CompileStatic
class AutodetectParser implements Parser {
	private final static Logger log = LoggerFactory.getLogger(AutodetectParser)

	/**
	 * The detector to use to check if data is HTML.
	 */
	Function<InputStream, Boolean> htmlDetector = HtmlDetector::isLikelyHtml as Function

	private final Parser htmlParser

	AutodetectParser(Parser htmlParser) {
		this.htmlParser = htmlParser
	}

	@Override
	Resource parse(InputStream is, URL baseUrl) throws IOException {
		try (def bis = new BufferedInputStream(is)) {
			if (htmlDetector.apply(bis)) {
				log.debug("{} detected as HTML", baseUrl)
				return htmlParser.parse(bis, baseUrl)

			} else {
				log.debug("{} not detected as HTML", baseUrl)
				is.close() //close the stream so that we don't read it and possibly close network connection ASAP
				return new Resource (
						url: baseUrl,
						state: ResourceState.Exists,
						title: UrlUtil.getTitleFromUrlPath(baseUrl),
				)
			}
		}
	}
}
