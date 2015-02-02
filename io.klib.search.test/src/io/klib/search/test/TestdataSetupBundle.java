package io.klib.search.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TestdataSetupBundle implements BundleActivator {

    public static final String TESTDATA = "testdata/";
    private BundleContext ctx;

    @Override
    public void start(BundleContext context) throws Exception {
        this.ctx = context;
        Bundle bundle = context.getBundle();

        // extract all files
        String testdataFiles = (String) bundle.getHeaders().get("Testdata-Files");
        if (testdataFiles != null) {
            String[] res = testdataFiles.split(",");
            for (int i = 0; i < res.length; i++) {
                URL entry = bundle.getEntry(TESTDATA.concat(res[i]));
                extract(entry);
            }
        }
        // create all folders (necessary to get empty folders as well
        String testdataFolder = (String) bundle.getHeaders().get("Testdata-Folder");
        if (testdataFolder != null) {
            String[] res = testdataFolder.split(",");
            for (int i = 0; i < res.length; i++) {
                File targetFile = ctx.getDataFile(TESTDATA.concat(res[i]));
                if (!targetFile.exists()) {
                    targetFile.mkdirs();
                }
            }
        }
    }

    private void extract(URL entryURL) throws IOException {
        File targetFile = ctx.getDataFile(entryURL.getPath());
        targetFile.getParentFile().mkdirs();
        Files.copy(entryURL.openStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub
    }

    public File getTestDataLocation() {
        return ctx.getDataFile(TESTDATA);
    }

}
