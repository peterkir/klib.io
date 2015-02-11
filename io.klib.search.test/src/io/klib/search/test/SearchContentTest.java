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

public class SearchContentTest extends TestCase {

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

    /**
     * java.nio.file.AccessDeniedException due to try Files.readAllBytes(path);
     */
    public void testSearchForContentWithDirAsFilename() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        expected.add(testfile("/dirC"));
        expected.add(testfile("/dirC/dirC"));

        Set<URI> found = searchFS.find(TESTDIR, ".*dirC", null, SearchOption.RECURSE);
        assertEquals(expected, found);
    }

    public void testContentSearch() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        expected.add(testfile("file1.txt"));

        Set<URI> found = searchFS.find(TESTDIR, ".*\\.txt", ".*without.*");
        assertEquals(expected, found);
    }

    public void testContentSearchWithLinebreak() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        expected.add(testfile("/dirA/dirJ/fileWithWindowsNewLine.txt"));

        Set<URI> found = searchFS.find(TESTDIR, ".*\\.txt", ".*new\\r\\nline.*", SearchOption.RECURSE);
        assertEquals(expected, found);
    }

    private URI testfile(final String subpath) {
        return Paths.get(TESTDIR.getPath(), subpath).toUri();
    }

}
