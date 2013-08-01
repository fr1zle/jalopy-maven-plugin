package org.codehaus.mojo.jalopy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.hunsicker.jalopy.Jalopy;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.History;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;

/**
 * Format the java source files following a coding convention.
 * @author jruiz@exist.com
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
@Mojo(name = "format")
public class JalopyMojo extends AbstractMojo {
	/**
	 * Sets the file format of the output files.  The file format controls what end of line character is used.  Either one of "UNIX", "DOS", "MAC", "DEFAULT" or "AUTO" can be used. The values are case
	 * insensitive.
	 */
	@Parameter
	private String fileFormat;
	/**
	 * Specifies the history policy to use. Either one of "COMMENT", "FILE" or "NONE" can be used.
	 */
	@Parameter
	private String history;
	/**
	 * Indicates whether a run should be held if errors occurred.
	 */
	@Parameter
	private boolean failOnError;
	@Parameter(property = "project.build.sourceDirectory")
	private File sourceDirectory;
	/**
	 * The encoding of the source files to reformat.
	 */
	private String encoding;
	@Parameter(property = "project.build.testSourceDirectory")
	private File testSourceDirectory;
	/**
	 * Sets the preferences file to use - given either relative to the project's basedir or as an absolute local path or internet address. If omitted, the current preferences are used, if available.
	 * Otherwise the Jalopy built-in defaults will be used. Defaults to "${basedir}/src/main/resources/jalopy.xml".
	 */
	@Parameter
	private String convention;
	/**
	 * For Source Directory. Specifies filesets defining which source files to format. This is a comma- or space-separated list of patterns of files.
	 */
	@Parameter
	private String srcIncludesPattern;
	/**
	 * For Source Directory. Specifies filesets defining which source files <b>not</b> to format. This is a comma- or space-separated list of patterns of files. Default value is
	 * <code>**\*.exc</code>.
	 */
	@Parameter
	private String srcExcludesPattern;
	/**
	 * For Test Directory. Specifies filesets defining which test source files to format. This is a comma- or space-separated list of patterns of files.
	 */
	@Parameter
	private String testIncludesPattern;
	/**
	 * For Test Directory. Specifies filesets defining which test source files <b>not</b> to format. This is a comma- or space-separated list of patterns of files. Default value is
	 * <code>**\\*.exc</code>.
	 */
	@Parameter
	private String testExcludesPattern;
	@Parameter(property = "project")
	private MavenProject project;
	private Locator locator;

	public void execute() throws MojoExecutionException {
		try {
			locator = new Locator(getLog(), new File(project.getBuild().getDirectory()));

			if (getSourceDirectory().exists()) {
				String[] filesToFormat = getIncludedFiles(getSourceDirectory(), getSrcIncludesPattern(), getSrcExcludesPattern());

				formatFiles(getSourceDirectory(), filesToFormat);
			}

			if (getTestSourceDirectory().exists()) {
				String[] filesToFormat = getIncludedFiles(getTestSourceDirectory(), getTestIncludesPattern(), getTestExcludesPattern());

				formatFiles(getTestSourceDirectory(), filesToFormat);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String[] getIncludedFiles(File directory, String includes, String excludes) {
		DirectoryScanner scanner = new DirectoryScanner();

		scanner.setBasedir(directory);

		scanner.setIncludes(StringUtils.split(includes, ","));

		scanner.setExcludes(StringUtils.split(excludes, ","));

		scanner.scan();

		String[] filesToFormat = scanner.getIncludedFiles();

		return filesToFormat;
	}

	private void formatFiles(File directory, String[] filesToFormat) throws FileNotFoundException, MojoExecutionException {
		Jalopy jalopy = new Jalopy();

		jalopy = createJalopy(jalopy);

		for (int i = 0; i < filesToFormat.length; i++) {
			File currentFile = new File(directory, filesToFormat[i]);

			jalopy.setInput(currentFile);

			jalopy.setOutput(currentFile);

			jalopy.format();

			logMessage(jalopy, currentFile);
		}
	}

	private void logMessage(Jalopy jalopy, File currentFile) throws MojoExecutionException {
		Log log = getLog();

		if (jalopy.getState() == Jalopy.State.OK) {
			log.info(currentFile + " formatted correctly.");
		} else if (jalopy.getState() == Jalopy.State.WARN) {
			log.warn(currentFile + " formatted with warnings.");
		} else if (jalopy.getState() == Jalopy.State.ERROR) {
			log.error(currentFile + " could not be formatted.");

			if (isFailOnError()) {
				throw new MojoExecutionException(currentFile + " could not be formatted.");
			}
		} else {
			log.info(currentFile + " formatted with unknown state.");
		}
	}

	private Jalopy createJalopy(Jalopy jalopy) {
		Log log = getLog();

		try {
			if (convention != null) {
				File conventionContents = locator.resolveLocation(convention, "jalopy-convention.xml");

				Jalopy.setConvention(conventionContents);
			} else {
				log.info("Using default convention : jalopy.xml");

				InputStream in = this.getClass().getClassLoader().getResourceAsStream("jalopy.xml");

				Convention.importSettings(in, Convention.EXTENSION_XML);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		Convention settings = Convention.getInstance();

		jalopy.setFileFormat(getFileFormat());

		jalopy.setInspect(settings.getBoolean(ConventionKeys.INSPECTOR, ConventionDefaults.INSPECTOR));

		if (!getHistory().equalsIgnoreCase("none")) {
			jalopy.setHistoryPolicy(History.Policy.valueOf(getHistory()));
		}

		History.Method historyMethod = History.Method.valueOf(settings.get(ConventionKeys.HISTORY_METHOD, ConventionDefaults.HISTORY_METHOD));

		jalopy.setHistoryMethod(historyMethod);

		jalopy.setBackup(settings.getInt(ConventionKeys.BACKUP_LEVEL, ConventionDefaults.BACKUP_LEVEL) > 0);

		jalopy.setForce(settings.getBoolean(ConventionKeys.FORCE_FORMATTING, ConventionDefaults.FORCE_FORMATTING));

		jalopy.setEncoding(encoding);

		return jalopy;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public boolean isFailOnError() {
		return failOnError;
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public String getSrcIncludesPattern() {
		return srcIncludesPattern;
	}

	public void setSrcIncludesPattern(String srcIncludesPattern) {
		this.srcIncludesPattern = srcIncludesPattern;
	}

	public String getSrcExcludesPattern() {
		return srcExcludesPattern;
	}

	public void setSrcExcludesPattern(String srcExcludesPattern) {
		this.srcExcludesPattern = srcExcludesPattern;
	}

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public File getTestSourceDirectory() {
		return testSourceDirectory;
	}

	public void setTestSourceDirectory(File testSourceDirectory) {
		this.testSourceDirectory = testSourceDirectory;
	}

	public String getTestIncludesPattern() {
		return testIncludesPattern;
	}

	public void setTestIncludesPattern(String testIncludesPattern) {
		this.testIncludesPattern = testIncludesPattern;
	}

	public String getTestExcludesPattern() {
		return testExcludesPattern;
	}

	public void setTestExcludesPattern(String testExcludesPattern) {
		this.testExcludesPattern = testExcludesPattern;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}
}
