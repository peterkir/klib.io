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

    private static final String EMPTY = "<<EMPTY>>";
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

                String dataFile = res[i].trim();
                if (dataFile.contains(EMPTY)) {
                    File dataDir = ctx.getDataFile(dataFile.replace(EMPTY, ""));
                    dataDir.mkdirs();
                } else {
                    extract(dataFile);
                }
            }
        }
    }

    private void extract(String dataFile) throws IOException {
        URL entryURL = ctx.getBundle().getEntry(dataFile);
        File targetFile = ctx.getDataFile(dataFile);
        targetFile.mkdirs();
        Files.copy(entryURL.openStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub
    }

}
