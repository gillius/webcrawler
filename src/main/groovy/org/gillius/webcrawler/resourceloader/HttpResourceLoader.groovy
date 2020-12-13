package org.gillius.webcrawler.resourceloader

import groovy.transform.CompileStatic
import org.gillius.webcrawler.UrlUtil
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceError
import org.gillius.webcrawler.model.ResourceState
import org.gillius.webcrawler.parser.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@CompileStatic
class HttpResourceLoader implements ResourceLoader {
	private final static Logger log = LoggerFactory.getLogger(HttpResourceLoader)

	private final HttpClient client = HttpClient.newBuilder()
	                                            .followRedirects(HttpClient.Redirect.NORMAL)
	                                            .build()

	private final Parser parser

	/**
	 * Constructs a HttpResourceLoader.
	 */
	HttpResourceLoader(Parser parser) {
		this.parser = parser
	}

	@Override
	Resource loadResource(URL url) {
		def req = HttpRequest.newBuilder(url.toURI())
		                     .header("Accept", "text/html")
		                     .build()

		def resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream())

		if (resp.statusCode() >= 400) {
			log.debug("{} returned code {}", url, resp.statusCode())
			resp.body().close()
			return new Resource(
					url: url,
					title: UrlUtil.getTitleFromUrlPath(url),
					state: getStateFromCode(resp.statusCode()),
					error: new ResourceError(resp.statusCode(), null)
			)
		}

		return parser.parse(resp.body(), url)
	}

	private static ResourceState getStateFromCode(int statusCode) {
		switch (statusCode) {
			case 404: return ResourceState.Broken
			case 406: return ResourceState.Exists //The server is reporting that this content cannot be text/html so we don't process it.
			default: return ResourceState.Error
		}
	}
}
