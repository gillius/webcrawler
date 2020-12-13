package org.gillius.webcrawler

import groovy.transform.CompileStatic

/**
 * Contains utility methods to work with {@link URL}s.
 */
@CompileStatic
class UrlUtil {
	/**
	 * Returns the title to be used for a resource specified by the given URL.
	 */
	static String getTitleFromUrlPath(URL url) {
		def title = url.path
		int lastSlash = title.lastIndexOf('/') + 1

		if (lastSlash < 0)
			return null
		else if (lastSlash == title.length())
			return '/' //special case for the root resource we want to show as / and not empty string
		else
			return title[lastSlash..-1]
	}

	/**
	 * Returns true if the 'url' should be considered as an external link from 'base'.
	 */
	static boolean isExternal(URL url, URL base) {
		//Do we want to consider a looser comparison to allow subdomains, i.e. images.example.com is not external to www.example.com?
		url.authority != base.authority ||
		effectiveProtocol(url) != effectiveProtocol(base)
	}

	/**
	 * Maps https to http so that https and http can compare as equal in external check.
	 */
	private static String effectiveProtocol(URL url) {
		url.protocol == "https" ? "http" : url.protocol
	}
}
