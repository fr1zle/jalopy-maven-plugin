package org.codehaus.mojo.jalopy;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import org.codehaus.plexus.PlexusTestCase;

/**
 * Copied from checkstyle plugin along with the tested class
 *
 * @author Edwin Punzalan
 */
public class LocatorTest extends TestCase {
	Locator locator;
	File testDir = new File(PlexusTestCase.getBasedir(), "target/unit-test/Locator");

	protected void setUp() throws Exception {
		locator = new Locator(null, testDir);
	}

	public void testEmptyLocation() throws Exception {
		assertNull("Test null location", locator.resolveLocation(null, ""));
		assertNull("Test empty location", locator.resolveLocation("", ""));
	}

	public void testURLs() throws Exception {
		String basedir = PlexusTestCase.getBasedir();
		File resolvedFile = locator.resolveLocation("file:///" + basedir + "/target/classes/jalopy.xml", "jalopy.xml");

		assertNotNull("Test resolved file", resolvedFile);
		assertTrue("Test resolved file exists", resolvedFile.exists());
	}

	public void testLocalFile() throws Exception {
		String basedir = PlexusTestCase.getBasedir();
		File resolvedFile = locator.resolveLocation(basedir + "/target/classes/jalopy.xml", "jalopy.xml");

		assertNotNull("Test resolved file", resolvedFile);
		assertTrue("Test resolved file exists", resolvedFile.exists());
	}

	public void testResource() throws Exception {
		File resolvedFile = locator.resolveLocation("META-INF/plexus/components.xml", "components.xml");

		assertNotNull("Test resolved file", resolvedFile);
		assertTrue("Test resolved file exists", resolvedFile.exists());
	}

	public void testException() {
		try {
			locator.resolveLocation("edwin/punzalan", "exception");

			fail("Expected IOException not thrown");
		} catch (IOException e) {
			// Expected
		}
	}
}
