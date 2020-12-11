package org.gillius.webcrawler

import groovy.transform.CompileStatic
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceError
import org.gillius.webcrawler.model.ResourceState
import org.gillius.webcrawler.parser.AutodetectParser
import org.gillius.webcrawler.parser.JsoupHtmlParser
import org.gillius.webcrawler.resourceloader.FileResourceLoader

/**
 * Entrypoint into the web crawler functionality
 */
@CompileStatic
class WebCrawler {
	static void main(String[] args) {
		FileResourceLoader loader = new FileResourceLoader(
				new AutodetectParser(
						new JsoupHtmlParser()
				)
		)

		println loader.loadResource(new File("src/test/resources/simple-site/index.html").toURI().toURL())
	}

	static Resource crawl(URL url) {
		new Resource(
				url: url,
				state: ResourceState.Broken,
				error: new ResourceError(httpCode: 404)
		)
	}
}
