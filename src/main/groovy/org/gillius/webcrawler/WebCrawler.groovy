package org.gillius.webcrawler

import groovy.cli.picocli.CliBuilder
import groovy.json.JsonOutput
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceSerializer
import org.gillius.webcrawler.parser.AutodetectParser
import org.gillius.webcrawler.parser.JsoupHtmlParser
import org.gillius.webcrawler.resourceloader.FileResourceLoader
import org.gillius.webcrawler.resourceloader.ResolvingResourceLoader

/**
 * Entrypoint into the web crawler functionality
 */
class WebCrawler {
	static void main(String[] args) {
		def cli = new CliBuilder(usage: './webcrawler <options>',
		                         footer: "At least one of -f or -u options required.\n" +
		                                 "Output goes to stdout, unless -o specified; logs go to stderr")
		cli.h(longOpt: "help", "Display usage")
		cli.f(longOpt: "file", args: 1, "Load a site from a local file path")
		cli.u(longOpt: "url", args: 1, "Load a site from a URL")
		cli.json("Output to JSON format instead of text format")
		cli.pretty("When combined with -json, pretty-prints the output. Note JSON output is " +
		           "buffered in memory so do not use with huge outputs.")
		cli.o(longOpt: "outputFile", args: 1, "Write output to specified file")

		def options = cli.parse(args)

		if (options.u) {
			throw new UnsupportedOperationException("-u option not yet supported")
		}

		if (options.h || (!options.f && !options.u)) {
			cli.usage()
			System.exit(options.h ? 0 : 1)
		}

		def url
		if (options.f) {
			url = new File(options.f).toURI().toURL()
		} else {
			url = new URL(options.u)
		}

		def res = crawl(url)

		OutputStreamWriter out
		if (options.o) {
			out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(options.o)))
		} else {
			out = new OutputStreamWriter(System.out)
		}

		try {
			if (options.json && !options.pretty) {
				out.println JsonOutput.prettyPrint(ResourceSerializer.toJsonString(res))
			} else if (options.json) {
				ResourceSerializer.toJson(res, out)
			} else {
				ResourceSerializer.toTextTree(res, out).close()
			}
		} finally {
			out.close()
		}
	}

	static Resource crawl(URL url) {
		new ResolvingResourceLoader(
				new FileResourceLoader(
						new AutodetectParser(
								new JsoupHtmlParser()
						)
				)
		).loadResource(url)
	}
}
