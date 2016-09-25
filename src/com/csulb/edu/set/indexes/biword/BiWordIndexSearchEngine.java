package com.csulb.edu.set.indexes.biword;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.csulb.edu.set.PorterStemmer;
import com.csulb.edu.set.SimpleTokenStream;
import com.csulb.edu.set.indexes.TokenStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BiWordIndexSearchEngine {

	public static List<String> fileNames;

	/**
	 * Creates a Positional Inverted Index from the corpus
	 * 
	 * @param pInvertedIndex
	 * @param directory
	 * @throws IOException
	 */
	public static BiWordIndex createBiWordInvertedIndex(String directory) throws IOException {

		final Path currentWorkingPath = Paths.get(directory).toAbsolutePath();

		// the inverted index
		final BiWordIndex index = new BiWordIndex();

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

					fileNames.add(file.getFileName().toString());

					// Get the contents of the body element of the file name
					indexFile(file.toFile(), index, mDocumentID);
					mDocumentID++;
				}
				return FileVisitResult.CONTINUE;
			}

			// don't throw exceptions if files are locked/other errors occur
			public FileVisitResult visitFileFailed(Path file, IOException e) {
				return FileVisitResult.CONTINUE;
			}

		});

		return index;
	}

	/**
	 * Indexes a file by reading a series of tokens from the file, treating each
	 * token as a term, and then adding the given document's ID to the inverted
	 * index for the term.
	 * 
	 * @param file
	 *            a File object for the document to index.
	 * @param index
	 *            the current state of the index for the files that have already
	 *            been processed.
	 * @param docID
	 *            the integer ID of the current document, needed when indexing
	 *            each term from the document.
	 */
	private static void indexFile(File jsonFile, BiWordIndex index, int docID) {
		// TO-DO: finish this method for indexing a particular file.
		// Construct a SimpleTokenStream for the given File.
		// Read each token from the stream and add it to the index.

		try {
			Reader reader = null;
			try {
				reader = new FileReader(jsonFile.toString());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			JsonParser jsonParser = new JsonParser();
			JsonElement element = jsonParser.parse(reader);

			String bodyContents = "";
			String title = "";

			if (element.isJsonObject()) {
				JsonObject doc = element.getAsJsonObject();
				bodyContents = doc.get("body").getAsString();
				title = doc.get("title").getAsString();
			}
			
			// Read and process the first token from the file
			TokenStream tokenStream = new SimpleTokenStream(title + " " + bodyContents);			
			String token1 = tokenStream.hasNextToken() ? PorterStemmer.processToken(processWord(tokenStream.nextToken())) : "";
			
			// Loop to read the next tokens. Sends the pair of two token to be indexed
			while (tokenStream.hasNextToken()) {
				String token2 = PorterStemmer.processToken(processWord(tokenStream.nextToken()));				
				index.addTerm(token1 + token2, docID);				
				token1 = token2;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String processWord(String next) {
		return next.replaceAll("\\W", "").toLowerCase();
	}

}
