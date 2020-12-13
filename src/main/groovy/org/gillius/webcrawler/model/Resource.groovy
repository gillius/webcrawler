package org.gillius.webcrawler.model

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

/**
 * A Resource defines an item linked to on a site. A Resource may potentially link to other resources.
 */
@EqualsAndHashCode
@CompileStatic
class Resource implements Cloneable {
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
	 * List of links in the order they are discovered (note there may be cycles). Never null, but may be an empty list.
	 */
	List<Resource> links = Collections.emptyList()

	/**
	 * The scanned state of the resource.
	 */
	ResourceState state

	/**
	 * If there was an error retrieving this resource, this will not be null and will describe the error.
	 */
	ResourceError error

	@Override
	Resource clone() {
		Resource ret = (Resource) super.clone()
		ret.links = (links == null || links.empty) ? Collections.<Resource>emptyList() : new ArrayList<Resource>(links)
		ret.error = error?.clone()
		return ret
	}

	String selfString() {
		StringBuilder out = new StringBuilder()
		out << url
		if (state != null && state != ResourceState.Exists)
			out << " (" << state << ")"
		if (title)
			out << " (" << title << ")"
		if (error)
			out << " (Error " << error.httpCode << ": " << error.message << ")"
		return out.toString()
	}

	/**
	 * Prints out this Resource as a string, but does not print multiple levels of links (to prevent infinite recursion
	 * if there is a cycle).
	 */
	@Override
	String toString() {
		return selfString() + (links ? ", links: \n\t" + links.collect {it.selfString()}.join('\n\t') : "")
	}
}
