package org.gillius.webcrawler.resourceloader

/**
 * The FutureResourceLoader is a variation of ResourceLoader that allows returning a Resource that will be resolved in
 * the future.
 */
@FunctionalInterface
interface FutureResourceLoader {
	/**
	 * @param url   URL to load
	 * @param state object used internally to track recursive state. If calling externally, set to null.
	 */
	FutureResource loadFutureResource(URL url, Object state)
}