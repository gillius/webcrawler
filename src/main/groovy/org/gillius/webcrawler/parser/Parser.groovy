package org.gillius.webcrawler.parser

import org.gillius.webcrawler.model.Resource

/**
 * A Parser parses data arriving via an InputStream to a {@link Resource}.
 */
@FunctionalInterface
interface Parser {
	/**
	 * Parses data arriving via an InputStream to a {@link Resource}.
	 *
	 * @param is      An InputStream, which will be possibly consumed and always closed.
	 * @param baseUrl base URL to assume if this document is HTML
	 *
	 * @return non-null Resource
	 */
	Resource parse(InputStream is, URL baseUrl)
}