package com.csulb.edu.set.indexes.pii;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.csulb.edu.set.PorterStemmer;
import com.csulb.edu.set.SimpleTokenStream;
import com.csulb.edu.set.indexes.Index;
import com.csulb.edu.set.indexes.TokenStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PositionalInvertedIndex extends Index<PositionalPosting> {

	public PositionalInvertedIndex(String directory) throws IOException {
		super(directory);
	}
	
	public void addTerm(String term, int pos, int documentID) {
		// Checking if there was only a single special character which got removed as part of processing
		// and only the empty string "" is left
		if (term.length() == 0) {
			return;
		}
		if (index.containsKey(term)) {

			List<PositionalPosting> positionalPostingList = index.get(term);
			PositionalPosting lastPosting = positionalPostingList.get(positionalPostingList.size() - 1);

			if (lastPosting.getDocumentId() == documentID) {
				lastPosting.getPositions().add(pos);
				// isDocPresent = true;
			} else {
				PositionalPosting newPosting = new PositionalPosting();
				newPosting.setDocumentId(documentID);
				newPosting.getPositions().add(pos);
				index.get(term).add(newPosting);
			}
		} else {
			PositionalPosting posting = new PositionalPosting();
			posting.setDocumentId(documentID);
			posting.getPositions().add(pos);

			List<PositionalPosting> postingList = new ArrayList<PositionalPosting>();
			postingList.add(posting);

			index.put(term, postingList);
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
			TokenStream tokenStream = new SimpleTokenStream(bodyContents);
			int position = 0;

			while (tokenStream.hasNextToken()) {
				
				String token = tokenStream.nextToken();
				
				// Check if the token is hyphenized
				// Then index the terms = # of hyphens + 1				
				if (token.contains("-")) {
					for (String term : token.split("-")) {					
						addTerm(PorterStemmer.processToken(processWord(term)), position, docID);
						position++;
					}
					position--;
				}							
				addTerm(PorterStemmer.processToken(processWord(token)), position, docID);
				position++;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
