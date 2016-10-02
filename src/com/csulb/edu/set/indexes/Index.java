package com.csulb.edu.set.indexes;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class Index<T> {

	protected HashMap<String, List<T>> index;
	protected List<String> fileNames;
	
	private static String specialCharsRegexStart = "^\\W*";
	private static String specialCharsRegexEnd = "\\W*$";

	public Index(String directory) throws IOException {

		index = new HashMap<String, List<T>>();

		final Path currentWorkingPath = Paths.get(directory).toAbsolutePath();
		
		// the list of file names that were processed
		fileNames = new ArrayList<String>();

		// This is our standard "walk through all .txt files" code.
		Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
			int mDocumentID = 0;

			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				// make sure we only process the current working directory
				if (currentWorkingPath.equals(dir)) {
					return FileVisitResult.CONTINUE;
				}
				return FileVisitResult.SKIP_SUBTREE;
			}

			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				// only process .txt files
				if (file.toString().endsWith(".json")) {
					// we have found a .txt file; add its name to the fileName
					// list,
					// then index the file and increase the document ID counter.
					//System.out.println("Indexing file " + file.getFileName());

					fileNames.add(file.getFileName().toString());

					// Get the contents of the body element of the file name
					indexFile(file.toFile(), mDocumentID);
					mDocumentID++;
				}
				return FileVisitResult.CONTINUE;
			}

			// don't throw exceptions if files are locked/other errors occur
			public FileVisitResult visitFileFailed(Path file, IOException e) {
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	public List<T> getPostings(String term) {
		return index.get(term);
	}

	public int getTermCount() {
		return index.keySet().size();
	}

	public String[] getDictionary() {
		String[] dict = index.keySet().toArray(new String[0]);
		Arrays.sort(dict);
		
		return dict;
	}
	
	public List<String> getFileNames() {
		return fileNames;
	}
	
	protected abstract void indexFile(File jsonFile, int docID);
	
	// TODO: move this
	public static String processWord(String next) {
		return next.replaceAll(specialCharsRegexStart, "").replaceAll(specialCharsRegexEnd, "").replaceAll("'","").toLowerCase();
	}
	
	public static String removeHyphens(String next) {
		return next.replaceAll("-","").toLowerCase();
	}

}
