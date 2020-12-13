package org.gillius.webcrawler.parser

import groovy.transform.CompileStatic
import org.gillius.webcrawler.UrlUtil
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState
import org.jsoup.Jsoup
import org.jsoup.select.Elements

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Uses jsoup library to parse an input stream that is assumed to likely be valid HTML.
 */
@CompileStatic
class JsoupHtmlParser implements Parser {
	@Override
	Resource parse(InputStream is, URL baseUrl) throws IOException {
		//specify null as charset name to detect charset based on BOM or meta tag
		def doc = Jsoup.parse(is, null, baseUrl.toString())

		def aHrefs = doc.select("a[href]").eachAttr("abs:href")
		def imgAndMedia = doc.select("[src]").eachAttr("abs:src")
		def imports = doc.select("link[href]").eachAttr("abs:href")

		def allRefs = aHrefs + imgAndMedia + imports

		return new Resource(
				url: baseUrl,
				state: ResourceState.Exists,
				title: doc.title(),
				links: allRefs.collect {href ->
					def linkUrl = new URL(href)
					new Resource(
							url: linkUrl,
							title: UrlUtil.getTitleFromUrlPath(linkUrl),
							state: UrlUtil.isExternal(linkUrl, baseUrl) ? ResourceState.External : ResourceState.Unresolved,
					)
				}
		)
	}
}
