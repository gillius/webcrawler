package org.gillius.webcrawler.model

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * A Page is a type of Resource that represents content, usually HTML, that provides links to other documents.
 */
@EqualsAndHashCode
@ToString
@CompileStatic
class Page extends Resource {
	/**
	 * Title from the head tag, if any.
	 */
	String title

	/**
	 * List of links in the order they are discovered (note there may be cycles).
	 */
	List<Resource> links
}
