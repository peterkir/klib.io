package de.klib.search.filesystem.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
import de.klib.search.filesystem.SearchFilesystem;

@Component(provide = SearchFilesystem.class)
public class SearchFilesystemImpl implements SearchFilesystem {

	private final Pattern ALLFILE_PATTERN = Pattern.compile(".*");
	private Pattern filenamePattern = ALLFILE_PATTERN;
	private Pattern contentPattern = null;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Set<URI> searchDir(File folder, String regexFilename,
			String regexContent, boolean... args) {
		Set<URI> foundFiles = new HashSet<URI>();
		Set<URI> foundContent = new HashSet<URI>();
		boolean recurse = false;
		boolean searchArchives = false;

		if ((regexFilename != null) && !regexFilename.equals("")) {
			filenamePattern = Pattern.compile(regexFilename);
		}
		if (args != null) {
			if (args.length >= 1) {
				recurse = args[0];
			}
			if (args.length >= 2) {
				searchArchives = args[1];
			}
		}

		foundFiles.addAll(collectFiles(folder, filenamePattern, recurse));

		if (searchArchives) {
			Set<URI> allFiles = new HashSet<URI>();
			allFiles.addAll(collectFiles(folder, ALLFILE_PATTERN, recurse));

			for (URI foundFile : allFiles) {
				try {
					ZipInputStream zis = new ZipInputStream(foundFile.toURL()
							.openStream());
					ZipEntry zipEntry;
					while ((zipEntry = zis.getNextEntry()) != null) {
						String name = zipEntry.getName();
						if (filenamePattern.matcher(name).matches()) {
							try {
								URI uri = new URI("jar:" + foundFile + "!/"
										+ name);
								foundFiles.add(uri);
							} catch (URISyntaxException e) {
								logger.error("unable to create uri from jar:"
										+ foundFile + "!/" + name, e);
							}
						}
					}
					zis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if ((regexContent != null) && !regexContent.equals("")) {
			this.contentPattern = Pattern.compile(regexContent, Pattern.DOTALL
					| Pattern.MULTILINE);

			byte[] readAllBytes = null;
			for (URI foundFile : foundFiles) {

				try {
					if (foundFile.getScheme().startsWith("jar")) {
						String foundFileStr = foundFile.toString();
						String zipURI = foundFileStr.substring(0,
								foundFileStr.indexOf("!"));
						String entryPath = foundFileStr.substring(
								foundFileStr.indexOf("!") + 1,
								foundFileStr.length());

						final Map<String, String> env = new HashMap<>();
						env.put("create", "true");
						FileSystem zfs;
						try {
							zfs = FileSystems.newFileSystem(new URI(zipURI),
									env);
							readAllBytes = Files.readAllBytes(zfs
									.getPath(entryPath));
							zfs.close();
						} catch (URISyntaxException e) {
							logger.error("",e);
						}
					} else {
						readAllBytes = Files.readAllBytes(Paths.get(foundFile));
					}
					String text = new String(readAllBytes,
							Charset.forName("UTF-8"));
					if (contentPattern.matcher(text).matches()) {
						foundContent.add(foundFile);
					}
				} catch (IOException e) {
					logger.error("",e);
				}
			}
		} else {
			// no content search required
			foundContent.addAll(foundFiles);
		}

		return foundContent;
	}

	public Set<URI> collectFiles(final File folder,
			final Pattern filenamePattern, boolean recurse) {

		Set<URI> foundFiles = new HashSet<URI>();

		File[] list = folder.listFiles();

		if (list != null) {
			for (File f : list) {
				if (recurse) {
					if (f.isDirectory()) {
						if (filenamePattern.matcher(f.getAbsolutePath())
								.matches()) {
							foundFiles.add(f.toURI());
						}
						foundFiles.addAll(collectFiles(f, filenamePattern,
								recurse));
					}
				}
				if (filenamePattern.matcher(f.toURI().toString()).matches()) {
					foundFiles.add(f.toURI());
				}
			}
		}
		return foundFiles;
	}

	@Override
	public Set<URI> searchDir(Set<File> folder, Set<String> filenamePattern,
			Set<String> contentPattern, Set<String> archivePattern,
			boolean recurse) {
		// TODO Auto-generated method stub
		return null;
	}

}
