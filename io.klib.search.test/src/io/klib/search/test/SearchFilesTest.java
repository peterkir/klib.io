package io.klib.search.test;

import io.klib.search.filesystem.SearchFilesystem;
import io.klib.search.filesystem.SearchOption;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class SearchFilesTest extends TestCase {

	// @formatter:off
	private SearchFilesystem searchFS;
	private File TESTDIR;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		BundleContext ctx = FrameworkUtil.getBundle(TestdataSetupBundle.class).getBundleContext();
		TESTDIR = ctx.getDataFile(TestdataSetupBundle.TESTDATA);
		assertTrue(TESTDIR.exists());

		ServiceReference ref = ctx.getServiceReference(SearchFilesystem.class.getName());
		assertNotNull(ref);
		searchFS = (SearchFilesystem) ctx.getService(ref);
		assertNotNull(searchFS);
	}

	public void testFlatFileSearch() throws Exception {
		Set<URI> expected = new HashSet<URI>();
		expected.add(testfile("file1.txt"));

		Set<URI> found = searchFS.find(TESTDIR, ".*\\.txt", null, (SearchOption[]) null);
		assertEquals(expected, found);
	}

	public void testSearchForForFileWithoutExtension() throws Exception {
		Set<URI> expected = new HashSet<URI>();
		expected.add(testfile("/fileWithOutExtension"));
		expected.add(testfile("/dirC/fileWithOutExtension"));

		Set<URI> found = searchFS.find(TESTDIR, ".*fileWithOutExtension", null, SearchOption.RECURSE);
		assertEquals(expected, found);
	}

	public void testRecursiveFileSearch() throws Exception {
		Set<URI> expected = new HashSet<URI>();
        expected.addAll(testfiles(new String[] { 
        		"file1.txt"
        		, "/dirA/dirJ/file1.txt"
        		, "/dirA/dirJ/file2.txt"
        		, "/dirA/dirJ/fileWithWindowsNewLine.txt"
        		, "/dirA/dirJ/fileWithUnixNewLine.txt"
        		, "/dirA/dirK/file1.txt"
        		, "/dirB/file2.txt" 
        }));

		Set<URI> found = searchFS.find(TESTDIR, ".*\\.txt", null, SearchOption.RECURSE);
		assertEquals(expected, found);
	}

	/**
	 * find everything but file "/dirA/dirJ/file2.txt"
	 */
	public void testRecursiveNegatedFileSearch() throws Exception {
		Set<URI> expected = new HashSet<URI>();
        expected.addAll(testfiles(new String[] { 
        		"/dirA/"
        		, "/dirA/dirJ"
        		, "/dirA/dirJ/.gitattributes"
        		, "/dirA/dirJ/file1.txt"
        		, "/dirA/dirJ/fileWithWindowsNewLine.txt"
        		, "/dirA/dirJ/fileWithUnixNewLine.txt"
        		, "/dirA/dirK/"
        		, "/dirA/dirK/file1.txt"
        		, "/dirB/"
        		, "/dirB/file2.txt"
        		, "/dirC/"
        		, "/dirC/dirC"
        		, "/dirD/"
        		, "dirD/.gitignore"
        		, "/dirC/fileWithOutExtension"
        		, "/dirC/osgi.core-4.0.1.jar"
        		, "/dirC/zip_dirB.zip" 
        		, "fileWithOutExtension"
        		, "file1.txt"
        		, "zip_dirA.zip"
        		, "zip_dirB.zip"
        }));

		Set<URI> found = searchFS.find(TESTDIR, "^(?!(.*/dirA/dirJ/file2.txt)).+$", null, SearchOption.RECURSE);
		assertEquals(expected, found);
	}

	private URI testfile(final String subpath) {
		return Paths.get(TESTDIR.getPath(), subpath).toUri();
	}

	private Set<URI> testfiles(final String[] subpaths) {
		final Set<URI> result = new HashSet<>();
		for (final String subpath : subpaths) {
			result.add(testfile(subpath));
		}
		return result;
	}
	//@formatter:off
}
