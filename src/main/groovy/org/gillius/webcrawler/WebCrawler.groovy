package org.gillius.webcrawler

import groovy.transform.CompileStatic
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceError
import org.gillius.webcrawler.model.ResourceState

/**
 * Entrypoint into the web crawler functionality
 */
@CompileStatic
class WebCrawler {
	static Resource crawl(URL url) {
		new Resource(
				url: url,
				state: ResourceState.Broken,
				error: new ResourceError(httpCode: 404)
		)
	}
}
