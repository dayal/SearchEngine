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
import java.util.HashMap;
import java.util.List;

import com.csulb.edu.set.PorterStemmer;
import com.csulb.edu.set.SimpleTokenStream;
import com.csulb.edu.set.indexes.Index;
import com.csulb.edu.set.indexes.TokenStream;
import com.csulb.edu.set.indexes.pii.PositionalPosting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BiWordIndex extends Index<Integer>{
	
	public BiWordIndex(String directory) throws IOException {
		super(directory);
		// TODO Auto-generated constructor stub
	}

	public void addTerm(String term, int documentID) {
		// TO-DO: add the term to the index hashtable. If the table does not
		// have
		// an entry for the term, initialize a new ArrayList<Integer>, add the
		// docID to the list, and put it into the map. Otherwise add the docID
		// to the list that already exists in the map, but ONLY IF the list does
		// not already contain the docID.
		if (index.containsKey(term)) {
			List<Integer> docIDs = index.get(term);
			
			// We need to compare the new document id only with the last document id added in the posting list till now.
			if (docIDs.get(docIDs.size() - 1) != documentID) {
				docIDs.add(documentID);
			}
		} else {
			List<Integer> docIDs = new ArrayList<Integer>();
			docIDs.add(documentID);
			index.put(term, docIDs);
		}
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
	protected void indexFile(File jsonFile, int docID) {
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
			
			// call addTerm of PII with token1 + position of the token and the doc
			
			// Loop to read the next tokens. Sends the pair of two token to be indexed
			while (tokenStream.hasNextToken()) {
				String token2 = PorterStemmer.processToken(processWord(tokenStream.nextToken()));				
				addTerm(token1 + token2, docID);
				// Call addTerm if PII with token2 + position of the token with the doc
				// Handle hyphenization
				token1 = token2;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
