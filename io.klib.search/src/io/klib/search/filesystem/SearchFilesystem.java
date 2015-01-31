package io.klib.search.filesystem;

import java.io.File;
import java.net.URI;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Interface for searches on the filesystem
 *
 */
public interface SearchFilesystem {

    /**
     * Search inside a root folder for a given filename and/or content pattern.
     * The result is a {@link Set} of all {@link URI}s matching the given search
     * patterns.
     * 
     * <p>
     * The {@code options} parameter may include any of the following:
     * <p>
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr>
     * <th>Option</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td> {@link SearchOption#RECURSE RECURSE}</td>
     * <td>If the folder contains child folders, the search will recurse into
     * them</td>
     * </tr>
     * <tr>
     * <td> {@link SearchOption#ARCHIVE ARCHIVE}</td>
     * <td>If a file found during search has the file ending ".zip" or ".jar" it
     * will be searched within this zip archive</td>
     * </tr>
     * </table>
     * 
     * @param folder
     *            root folder the search starts from
     * 
     * @param filenamePattern
     *            string representation of a regular expression {@link Pattern}
     *            of the filename/s to search for
     * 
     * @param contentPattern
     *            string representation of a regular expression {@link Pattern}
     *            for the text content to search for inside matched files, can
     *            be null or empty
     * 
     * @param options
     *            specify the SearchOptions
     * 
     * @return Set of all URIs files found matching the given filenamePattern
     *         and contentPattern
     */
    Set<URI> find(File folder, String filenamePattern, String contentPattern, SearchOption... options);

    /**
     *
     * @param file
     *            root folder the search starts from
     * 
     * @param filenamePattern
     *            set of string representations of regular expressions
     *            {@link Pattern} of the filename/s to search for
     * 
     * @param contentPattern
     *            set of string representations of regular expressions
     *            {@link Pattern} for the text content to search
     * 
     * @param archivePattern
     *            set of string representations of regular expression
     *            {@link Pattern} for the archive file names/extensions to
     *            investigate
     * 
     * @param options
     *            specify the SearchOptions
     * 
     * @return Set of all URIs files found matching the given filenamePattern,
     *         contentPattern and archivePattern
     */
    Set<URI> find(Set<File> file, Set<String> filenamePattern, Set<String> contentPattern,
            Set<String> zipArchivePattern, SearchOption... options);

}
