package de.klib.search.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.klib.search.filesystem.SearchFilesystem;

public class FilesystemSearchTest extends TestCase {

    private SearchFilesystem searchFS;
    private File             TESTDIR;

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

        Set<URI> found = searchFS.searchDir(TESTDIR, ".*\\.txt", null, null);
        assertEquals(expected, found);
    }

    public void testRecursiveFileSearch() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        ////@formatter:off
        expected.addAll(testfiles(new String[] {
                                                "file1.txt",
                                                "/dirA/dirJ/file1.txt",
                                                "/dirA/dirJ/file2.txt",
                                                "/dirA/dirJ/file3.txt",
                                                "/dirA/dirK/file1.txt",
                                                "/dirB/file2.txt"
        }));
        ////@formatter:on

        Set<URI> found = searchFS.searchDir(TESTDIR, ".*\\.txt", null, true);
        assertEquals(expected, found);
    }

    public void testArchiveFileSearch() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        ////@formatter:off
        expected.addAll(testfiles(new String[] {
                                                "file1.txt",
                                                "/dirA/dirJ/file1.txt",
                                                "/dirA/dirK/file1.txt"
        }));
        expected.addAll(jarfiles(new String[] {
                                               "zip_dirA.zip!/dirA/dirJ/file1.txt",
                                               "zip_dirA.zip!/dirA/dirK/file1.txt",
        }));
        ////@formatter:on

        Set<URI> found = searchFS.searchDir(TESTDIR, ".*file1.txt", null, true, true);
        assertEquals(expected, found);
    }

    public void testArchiveContentSearch() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        ////@formatter:off
        expected.add(testfile("/dirA/dirJ/file3.txt"));
        expected.add(jarfile("zip_dirA.zip!/dirA/dirJ/file3.txt"));
        ////@formatter:on

        Set<URI> found = searchFS.searchDir(TESTDIR, ".*\\.txt", ".*new\\r\\nline.*", true, true);
        assertEquals(expected, found);
    }

    public void testArchiveContentDotAllSearch() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        ////@formatter:off
        expected.add(jarfile("dirC/osgi.core-4.0.1.jar!/META-INF/MANIFEST.MF"));
        ////@formatter:on

        // searching for text wrapped "org.osgi.service.startlevel" from MANIFEST.MF
        Set<URI> found = searchFS.searchDir(TESTDIR,
                                            ".*MANIFEST\\.MF",
                                            ".*o\\s*r\\s*g\\s*\\.\\s*o\\s*s\\s*g\\s*i\\s*\\.\\s*s\\s*e\\s*r\\s*v\\s*i\\s*c\\s*e\\s*\\.\\s*s\\s*t\\s*a\\s*r\\s*t\\s*l\\s*e\\s*v\\s*e\\s*l.*",
                                            true,
                                            true);
        assertEquals(expected, found);
    }

    public void testContent() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        expected.add(testfile("file1.txt"));

        Set<URI> found = searchFS.searchDir(TESTDIR, ".*\\.txt", ".*without.*");
        assertEquals(expected, found);
    }

    public void testContentLinebreak() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        expected.add(testfile("/dirA/dirJ/file3.txt"));

        Set<URI> found = searchFS.searchDir(TESTDIR, ".*\\.txt", ".*new\\r\\nline.*", true);
        assertEquals(expected, found);
    }

    public void testRecursiveNegatedFileSearch() throws Exception {
        Set<URI> expected = new HashSet<URI>();
        ////@formatter:off
        expected.addAll(testfiles(new String[] {
                                                "file1.txt",
                                                "zip_dirA.zip",
                                                "zip_dirB.zip",
                                                "/dirA/",
                                                "/dirA/dirJ",
                                                "/dirA/dirJ/file1.txt",
                                                "/dirA/dirJ/file3.txt",
                                                "/dirA/dirK/",
                                                "/dirA/dirK/file1.txt",
                                                "/dirB/",
                                                "/dirB/file2.txt",
                                                "/dirC/",
                                                "/dirC/osgi.core-4.0.1.jar",
                                                "/dirC/zip_dirB.zip"
        }));
        ////@formatter:on

        Set<URI> found = searchFS.searchDir(TESTDIR, "^(?!(.*/dirA/dirJ/file2.txt)).+$", null, true);
        assertEquals(expected, found);
    }
    
    private URI testfile(String subpath) {
        return Paths.get(TESTDIR.getPath(), subpath).toUri();
    }

    private Set<URI> testfiles(String[] subpaths) {
        Set<URI> result = new HashSet<>();
        for (String subpath: subpaths) {
            result.add(testfile(subpath));
        }
        return result;
    }

    private URI jarfile(String subpath) {
        URI result = Paths.get(TESTDIR.getPath()).toUri();
        try {
            result = new URI("jar:" + result.toURL() + subpath);
        }
        catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    private Set<URI> jarfiles(String[] subpaths) {
        Set<URI> result = new HashSet<>();
        for (String subpath: subpaths) {
            if (jarfile(subpath) != null) {
                result.add(jarfile(subpath));
            }
        }
        return result;
    }
}
