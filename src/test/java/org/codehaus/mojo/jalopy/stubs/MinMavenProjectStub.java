package org.codehaus.mojo.jalopy.stubs;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Build;
import org.apache.maven.model.Organization;
import org.codehaus.plexus.PlexusTestCase;

/**
 * Copied from the Checkstyle plugin.
 *
 * @author Edwin Punzalan
 */
public class MinMavenProjectStub extends org.apache.maven.plugin.testing.stubs.MavenProjectStub {
	public List getCompileClasspathElements() throws DependencyResolutionRequiredException {
		return Collections.singletonList(PlexusTestCase.getBasedir() + "/target/classes");
	}

	public File getBasedir() {
		return new File(PlexusTestCase.getBasedir());
	}

	public List getReportPlugins() {
		return Collections.EMPTY_LIST;
	}

	public Organization getOrganization() {
		Organization organization = new Organization();

		organization.setName("maven-plugin-tests");

		return organization;
	}

	public String getInceptionYear() {
		return "2006";
	}

	public Build getBuild() {
		Build build = new Build();

		build.setDirectory(PlexusTestCase.getBasedir() + "/target/test-classes");

		return build;
	}
}
