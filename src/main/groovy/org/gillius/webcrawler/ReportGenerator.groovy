package org.gillius.webcrawler

import groovy.transform.CompileStatic
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceState

import java.util.function.Consumer

@CompileStatic
/**
 * Contains a few different methods to generate reports from {@link Resource}s.
 */
class ReportGenerator {
	private static final String eol = System.getProperty("line.separator")

	/**
	 * Generates list of HTML resource URLs suitable for use as a plain-text form of a sitemap to be used in a robots.txt.
	 */
	static String generateSitemapString(Resource root) {
		generateSitemap(root, new StringWriter()).toString()
	}

	/**
	 * Generates list of HTML resource URLs suitable for use as a plain-text form of a sitemap to be used in a robots.txt.
	 */
	static Writer generateSitemap(Resource root, Writer out) {
		visit(root) {
			if (it.state == ResourceState.Exists && it.html)
				out.println(it.url)
		}

		return out
	}

	/**
	 * Generates list of all URLs and some information about them.
	 */
	static String generateUrlsListString(Resource root) {
		generateUrlsList(root, new StringWriter()).toString()
	}

	/**
	 * Generates list of all URLs and some information about them.
	 */
	static Writer generateUrlsList(Resource root, Writer out) {
		visit(root) {
			out << it.selfString()
			if (it.links) {
				out << ", " << it.links.size() << " outgoing links"
			}
			out << eol
		}

		return out
	}

	private static void visit(Resource root, Consumer<Resource> consumer) {
		new UniqueResourceVisitor(consumer).visit(root)
	}

	private static class UniqueResourceVisitor {
		private final	Set<URL> seen = new HashSet<>()
		private final Consumer<Resource> consumer

		UniqueResourceVisitor(Consumer<Resource> consumer) {
			this.consumer = consumer
		}

		void visit(Resource res) {
			if (seen.add(res.url)) {
				consumer.accept(res)

				res.links?.each(this.&visit)
			}
		}
	}
}
