# Web Crawler Project

This is a simple project that will scan all pages and content reachable by an HTTP/HTTPS or file URL. It will ignore
content on other domains.

![test](https://github.com/gillius/webcrawler/workflows/test/badge.svg?branch=main)

## License

This project is licensed under the terms of the "MIT License".

## Usage

In order to run tbe webcrawler, you need the following:

* JDK 11 or later on your path, or with JAVA_HOME pointing to the installation. You can download from
  [AdoptOpenJDK](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot).
* Internet connection to be able to download artifacts to build at https://repo1.maven.org/maven2/.

Usage output:

      -d, --maxDepth=PARAM     Maximum depth of links to traverse (default 10)
      -f, --file=PARAM         Load a site from a local file path
      -h, --help               Display usage
          -json                Output to JSON format instead of text format if
                                 supported
      -o, --outputFile=PARAM   Write output to specified file
          -pretty              When combined with -json, pretty-prints the output.
                                 Note JSON output is buffered in memory so do not
                                 use with huge outputs.
      -q, --quiet              Quiet mode: suppresses even the standard logging
                                 output showing the URLs being loaded
      -r, --report=PARAM       The report type (default urls):
                               raw     : Display pages and links in a tree
                                 (supports JSON)
                               sitemap : Output list of unique URLs suitable for
                                 use as plaintext sitemap
                               urls    : List of all URLs similar to sitemap but
                                 includes non-HTML resources
      -t, --threads=PARAM      The number of threads to use for processing (default
                                 1)
      -u, --url=PARAM          Load a site from a URL
      -v, --verbose            Includes extra debug logging output
    At least one of -f or -u options required.
    Output goes to stdout, unless -o specified; logs go to stderr

Example crawl included test site: `./webcrawler -f src/test/resources/simple-site/index.html`. This example works
on *nix shells or a Bash shell in Windows such as Git Bash shell.

You may also want to add `-r sitemap` for sitemap output, or `-r raw -json -pretty` for a listing of the full data model.

Windows systems: There is also a `webcrawler.bat` that works the same way.

*nix systems: If you are running on a *nix system where Bash is not installed, you can run the command through gradle
directly: `./gradlew run --args="-f whatever"`

Running from IDE: import the Gradle project in your favorite IDE such as [IntelliJ IDEA](https://www.jetbrains.com/idea/)
and run the WebCrawler class's main method.

### Detailed Usage Info

The purpose of the `maxDepth` or `-d` parameter is to limit how deep of links the crawler will traverse. This is
actually not to deal with cycles such as A -> B -> C -> A. That is handled via the caching mechanism that will not
download the same URL twice. Instead, it is to handle dynamic pages, such as a database listing with prev/next buttons
so maybe you start at `/people` then next button is to `/people?from=100` then `/people?from=200`, etc. going forever
or for a very long time. URLs that are not loaded due to this setting will show in the site graph as `(Unresolved)`.

You can speed up crawling by increasing the number of threads. But this will put extra load on the server. Browsers
typically load resources with 2x or 4x parallelism, so a similar setting would not be reasonable. If crawling a local
site or your own server where you are OK with the load, you can set the value higher. Depending on network latency,
going over 2x the number of CPU cores will produce decreasing returns as CPU becomes the bottleneck rather than
network I/O.

The following reports are available to be generated, the default being "raw":

* raw: Displays a dump of the `Resource` object hierarchy, which contains all pages and resources linked to by those
  pages.
* sitemap: Displays list of HTML resource URLs suitable for use as a plain-text form of a
  [Sitemap](https://en.wikipedia.org/wiki/Sitemaps) that can be referenced in a
  [robots.txt](https://en.wikipedia.org/wiki/Robots_exclusion_standard)
* urls: Like sitemap, but includes all non-HTML URLs. Excludes external URLs.

## Static Web Server

If you'd like to test the included simple site via the HTTP protocol, one technique that can be used, if you have
node.js installed, is to run:

    npm install -g static-server
    cd src/test/resources/simple-site/
    static-server

Then you will see the server start (by default) on http://localhost:9080, then you can run
`./webcrawler -u http://localhost:9080` to perform the same operation as the previous test run.

## Unit Tests

This project has many unit tests. Running `./gradlew test` will run those tests, or you can perform a similar
operation if you have the project open from an IDE such as IntelliJ IDEA. In IntelliJ you can right click the tests
folder and pick run.

The unit test are also run on every push and pull request via GitHub Actions and can be seen on the Actions tab in
this GitHub project.

## TODOs

* Hash content so that the same content at different pages can be detected as the same thing and we
  don't re-index it.
* Use a HEAD before GET in case the content is big or not HTML.
* Support a max-depth option.
* Simple static server for testing the actual sites with http URLs.
* Find out the tags that a bot like Google puts on requests that would encourage sites to return a
  server-side generated page for pages generated by JavaScript.
* Make sure that we have a way to limit the amount of data we process in the non-local case.
* Allow a limit on the maximum time to resolve a FutureResource
  
## Not In Scope -- Future Work
* Some way to support links that might be in scripts or generated by scripts? That would require running
  the full front-end code of the site, which we don't want to do.
* Parsing SVG to find links in them.
* Full evaluation to ensure no security vulnerabilities with JSoup exist when parsing malicious inputs. Given the
  sandbox nature of the JVM and limits on max processing size in JSoup, likely the worst scenario is some kind of
  CPU-based DoS, which has limited impact as this is intended to be run as an interactive CLI application.
* Loose comparison of domains when detecting external links, such as links to example.com from www.example.com
  (alias domains), or links to images.example.com from example.com (CDN domains)
