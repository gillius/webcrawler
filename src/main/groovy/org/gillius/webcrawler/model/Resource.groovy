package org.gillius.webcrawler.model

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * A Resource defines an item linked to on a site. A Resource may potentially be a {@link Page}, which can link to
 * other resources.
 */
@EqualsAndHashCode
@ToString
@CompileStatic
class Resource {
	/**
	 * The URL of the resource. May not be null.
	 */
	URL url

	/**
	 * A title or description of the resource. For HTML pages, this is the title tag's value. May be null if there is no
	 * way to obtain a title from the page.
	 */
	String title

	/**
	 * The scanned state of the resource.
	 */
	ResourceState state

	/**
	 * If there was an error retrieving this resource, this will not be null and will describe the error.
	 */
	ResourceError error
}
