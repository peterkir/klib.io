package io.klib.search.filesystem.impl;

import io.klib.search.filesystem.SearchFilesystem;
import io.klib.search.filesystem.SearchOption;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;

@Component(provide = SearchFilesystem.class)
public class SearchFilesystemImpl implements SearchFilesystem {

    private final Pattern ALLFILE_PATTERN = Pattern.compile(".*");
    private Pattern filenameRegExPattern = ALLFILE_PATTERN;
    private Pattern contentRegExPattern = null;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Set<URI> collectFiles(final File folder, final Pattern filenamePattern, boolean recurse) {

        Set<URI> foundFiles = new HashSet<URI>();
        Set<File> files = new HashSet<File>();
        try {
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(folder.toPath());
            for (Path path : dirStream) {
                files.add(path.toFile());
            }
        } catch (IOException e) {
            logger.error("error in collecting recursively children of directory " + folder, e);
        }
        File[] list = files.toArray(new File[files.size()]);

        if (list != null) {
            for (File f : list) {
                if (recurse) {
                    if (f.isDirectory()) {
                        if (filenamePattern.matcher(f.getAbsolutePath()).matches()) {
                            foundFiles.add(f.toURI());
                        }
                        foundFiles.addAll(collectFiles(f, filenamePattern, recurse));
                    }
                }
                String filename = f.toURI().toString();
                if (filenamePattern.matcher(filename).matches()) {
                    foundFiles.add(f.toURI());
                }
            }
        }
        return foundFiles;
    }

    @Override
    public Set<URI> find(Set<File> folder, Set<String> filenamePattern, Set<String> contentPattern,
            Set<String> archivePattern, SearchOption... options) {
        System.err.println("not implemented yet");
        return null;
    }

    @Override
    public Set<URI> find(File folder, String filenamePattern, String contentPattern, SearchOption... options) {
        Set<URI> foundFiles = new HashSet<URI>();
        Set<URI> foundContent = new HashSet<URI>();

        if ((filenamePattern != null) && !filenamePattern.equals("")) {
            filenameRegExPattern = Pattern.compile(filenamePattern);
        }
        SearchOptions opts = SearchOptions.parse(options);

        foundFiles.addAll(collectFiles(folder, filenameRegExPattern, opts.recurse));

        if (opts.inArchives) {
            Set<URI> allFiles = new HashSet<URI>();
            allFiles.addAll(collectFiles(folder, ALLFILE_PATTERN, opts.recurse));

            for (URI foundFile : allFiles) {
                try {
                    ZipInputStream zis = new ZipInputStream(foundFile.toURL().openStream());
                    ZipEntry zipEntry;
                    while ((zipEntry = zis.getNextEntry()) != null) {
                        String name = zipEntry.getName();
                        if (filenameRegExPattern.matcher(name).matches()) {
                            try {
                                URI uri = new URI("jar:" + foundFile + "!/" + name);
                                foundFiles.add(uri);
                            } catch (URISyntaxException e) {
                                logger.error("unable to create uri from jar:" + foundFile + "!/" + name, e);
                            }
                        }
                    }
                    zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if ((contentPattern != null) && !contentPattern.equals("")) {
            this.contentRegExPattern = Pattern.compile(contentPattern, Pattern.DOTALL | Pattern.MULTILINE);

            byte[] readAllBytes = null;
            for (URI foundFile : foundFiles) {
                try {
                    if (foundFile.getScheme().startsWith("jar")) {
                        String foundFileStr = foundFile.toString();
                        String zipURI = foundFileStr.substring(0, foundFileStr.indexOf("!"));
                        String entryPath = foundFileStr.substring(foundFileStr.indexOf("!") + 1, foundFileStr.length());

                        final Map<String, String> env = new HashMap<>();
                        env.put("create", "true");
                        FileSystem zfs;
                        try {
                            zfs = FileSystems.newFileSystem(new URI(zipURI), env);
                            readAllBytes = Files.readAllBytes(zfs.getPath(entryPath));
                            zfs.close();
                        } catch (URISyntaxException e) {
                            logger.error("", e);
                        }
                    } else {
                        Path path = Paths.get(foundFile);
                        if (!path.toFile().isDirectory()) {
                            readAllBytes = Files.readAllBytes(path);
                        }
                    }
                    if (readAllBytes != null) {
                        String text = new String(readAllBytes, Charset.forName("UTF-8"));
                        if (contentRegExPattern.matcher(text).matches()) {
                            foundContent.add(foundFile);
                        }
                    }
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        } else {
            // no content search required
            foundContent.addAll(foundFiles);
        }

        return foundContent;
    }

    /**
     * Parses the arguments for a file copy operation.
     */
    private static class SearchOptions {
        boolean recurse = false;
        boolean inArchives = false;

        private SearchOptions() {
        }

        static SearchOptions parse(SearchOption... options) {
            SearchOptions result = new SearchOptions();
            if (options != null) {
                for (SearchOption option : options) {
                    if (option == SearchOption.RECURSE) {
                        result.recurse = true;
                        continue;
                    }
                    if (option == SearchOption.ARCHIVE) {
                        result.inArchives = true;
                        continue;
                    }
                    if (option == null)
                        throw new NullPointerException();
                    throw new UnsupportedOperationException("'" + option + "' is not a recognized search option");
                }
            }
            return result;
        }
    }
}
