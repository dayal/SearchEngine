package com.csulb.edu.set;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

public class PIndexSearchEngine {

	public static List<String> fileNames;

	public static void main(String[] args) throws IOException {

		Scanner read = new Scanner(System.in);
		read.useDelimiter(System.getProperty("line.separator"));

		System.out.println("Enter the directory path containing the corpus");

		String userInput = read.next();

		// the inverted index
		final PositionalInvertedIndex index = createPositionalInvertedIndex(userInput);

		// Prints the inverted index
		printResults(index, fileNames);

		// Implement the same program as in Homework 1: ask the user for a term,
		// retrieve the postings list for that term, and print the names of the
		// documents which contain the term.

		String word = "";

		// while (true) {
		// Scanner getUserInput = new Scanner(System.in);
		// System.out.print("Enter a term to search for : ");
		// word = getUserInput.next();
		//
		// if (word.equalsIgnoreCase("quit")) {
		// System.out.println("\nBBye!");
		// break;
		// }
		//
		// System.out.print(word + ": ");
		// for (int i = 0; i < index.getPostings(word).size(); i++) {
		// int fileIndex = index.getPostings(word).get(i);
		// System.out.print(fileNames.get(fileIndex));
		// if (i != index.getPostings(word).size() - 1) {
		// System.out.print(", ");
		// }
		// }
		// System.out.println("\n");
		// }

		while (true) {
			System.out.print("Enter queries to search for: ");
			String input = read.next();

			if (input.equalsIgnoreCase("quit")) {
				System.out.println("\nBBye!");
				break;
			}

			List<Integer> docIds = runQueries(input, index);
			System.out.println("files matching queries:");
			for (int docId : docIds) {
				System.out.println(fileNames.get(docId));
			}
		}
		read.close();

	}

	/**
	 * Creates a Positional Inverted Index from the corpus
	 * 
	 * @param pInvertedIndex
	 * @param directory
	 * @throws IOException
	 */
	public static PositionalInvertedIndex createPositionalInvertedIndex(String directory) throws IOException {

		final Path currentWorkingPath = Paths.get(directory).toAbsolutePath();

		// the inverted index
		final PositionalInvertedIndex index = new PositionalInvertedIndex();

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
					System.out.println("Indexing file " + file.getFileName());

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
	private static void indexFile(File jsonFile, PositionalInvertedIndex index, int docID) {
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
			TokenStream tokenStream = new SimpleTokenStream(title + " " + bodyContents);
			int position = 0;

			while (tokenStream.hasNextToken()) {
				
				String token = tokenStream.nextToken();
				
				// Check if the token is hyphenized
				// Then index the terms = # of hyphens + 1				
				if (token.contains("-")) {
					for (String term : token.split("-")) {					
						index.addTerm(PorterStemmer.processToken(processWord(term)), position, docID);
						position++;
					}
					position--;
				}				
				token = processWord(token);				
				index.addTerm(PorterStemmer.processToken(token), position, docID);
				position++;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printResults(PositionalInvertedIndex index, List<String> fileNames) {

		// TO-DO: print the inverted index.
		// Retrieve the dictionary from the index. (It will already be sorted.)
		// For each term in the dictionary, retrieve the postings list for the
		// term. Use the postings list to print the list of document names that
		// contain the term. (The document ID in a postings list corresponds to
		// an index in the fileNames list.)

		// Print the postings list so they are all left-aligned starting at the
		// same column, one space after the longest of the term lengths.
		// Example:
		//
		// as: document0 document3 document4 document5
		// engines: document1
		// search: document2 document4

		// Finding the length of the longest term which is required to format
		// the output
		int longestTerm = 0;
		for (String term : index.getDictionary()) {
			longestTerm = Math.max(longestTerm, term.length());
		}

		for (String term : index.getDictionary()) {
			System.out.print(term + ":");
			printSpaces(longestTerm - term.length() + 2);

			// System.out.println("Size of posting list : " +
			// index.getPostings(term).size());

			for (PositionalPosting posting : index.getPostings(term)) {
				int fileIndex = posting.getDocumentId();
				System.out.print("<" + fileNames.get(fileIndex) + " : [");

				for (Integer position : posting.getPositions()) {
					System.out.print(position + ",");
				}
				System.out.print("]>");
			}
			System.out.println();
		}
	}

	private static List<Integer> runQueries(String queryInput, PositionalInvertedIndex index) {
		List<Integer> docIds = new ArrayList<Integer>();
		List<Query> queries = QueryParser.parseQuery(queryInput);

		for (Query query : queries) {
			docIds = getUnion(docIds, getdocIdsMatchingQuery(query, index));
		}

		return docIds;
	}

	private static List<Integer> getdocIdsMatchingQuery(Query query, PositionalInvertedIndex index) {
		List<Integer> results = new ArrayList<Integer>();
		for (QueryLiteral queryLiteral : query.getQueryLiterals()) {
			// docIds that match the current query literal that is being
			// processed
			List<Integer> docIds = new ArrayList<Integer>();
			if (!queryLiteral.isPhrase()) {
				List<PositionalPosting> positionalPostings = index
						.getPostings(PorterStemmer.processToken(queryLiteral.getTokens().get(0).toLowerCase()));
				if (positionalPostings != null) {
					for (PositionalPosting positionalPosting : positionalPostings) {
						docIds.add(positionalPosting.getDocumentId());
					}
				}
			} else {
				List<PositionalPosting> postings = new ArrayList<PositionalPosting>();
				for (int i = 0; i < queryLiteral.getTokens().size(); i++) {
					String token = queryLiteral.getTokens().get(i);
					List<PositionalPosting> currentPostings = index
							.getPostings(PorterStemmer.processToken(token.toLowerCase()));
					if (postings.isEmpty()) {
						postings = currentPostings;
					} else {
						int j = 0, k = 0;
						List<PositionalPosting> newPostings = new ArrayList<PositionalPosting>();
						while (j < postings.size() && k < currentPostings.size()) {
							if (postings.get(j).getDocumentId() < currentPostings.get(k).getDocumentId()) {
								j++;
							} else if (postings.get(j).getDocumentId() > currentPostings.get(k).getDocumentId()) {
								k++;
							} else {
								// if postings have the same documentId
								List<Integer> postingsPositions = postings.get(j).getPositions();
								List<Integer> currentPostingsPositions = new ArrayList<Integer>(
										currentPostings.get(k).getPositions());
								for (int l = 0; l < currentPostingsPositions.size(); l++) {
									currentPostingsPositions.set(l, currentPostingsPositions.get(l) - 1);
								}
								List<Integer> newPostingsPositions = getIntersection(postingsPositions,
										currentPostingsPositions);
								if (!newPostingsPositions.isEmpty()) {
									newPostings.add(new PositionalPosting(postings.get(j).getDocumentId(),
											getIntersection(postingsPositions, currentPostingsPositions)));
								}
								j++;
								k++;
							}
						}
						postings = newPostings;
					}
				}
				for (PositionalPosting posting : postings) {
					docIds.add(posting.getDocumentId());
				}
			}

			if (results.isEmpty()) {
				results = docIds;
			} else {
				if (queryLiteral.isPositive()) {
					results = getIntersection(results, docIds);
				} else {
					results = getDifference(results, docIds);
				}
			}
		}

		return results;
	}

	// helper method to get the union of two sorted docId lists
	private static List<Integer> getUnion(List<Integer> docIdsA, List<Integer> docIdsB) {
		int i = 0, j = 0;
		List<Integer> results = new ArrayList<Integer>();
		while (i < docIdsA.size() && j < docIdsB.size()) {
			if (docIdsA.get(i) < docIdsB.get(j)) {
				results.add(docIdsA.get(i++));
			} else if (docIdsA.get(i) > docIdsB.get(j)) {
				results.add(docIdsB.get(j++));
			} else {
				results.add(docIdsA.get(i++));
				j++;
			}
		}
		while (i < docIdsA.size()) {
			results.add(docIdsA.get(i++));
		}
		while (j < docIdsB.size()) {
			results.add(docIdsB.get(j++));
		}

		return results;
	}

	// helper method to get the intersection of two sorted docId lists
	private static List<Integer> getIntersection(List<Integer> docIdsA, List<Integer> docIdsB) {
		int i = 0, j = 0;
		List<Integer> results = new ArrayList<Integer>();
		while (i < docIdsA.size() && j < docIdsB.size()) {
			if (docIdsA.get(i) < docIdsB.get(j)) {
				i++;
			} else if (docIdsA.get(i) > docIdsB.get(j)) {
				j++;
			} else {
				results.add(docIdsA.get(i++));
				j++;
			}
		}

		return results;
	}

	// helper method to get the difference of two sorted docId lists
	private static List<Integer> getDifference(List<Integer> docIdsA, List<Integer> docIdsB) {
		int i = 0, j = 0;
		List<Integer> results = new ArrayList<Integer>();
		while (i < docIdsA.size() && j < docIdsB.size()) {
			if (docIdsA.get(i) < docIdsB.get(j)) {
				results.add(docIdsA.get(i++));
			} else if (docIdsA.get(i) > docIdsB.get(j)) {
				j++;
			} else {
				i++;
				j++;
			}
		}

		while (i < docIdsA.size()) {
			results.add(docIdsA.get(i++));
		}

		return results;
	}

	// prints a bunch of spaces
	private static void printSpaces(int spaces) {
		for (int i = 0; i < spaces; i++) {
			System.out.print(" ");
		}
	}
	
	private static String processWord(String next) {
		return next.replaceAll("\\W", "").toLowerCase();
	}

}
