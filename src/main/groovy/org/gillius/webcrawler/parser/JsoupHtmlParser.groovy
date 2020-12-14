package org.gillius.webcrawler.parser

import groovy.transform.CompileStatic
import org.gillius.webcrawler.UrlUtil
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState
import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Uses jsoup library to parse an input stream that is assumed to likely be valid HTML.
 */
@CompileStatic
class JsoupHtmlParser implements Parser {
	private final static Logger log = LoggerFactory.getLogger(JsoupHtmlParser)

	@Override
	Resource parse(InputStream is, URL baseUrl) throws IOException {
		//specify null as charset name to detect charset based on BOM or meta tag
		def doc = Jsoup.parse(is, null, baseUrl.toString())

		def aHrefs = doc.select("a[href]").eachAttr("abs:href")
		def imgAndMedia = doc.select("[src]").eachAttr("abs:src")
		def imports = doc.select("link[href]").eachAttr("abs:href")

		def allUrls = (aHrefs + imgAndMedia + imports).collect {
			try {
				return UrlUtil.parseUrlForCrawling(it)
			} catch (e) {
				log.error("Ignoring URL {} as it cannot be parsed: {}", it, e.toString())
				return null
			}
		}.grep() //grep will remove all of the nulls

		def uniqueUrls = new LinkedHashSet<>(allUrls)

		return new Resource(
				url: baseUrl,
				state: ResourceState.Exists,
				title: doc.title(),
				html: true,
				links: uniqueUrls.collect {linkUrl ->
					new Resource(
							url: linkUrl,
							title: UrlUtil.getTitleFromUrlPath(linkUrl),
							state: UrlUtil.isExternal(linkUrl, baseUrl) ? ResourceState.External : ResourceState.Unresolved,
					)
				}
		)
	}
}
