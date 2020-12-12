package org.gillius.webcrawler.resourceloader

/**
 * The FutureResourceLoader is a variation of ResourceLoader that allows returning a Resource that will be resolved in
 * the future.
 */
@FunctionalInterface
interface FutureResourceLoader {
	FutureResource loadFutureResource(URL url)
}