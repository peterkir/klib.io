package io.klib.search.filesystem;

import java.io.File;
import java.net.URI;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Interface for the Searches on the filesystem
 *
 */
public interface SearchFilesystem {

    /**
     * Search a directory for a filename pattern
     *
     * @param {@link File} folder to search
     * @param {@link String} filenamePattern as regular expression {@link Pattern} for the filename to search for
     * @param contentPattern - regular expression {@link Pattern} for the content to search for
     * @param boolean - true for recursive search
     * @param boolean - true for search inside archives (.zip,.jar)

     * @return {@link Set} of all {@link URI}s found inside the specified folder that matches filenamePattern and
     *         contentPattern
     */
    Set<URI> searchDir(File folder, String filenamePattern, String contentPattern, boolean... args);

    /**
     *
     * @param {@link Set}<File> folder to search
     * @param {@link Set}&lt;{@link String}&gt; filenamePattern - regular expression {@link Pattern} for the filename to
     *        investigate
     * @param {@link Set}&lt;{@link String}&gt; contentPattern - regular expression {@link Pattern} for the content to
     *        investigate
     * @param {@link Set}&lt;{@link String}&gt; archivePattern - regular expression {@link Pattern} for the archive file
     *        extensions to investigate
     * @param boolean - true for recursive search
     * @return {@link Set} of all {@link URI}s found inside the specified folder that matches filenamePattern and
     *         contentPattern
     */
    Set<URI> searchDir(Set<File> folder,
                       Set<String> filenamePattern,
                       Set<String> contentPattern,
                       Set<String> archivePattern,
                       boolean recurse);

}
