package org.gillius.webcrawler.resourceloader

import groovy.transform.CompileStatic
import org.gillius.webcrawler.UrlUtil
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceError
import org.gillius.webcrawler.model.ResourceState
import org.gillius.webcrawler.parser.Parser

import java.nio.file.Files
import java.nio.file.Paths

/**
 * An implementation of FileResourceLoader that locates and parses resources for file protocol URLs.
 */
@CompileStatic
class FileResourceLoader implements ResourceLoader {
	/**
	 * The default maximum size (in bytes) of content that we will try to parse.
	 */
	public static final long DEFAULT_MAX_PARSE_SIZE = 2_000_000

	/**
	 * The maximum number of bytes this loader will try to parse.
	 */
	long maxParseSizeBytes = DEFAULT_MAX_PARSE_SIZE

	private final Parser parser

	/**
	 * Constructs a FileResourceLoader.
	 */
	FileResourceLoader(Parser parser) {
		this.parser = parser
	}

	@Override
	Resource loadResource(URL url) {
		try {
			def path = Paths.get(url.toURI())
			if (!Files.exists(path)) {
				return new Resource(
						url: url,
						state: ResourceState.Broken,
						title: UrlUtil.getTitleFromUrlPath(url),
						error: new ResourceError(404, "File not found")
				)
			}
			def fileSize = Files.size(path)
			if (fileSize > maxParseSizeBytes) {
				return new Resource(
						url: url,
						state: ResourceState.Exists,
						title: UrlUtil.getTitleFromUrlPath(url),
						error: new ResourceError(0, "File size $fileSize is too large to process; skipping check for links")
				)
			}

			return parser.parse(Files.newInputStream(path), url)

		} catch (e) {
			return new Resource(
					url: url,
					state: ResourceState.Error,
					title: UrlUtil.getTitleFromUrlPath(url),
					error: new ResourceError(0, e.toString())
			)
		}
	}
}
