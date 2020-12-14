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
	 * Parses the given string, attempting to clean up issues like " " not being escaped, then runs the logic of
	 * {@link #removeDefaultPortAndRefAndNormalize(java.net.URL)}.
	 */
	static URL parseUrlForCrawling(String url) throws MalformedURLException {
		//TODO: improve this, but it seems like the vast majority of issues are with space.
		//URLEncoder is too aggressive, so we can't use it.
		removeDefaultPortAndRefAndNormalize(new URL(url.replace(" ", "%20")))
	}

	/**
	 * Removes the "ref" part of the URL (aka the "anchor"), and the port number if it is 80 on http or 443 on https.
	 */
	static URL removeDefaultPortAndRefAndNormalize(URL url) {
		def uri = url.toURI().normalize()
		if (url.ref || url.port == url.defaultPort) {
			def updatedUri = new URI(uri.scheme, uri.rawUserInfo, uri.host, -1, uri.rawPath, uri.rawQuery, null)
			return updatedUri.toURL()
		} else {
			return uri.toURL()
		}
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
