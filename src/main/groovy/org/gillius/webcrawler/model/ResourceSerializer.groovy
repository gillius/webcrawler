package org.gillius.webcrawler.model

import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * Methods to serialize a tree of {@link Resource}s.
 */
@CompileStatic
class ResourceSerializer {
	private static final String eol = System.getProperty("line.separator")

	/**
	 * Writes a human-readable text output of {@link Resource}s starting from the given root to a String.
	 */
	static String toTextTreeString(Resource resource) {
		toTextTree(resource, new StringWriter()).toString()
	}

	/**
	 * Streams a human-readable text output of {@link Resource}s starting from the given root to the specified Writer.
	 */
	static Writer toTextTree(Resource resource, Writer writer) {
		new ResourceTextWriter(writer).write(resource, 0)
		return writer
	}

	/**
	 * Performs a recursive write of resources, keeping track of which ones have seen to prevent infinite recursion due
	 * to cycles.
	 */
	private static class ResourceTextWriter {
		private final	Set<Resource> seen = Collections.newSetFromMap(new IdentityHashMap<Resource, Boolean>())
		private final Writer writer

		ResourceTextWriter(Writer writer) {
			this.writer = writer
		}

		void write(Resource resource, int level) {

			level.times {writer << "  " }
			writer << resource.selfString()

			if (seen.add(resource)) { //Only render links if we have not written out this Resource
				writer << eol

				int nextLevel = level + 1
				for (Resource link : resource.links) {
					write(link, nextLevel)
				}
			} else {
				if (resource.links)
					writer << " (links displayed earlier)" << eol
				else
					writer << eol
			}
		}
	}


	/**
	 * Writes a JSON output of {@link Resource}s starting from the given root to a String.
	 */
	static String toJsonString(Resource resource) {
		toJson(resource, new StringWriter()).toString()
	}

	/**
	 * Streams a JSON output of {@link Resource}s starting from the given root to a Writer.
	 */
	static Writer toJson(Resource resource, Writer writer) {
		new ResourceJsonWriter(writer).write(resource)
		return writer
	}

	private static class ResourceJsonWriter {
		private final	Set<Resource> seen = Collections.newSetFromMap(new IdentityHashMap<Resource, Boolean>())
		private final StreamingJsonBuilder builder

		ResourceJsonWriter(Writer writer) {
			builder = new StreamingJsonBuilder(writer)
		}

		@CompileDynamic
		void write(Resource resource) {
			builder {
				doWrite(delegate, resource)
			}
		}

		@CompileDynamic
		void doWrite(def b, Resource resource) {
			b.url resource.url
			b.title resource.title
			b.html resource.html
			b.state resource.state
			if (resource.error)
				b.error resource.error

			if (seen.add(resource)) {
				if (resource.links) {
					b.links(resource.links) {
						doWrite(delegate, it)
					}
				}
			} else {
				b.repeated true
			}
		}
	}
}
