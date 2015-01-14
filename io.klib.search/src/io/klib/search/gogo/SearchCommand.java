package io.klib.search.gogo;

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import io.klib.search.filesystem.SearchFilesystem;

@Component(properties = {
        "osgi.command.scope=util", "osgi.command.function=searchDir"

}, provide = SearchCommand.class)
public class SearchCommand {

    /**
     * All repositories, including children of composite repositories
     */
    @SuppressWarnings("unused")
    private BundleContext    bc;
    private SearchFilesystem search;

    @Reference
    void setSearchFilesystem(SearchFilesystem search) {
        this.search = search;
    }

    @Activate
    void activate() {
        bc = FrameworkUtil.getBundle(SearchCommand.class).getBundleContext();
    }

    @Descriptor("list directory files")
    public String searchDir(@Descriptor("root folder") final File rootPath) {
        Set<URI> files = search.searchDir(rootPath, "", "");
        return format(files);
    }

    @Descriptor("list directory files")
    public String searchDir(@Descriptor("root folder") final File rootPath,
                            @Descriptor("recursive search") final boolean recursively) {
        Set<URI> files = search.searchDir(rootPath, "", "", recursively);
        return format(files);
    }

    @Descriptor("search directory for a specific filename")
    public String searchDir(@Descriptor("root folder") final File rootPath,
                            @Descriptor("regex pattern for filename") final String filenamePattern,
                            @Descriptor("regex pattern for content - can be null") final String contentPattern) {
        Set<URI> files = search.searchDir(rootPath, filenamePattern, contentPattern);
        return format(files);
    }

    @Descriptor("search directory for a specific filename")
    public String searchDir(@Descriptor("root folder") final File rootPath,
                            @Descriptor("regex pattern for filename") final String filenamePattern,
                            @Descriptor("regex pattern for content - can be null") final String contentPattern,
                            @Descriptor("recursive search") final boolean recursively) {
        Set<URI> files = search.searchDir(rootPath, filenamePattern, contentPattern, recursively);
        return format(files);
    }

    @Descriptor("search directory for a specific filename")
    public String searchDir(@Descriptor("root folder") final File rootPath,
                            @Descriptor("regex pattern for filename") final String filenamePattern,
                            @Descriptor("regex pattern for content - can be null") final String contentPattern,
                            @Descriptor("recursive search") final boolean recursively,
                            @Descriptor("archive (zip,gzip,jar) search") final boolean insideArchives) {
        Set<URI> files = search.searchDir(rootPath, filenamePattern, contentPattern, recursively, insideArchives);
        return format(files);
    }

    private String format(Set<URI> uris) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n### searchDir result ###\n");
        sb.append("file and/or content pattern matched for the following files:\n\n");
        for (URI uri: uris) {
            sb.append(uri.toString()).append("\n");
        }
        if (sb.length() == 0) {
            sb.append("nothing found");
        }
        return sb.toString();
    }
}
