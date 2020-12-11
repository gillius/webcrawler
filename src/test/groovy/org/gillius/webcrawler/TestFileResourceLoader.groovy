package org.gillius.webcrawler

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import groovy.transform.CompileStatic
import org.gillius.webcrawler.model.Resource
import org.gillius.webcrawler.model.ResourceError
import org.gillius.webcrawler.model.ResourceState
import org.gillius.webcrawler.parser.Parser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.FileSystem
import java.nio.file.Files

/**
 * Tests the {@link FileResourceLoader}.
 */
class TestFileResourceLoader {
	private static final Resource parsedResource = new Resource()

	private FileResourceLoader loader
	private FileSystem fs
	private Parser parser = {a, b -> parsedResource}

	@BeforeEach
	void setUp() {
		fs = Jimfs.newFileSystem(Configuration.unix())
		loader = new FileResourceLoader({ a, b -> parser.parse(a, b) }, fs)
	}

	@Test
	void "A file that does not exist results in a Broken Resource"() {
		def url = new URL("file:///doesnotexist")

		assert loader.loadResource(url) == new Resource(
				url: url,
				state: ResourceState.Broken,
				title: "doesnotexist",
				error: new ResourceError(404, "File not found")
		)
	}

	@Test
	void "A file that is very large is not treated as HTML"() {
		loader.maxParseSizeBytes = 3000
		Files.write(fs.getPath("/verylarge"), new byte[loader.maxParseSizeBytes + 1])

		def url = new URL("file:///verylarge")

		assert loader.loadResource(url) == new Resource(
				url: url,
				state: ResourceState.Exists,
				title: "verylarge",
				error: new ResourceError(0, "File size 3001 is too large to process; skipping check for links")
		)
	}

	@Test
	void "A file that exists and is not too large gets parsed"() {
		loader.maxParseSizeBytes = 3000
		Files.write(fs.getPath("/file"), new byte[loader.maxParseSizeBytes / 2])

		def url = new URL("file:///file")

		assert parsedResource.is(loader.loadResource(url))
	}

	@Test
	void "A file that fails to get parsed comes out as an error resource"() {
		loader.maxParseSizeBytes = 3000
		Files.write(fs.getPath("/file"), new byte[loader.maxParseSizeBytes / 2])

		def url = new URL("file:///file")
		parser = {a, b -> throw new IOException("I broke")}

		def res = loader.loadResource(url)

		assert res.state == ResourceState.Error &&
		       res.error.message.contains("I broke")
	}
}
