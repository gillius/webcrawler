package org.gillius.webcrawler.resourceloader

import org.gillius.webcrawler.model.Resource

/**
 * A strategy to load {@link Resource}s from a {@link URL}.
 */
@FunctionalInterface
interface ResourceLoader {
	/**
	 * Loads a resource from the given URL. This method must not throw any Exception. Instead of throwing exceptions,
	 * normally an error state {@link Resource} is returned.
	 */
	Resource loadResource(URL url)
}
