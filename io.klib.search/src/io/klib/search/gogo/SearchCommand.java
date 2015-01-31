package io.klib.search.gogo;

import io.klib.search.filesystem.SearchFilesystem;
import io.klib.search.filesystem.SearchOption;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(properties = { "osgi.command.scope=util", "osgi.command.function=find"

}, provide = SearchCommand.class)
public class SearchCommand {

    /**
     * All repositories, including children of composite repositories
     */
    @SuppressWarnings("unused")
    private BundleContext bc;
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
        Set<URI> files = search.find(rootPath, "", "");
        return format(files);
    }

    @Descriptor("list directory files recursively")
    public String searchDir(@Descriptor("root folder") final File rootPath,
            @Descriptor("recursive search") final boolean recursively) {
        Set<URI> files = new HashSet<URI>();
        if (recursively) {
            files = search.find(rootPath, "", "", SearchOption.RECURSE);
        } else {
            files = search.find(rootPath, "", "", (SearchOption[]) null);
        }
        return format(files);
    }

    @Descriptor("search directory for a specific filename")
    public String searchDir(@Descriptor("root folder") final File rootPath,
            @Descriptor("regex pattern for filename") final String filenamePattern,
            @Descriptor("regex pattern for content - can be null") final String contentPattern) {
        Set<URI> files = search.find(rootPath, filenamePattern, contentPattern);
        return format(files);
    }

    @Descriptor("search directory for a specific filename")
    public String searchDir(@Descriptor("root folder") final File rootPath,
            @Descriptor("regex pattern for filename") final String filenamePattern,
            @Descriptor("regex pattern for content - can be null") final String contentPattern,
            @Descriptor("recursive search") final boolean recursively) {
        Set<URI> files = new HashSet<URI>();
        if (recursively) {
            files = search.find(rootPath, filenamePattern, contentPattern, SearchOption.RECURSE);
        } else {
            files = search.find(rootPath, filenamePattern, contentPattern, (SearchOption[]) null);
        }
        return format(files);
    }

    @Descriptor("search directory for a specific filename")
    public String searchDir(@Descriptor("root folder") final File rootPath,
            @Descriptor("regex pattern for filename") final String filenamePattern,
            @Descriptor("regex pattern for content - can be null") final String contentPattern,
            @Descriptor("recursive search") final boolean recursively,
            @Descriptor("archive (zip,jar) search") final boolean insideArchives) {
        Set<URI> files = new HashSet<URI>();
        List<SearchOption> opts = new ArrayList<SearchOption>();
        if (recursively) {
            opts.add(SearchOption.RECURSE);
        }
        if (insideArchives) {
            opts.add(SearchOption.ARCHIVE);
        }
        files = search.find(rootPath, filenamePattern, contentPattern, opts.toArray(new SearchOption[opts.size()]));
        return format(files);
    }

    private String format(Set<URI> uris) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n### searchDir result ###\n");
        sb.append("file and/or content pattern matched for the following files:\n\n");
        for (URI uri : uris) {
            sb.append(uri.toString()).append("\n");
        }
        if (sb.length() == 0) {
            sb.append("nothing found");
        }
        return sb.toString();
    }
}
