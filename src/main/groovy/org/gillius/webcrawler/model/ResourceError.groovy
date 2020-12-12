package org.gillius.webcrawler.model

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * Represents details about an error while retrieving a {@link Resource}
 */
@Immutable
@CompileStatic
class ResourceError implements Cloneable {
	/**
	 * HTTP response code, or 0 if an HTTP call could not even be made.
	 */
	int httpCode

	/**
	 * An error message describing the error, from web server or client code if network error prevented the connection.
	 */
	String message

	@Override
	ResourceError clone() {
		return (ResourceError) super.clone()
	}
}
