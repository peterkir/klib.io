package io.klib.search.test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestdataSetupBundle implements BundleActivator {

    public static final String TESTDATA = "testdata";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private BundleContext ctx;

    @Override
    public void start(BundleContext context) throws Exception {
        this.ctx = context;
        Bundle bundle = context.getBundle();

        // copy test data
        @SuppressWarnings("unchecked")
        Enumeration<URL> entries = bundle.findEntries(TESTDATA, "*.*", true);
        if (entries == null) {
            logger.error("No testdata folder found inside bundle!");
        } else {
            while (entries.hasMoreElements()) {
                URL entryURL = entries.nextElement();
                File targetFile = context.getDataFile(entryURL.getPath());
                targetFile.getParentFile().mkdirs();
                Files.copy(entryURL.openStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub
    }

    public File getTestDataLocation() {
        return ctx.getDataFile(TESTDATA);
    }

}
