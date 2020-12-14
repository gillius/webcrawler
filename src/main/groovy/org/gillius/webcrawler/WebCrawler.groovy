package org.gillius.webcrawler

import groovy.cli.picocli.CliBuilder
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceSerializer
import org.gillius.webcrawler.parser.AutodetectParser
import org.gillius.webcrawler.parser.JsoupHtmlParser
import org.gillius.webcrawler.resourceloader.FileResourceLoader
import org.gillius.webcrawler.resourceloader.HttpResourceLoader
import org.gillius.webcrawler.resourceloader.ImmediateExecutorService
import org.gillius.webcrawler.resourceloader.ResolvingResourceLoader
import org.slf4j.impl.SimpleLogger

import java.util.concurrent.Executors

/**
 * Entrypoint into the web crawler functionality
 */
class WebCrawler {
	static void main(String[] args) {
		System.setProperty(SimpleLogger.SHOW_LOG_NAME_KEY, "false")
		System.setProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true")

		def cli = new CliBuilder(usage: './webcrawler <options>',
		                         footer: "At least one of -f or -u options required.\n" +
		                                 "Output goes to stdout, unless -o specified; logs go to stderr")
		cli.h(longOpt: "help", "Display usage")
		cli.f(longOpt: "file", args: 1, "Load a site from a local file path")
		cli.u(longOpt: "url", args: 1, "Load a site from a URL")
		cli.json("Output to JSON format instead of text format if supported")
		cli.pretty("When combined with -json, pretty-prints the output. Note JSON output is " +
		           "buffered in memory so do not use with huge outputs.")
		cli.t(longOpt: "threads", args: 1, defaultValue: "1", type: Integer, "The number of threads to use for processing (default 1)")
		cli.d(longOpt: "maxDepth", args: 1, defaultValue: "10", type: Integer, "Maximum depth of links to traverse (default 10)")
		cli.o(longOpt: "outputFile", args: 1, "Write output to specified file")
		cli.v(longOpt: "verbose", "Includes extra debug logging output")
		cli.q(longOpt: "quiet", "Quiet mode: suppresses even the standard logging output showing the URLs being loaded")
		cli.r(longOpt: "report", args: 1, defaultValue: "urls",
		      "The report type (default raw):\n" +
		      "raw     : Display pages and links in a tree (supports JSON)\n" +
		      "sitemap : Output list of unique URLs suitable for use as plaintext sitemap\n" +
		      "urls    : List of all URLs similar to sitemap but includes non-HTML resources and external links"
		)

		def options = cli.parse(args)

		if (options.q) {
			System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "off")
		} else if (options.v) {
			System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug")
		}

		if (options.h || (!options.f && !options.u)) {
			cli.usage()
			System.exit(options.h ? 0 : 1)
		}

		if (options.json && options.report != "raw") {
			System.err.println("-json can only be used with raw report format currently")
			cli.usage()
			System.exit(1)
		}

		def url
		if (options.f) {
			url = new File(options.f).toURI().toURL()
		} else {
			url = new URL(options.u)
		}

		def res = crawl(url, new Options(numThreads: options.t, maxDepth: options.d))

		OutputStreamWriter out
		if (options.o) {
			out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(options.o)))
		} else {
			out = new OutputStreamWriter(System.out)
		}

		try {
			switch (options.report) {
				case "raw":
					if (options.json && options.pretty) {
						out.println JsonOutput.prettyPrint(ResourceSerializer.toJsonString(res))
					} else if (options.json) {
						ResourceSerializer.toJson(res, out)
					} else {
						ResourceSerializer.toTextTree(res, out).close()
					}
					break

				case "sitemap":
					ReportGenerator.generateSitemap(res, out).close()
					break

				case "urls":
					ReportGenerator.generateUrlsList(res, out).close()
					break

				case "all_urls":
					break
			}
		} finally {
			out.close()
		}
	}

	/**
	 * Main entry point to the WebCrawler functionality.
	 *
	 * @param url     Root URL to start crawling
	 * @param options Options for the crawl process.
	 */
	@CompileStatic
	static Resource crawl(URL url, Options options) {
		def threadPool = options.numThreads == 1 ? new ImmediateExecutorService() : Executors.newFixedThreadPool(options.numThreads)

		try {
			def commonParser = new AutodetectParser(new JsoupHtmlParser())

			def protocolSpecificParser
			def protocol = url.protocol
			if (protocol in ["http", "https"])
				protocolSpecificParser = new HttpResourceLoader(commonParser)
			else if (protocol == "file")
				protocolSpecificParser = new FileResourceLoader(commonParser)
			else
				throw new IllegalArgumentException("Unknown URL protocol " + protocol)

			return new ResolvingResourceLoader(protocolSpecificParser, options.maxDepth, threadPool)
					.loadResource(url)
		} finally {
			threadPool.shutdown()
		}
	}

	/**
	 * Defines options controlling the crawl process. Any options added in the future will have backwards-compatible
	 * default settings. The default constructor creates an Options will all default settings.
	 */
	@CompileStatic
	static class Options {
		/**
		 * The number of threads used to load resources. Effectively defines the number of concurrently open connections.
		 */
		int numThreads = 1

		/**
		 * The maximum number of link depth of resources to load fully. Meant to handle dynamically generated or infinite
		 * sequences of pages.
		 */
		int maxDepth = 10
	}
}
