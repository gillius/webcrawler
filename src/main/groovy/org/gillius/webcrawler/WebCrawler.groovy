package org.gillius.webcrawler

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceSerializer
import org.gillius.webcrawler.parser.AutodetectParser
import org.gillius.webcrawler.parser.JsoupHtmlParser
import org.gillius.webcrawler.resourceloader.FileResourceLoader
import org.gillius.webcrawler.resourceloader.ResolvingResourceLoader

/**
 * Entrypoint into the web crawler functionality
 */
@CompileStatic
class WebCrawler {
	static void main(String[] args) {
		def res = crawl(new File("src/test/resources/simple-site/index.html").toURI().toURL())

		def out = new OutputStreamWriter(System.out)
		ResourceSerializer.toTextTree(res, out)

		out << "\n\n"
		out << JsonOutput.prettyPrint(ResourceSerializer.toJsonString(res))
		out.close()
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
