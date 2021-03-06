package org.codehaus.mojo.jalopy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Performs Locator services for the <code>*Location</code> parameters in the Reports. <p> Copied from the maven-checkstyle-plugin.
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public class Locator {
	private Log log;
	private File localDir;

	/**
	 * Create a Locator object.
	 *
	 * @param log the logger object to log with.
	 * @param resolveToDir the directory to resolve resources into.
	 */
	public Locator(Log log, File resolveToDir) {
		this.log = log;
		this.localDir = resolveToDir;
	}

	/**
	 * Obtain a Log object.
	 *
	 * @return the Log object.
	 */
	private Log getLog() {
		if (this.log == null) {
			this.log = new SystemStreamLog();
		}
		return this.log;
	}

	/**
	 * <p> Attempts to resolve a location parameter into a real file. </p>
	 *
	 * <p/> Checks a location string to for a resource, URL, or File that matches. If a resource or URL is found, then a local file is created with that locations contents. </p>
	 *
	 * @param location the location string to match against.
	 * @param localfile the local file to use in case of resource or URL.
	 * @return the File of the resolved location.
	 * @throws IOException if file is unable to be found or copied into <code>localfile</code> destination.
	 */
	public File resolveLocation(String location, String localfile) throws IOException {
		getLog().debug("resolveLocation(" + location + ", " + localfile + ")");
		if (StringUtils.isEmpty(location)) {
			return null;
		}

		File retFile = new File(localDir, localfile);

		// Attempt a URL
		if (location.indexOf("://") > 1) {
			// Found a URL
			URL url = new URL(location);
			getLog().debug("Potential URL: " + url.toExternalForm());
			FileUtils.copyURLToFile(url, retFile);
		} else {
			getLog().debug("Location is not a URL.");
			// Attempt a file
			File fileLocation = new File(location);
			if (fileLocation.exists()) {
				// Found a file
				getLog().debug("Potential File: " + fileLocation.getAbsolutePath());
				FileUtils.copyFile(fileLocation, retFile);
			} else {
				getLog().debug("Location is not a File.");
				// Attempt a resource
				URL url = this.getClass().getClassLoader().getResource(location);
				if (url != null) {
					// Found a resource
					getLog().debug("Potential Resource: " + url.toExternalForm());
					FileUtils.copyURLToFile(url, retFile);
				} else {
					getLog().debug("Location is not a Resource.");
					throw new IOException("Unable to find location '" + location + "' as URL, File or Resource. Working dir is " + new File("").getAbsolutePath());
				}
			}
		}

		if (!retFile.exists()) {
			throw new FileNotFoundException("Destination file does not exist.");
		}

		if (retFile.length() <= 0) {
			throw new IOException("Destination file has no content: " + retFile + ". Is " + location + " empty?");
		}

		return retFile;
	}
}
