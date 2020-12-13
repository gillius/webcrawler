package org.gillius.webcrawler

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * A method to determine if a block of content might be HTML.
 *
 * I looked at a few various methods to do this and did not like them:
 * <ul>
 *   <li>Try parsing with JSoup and see if it failed. This doesn't work, because JSoup accepts <i>anything</i>. If it's
 *       a binary file, it will just pretend the document is an html doc with all of those bytes in its body (because it
 *       adds the "missing" html, head, and body tags).
 *   <li>Use {@link URLConnection#guessContentTypeFromStream(java.io.InputStream)}. Its code unfortunately looked like a
 *       snapshot in time from Java 1.0. It cannot detect HTML encoded in UTF-16 at all, or in UTF-8 if there's a BOM,
 *       which is allowed in HTML5.
 *   <li>Use a library like Apache Tika. Good solution but I didn't want to have a dependency on a tree of libraries
 *       for a simple tool.
 * </ul>
 *
 * So I make my own version. Note that JSoup that we use to parse the HTML handles the BOM properly, so this class
 * simply needs to perform the detection. This supports UTF-8, UTF-16 with BOM, but not UTF-32, but does support
 * character sets like ASCII, ISO-8859-1 which parse well enough to detect with UTF-8 assumption.
 */
class HtmlDetector {
	private static final int LEAD_BYTES = 1024

	static boolean isLikelyHtml(byte[] data) {
		isLikelyHtml(new ByteArrayInputStream(data))
	}

	/**
	 * Returns true if this input stream is likely HTML. Uses the mark and reset operations to ensure that the stream's
	 * position is not modified.
	 */
	static boolean isLikelyHtml(InputStream is) {
		assert is.markSupported()

		is.mark(LEAD_BYTES)
		byte[] data = is.readNBytes(LEAD_BYTES)
		is.reset()

		Charset charset
		int readStart

		if (data[0] == (byte) 0xFE && data[1] == (byte) 0xFF ||
				data[0] == (byte) 0xFF && data[1] == (byte) 0xFE) {
			charset = StandardCharsets.UTF_16
			readStart = 0 //Java UTF-16 decoder supports BOM

		} else if (data[0] == (byte) 0xEF && data[1] == (byte) 0xBB && data[2] == (byte) 0xBF) {
			charset = StandardCharsets.UTF_8
			readStart = 3 //skip the BOM

		} else {
			charset = StandardCharsets.UTF_8
			readStart = 0 //no BOM

		}

		char[] chars = new char[LEAD_BYTES/2]
		//we don't read in a loop because we rely on ByteArrayInputStream's behavior to return everything it can in one call.
		int numChars = new InputStreamReader(new ByteArrayInputStream(data), charset).read(chars)

		String maybeHtmlString = new String(chars, 0, numChars).toLowerCase()

		return maybeHtmlString.contains("<html") || maybeHtmlString.contains("<body")
	}
}
