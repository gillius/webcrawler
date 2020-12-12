package org.gillius.webcrawler

import groovy.transform.CompileStatic
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceError
import org.gillius.webcrawler.model.ResourceState
import org.gillius.webcrawler.parser.AutodetectParser
import org.gillius.webcrawler.parser.JsoupHtmlParser
import org.gillius.webcrawler.resourceloader.FileResourceLoader
import org.gillius.webcrawler.resourceloader.ResolvingResourceLoader
import org.gillius.webcrawler.resourceloader.ResourceLoader

/**
 * Entrypoint into the web crawler functionality
 */
@CompileStatic
class WebCrawler {
	static void main(String[] args) {
		println crawl(new File("src/test/resources/simple-site/index.html").toURI().toURL())
	}

	static Resource crawl(URL url) {
		new ResolvingResourceLoader(
				new FileResourceLoader(
						new AutodetectParser(
								new JsoupHtmlParser()
						)
				)
		).loadResource(url)
	}
}
