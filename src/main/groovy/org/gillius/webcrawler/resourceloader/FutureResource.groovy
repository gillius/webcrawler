package org.gillius.webcrawler.resourceloader

import groovy.transform.CompileStatic
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceError
import org.gillius.webcrawler.model.ResourceState

import java.util.concurrent.Future

/**
 * Holds a partially-resolved Resource that will be completely resolved in the future.
 */
@CompileStatic
class FutureResource {
	private final Resource resource
	private List<Future<FutureResource>> futureResources

	FutureResource(Resource resource, List<Future<FutureResource>> futureResources) {
		this.resource = resource
		this.futureResources = futureResources
	}

	/**
	 *  Resolve a FutureResource into a Resource, including all of its links, by blocking until the resolve completes.
	 */
	Resource resolve() {
		if (futureResources) {
			//So this trick prevents infinite recursion so if this FutureResource is already resolving, we don't try to
			//resolve it again.
			def cap = futureResources
			futureResources = null
			resource.links = cap.collect {
				try {
					return it.get().resolve()
				} catch(e) {
					return new Resource(state: ResourceState.Error, error: new ResourceError(0, e.toString()))
				}
			}
		}
		return resource
	}
}
