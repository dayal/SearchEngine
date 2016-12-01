/**
 * 
 */
package edu.csulb.set.documentclassifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.csulb.set.indexes.TokenStream;
import edu.csulb.set.indexes.pii.PositionalPosting;
import edu.csulb.set.utils.Utils;

/**
 * 
 *
 */
public class ClassifyDocuments {

	/**
	 * Create all the directory paths.
	 * String allDocs -> path to the directory containing ALL the 85 documents except the 11 controversial documents
	 * String hamiltonDocs -> path to the directory containing the docs claimed to be written by HAMILTON
	 * String jayDocs -> path to the directory containing the docs claimed to be written by JAY
	 * String madisonDocs -> path to the directory containing the docs claimed to be written by MADISON
	 * String hamiltonAndMadison -> path to the directory containing the docs claimed to be written by both HAMILTON and MADISON
	 * String toBeClassified -> path to the directory containing the 11 controversial documents to be classified
	 */
	
	String allDocs = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\ALL";
	String hamiltonDocs = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\HAMILTON";
	String jayDocs = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\JAY";
	String madisonDocs = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\MADISON";
	String hamiltonAndMadison = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\HAMILTON AND MADISON";
	String toBeClassified = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\HAMILTON OR MADISON";

	// A list to store all the filenames. The index of the list maps to the documentID stored in the postings list of the inverted index
	List<String> fileNames = new ArrayList<String>();

	// Now index all the documents in the ALL folder
	PositionalInvertedIndex pInvertedIndex;

	Set<String> setOfHamiltonDocs;
	Set<String> setOfMadisonDocs;
	Set<String> setOfJayDocs;

	String[] corpusVocab;

	/**
	 * 1. Index the documents 
	 * 2. Calculate wdt
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		ClassifyDocuments docsClassification = new ClassifyDocuments();

		// Create the invertedIndex
		docsClassification.createIndex();

		// Call doRocchioClassification
		docsClassification.doRocchioClassification();

		// Call doBayesianClassification

	}

	/**
	 * 
	 */
	private void createIndex() {
		// Now index all the documents in the ALL folder
		pInvertedIndex = new PositionalInvertedIndex();

		/**
		 * Indexing the documents in the ALL folder. Currently the ALL folder
		 * contains a total of 74 document items
		 */
		PositionalInvertedIndex.createIndex(allDocs, pInvertedIndex, fileNames, 0);

		/**
		 * Indexing the documents in the HAMILTON OR MADISON folder. Adding it
		 * to the same index This folder has a total of 11 document items
		 */
		PositionalInvertedIndex.createIndex(toBeClassified, pInvertedIndex, fileNames, fileNames.size());

		corpusVocab = pInvertedIndex.getDictionary();

		setOfHamiltonDocs = populateAlreadyClassifiedDocsList(hamiltonDocs);
		setOfMadisonDocs = populateAlreadyClassifiedDocsList(madisonDocs);
		setOfJayDocs = populateAlreadyClassifiedDocsList(jayDocs);
	}

	/**
	 * 
	 */
	private void doRocchioClassification() {

		// Create the document vectors of all the 85 documents available		
		// List<DocumentVector> docVectorsList = new ArrayList<DocumentVector>(85);

		List<DocumentVector> docVectorsList = Stream.generate(DocumentVector::new).limit(85)
				.collect(Collectors.toList());		

		for (int i = 0; i < corpusVocab.length; i++) {

			for (PositionalPosting pPosting : pInvertedIndex.getPostings(corpusVocab[i])) {

				DocumentVector docVector = docVectorsList.get(pPosting.getDocumentId());
				if (docVector.getDocumentId() == null) {
					
					// Creates a T-dimensional document vector having all the vector components initialized to 0.0
					docVector.addAll(Collections.nCopies(corpusVocab.length, 0.0));
					
					// Set the documentId of this document to the particular docId to which this vector belongs
					docVector.setDocumentId(pPosting.getDocumentId());
				}
				double wdt = 1 + Math.log(pPosting.getPositions().size());
				docVector.set(i, wdt);

				docVector.setLd(docVector.getLd() + Math.pow(wdt, 2));
			}
		}

		DocumentClass hamiltonDocClass = new DocumentClass("H");
		DocumentClass madisonDocClass = new DocumentClass("M");
		DocumentClass jayDocClass = new DocumentClass("J");

		List<DocumentVector> docsToBeClassified = new ArrayList<DocumentVector>();

		// Normalize each of the document vectors by the Euclidian distance Ld
		// Add the normalized vector to their corresponding classes
		for (DocumentVector documentVector : docVectorsList) {

			documentVector.normalizeVector();

			boolean isDocClassified = false;

			if (setOfHamiltonDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
				hamiltonDocClass.getDocVectors().add(documentVector);
				isDocClassified = true;
			}
			if (setOfJayDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
				jayDocClass.getDocVectors().add(documentVector);
				isDocClassified = true;
			}
			if (setOfMadisonDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
				madisonDocClass.getDocVectors().add(documentVector);
				isDocClassified = true;
			}

			if (!isDocClassified) {
				docsToBeClassified.add(documentVector);
			}
		}

		// Initialize the centroid vector of all the three classes to 0.0
		hamiltonDocClass.getCentroid().addAll(Collections.nCopies(corpusVocab.length, 0.0));
		madisonDocClass.getCentroid().addAll(Collections.nCopies(corpusVocab.length, 0.0));
		jayDocClass.getCentroid().addAll(Collections.nCopies(corpusVocab.length, 0.0));
		
		// Find the centroid of all the three classes of documents
		hamiltonDocClass.calculateCentroid();
		madisonDocClass.calculateCentroid();
		jayDocClass.calculateCentroid();

		for (DocumentVector toBeClassifiedDocVector : docsToBeClassified) {
			double distanceFromHClass = DocumentVector.findEuclidianDistance(toBeClassifiedDocVector,
					hamiltonDocClass.getCentroid());
			double distanceFromMClass = DocumentVector.findEuclidianDistance(toBeClassifiedDocVector,
					madisonDocClass.getCentroid());
			// double distanceFromJClass =
			// DocumentVector.findEuclidianDistance(toBeClassifiedDocVector,
			// jayDocClass.getCentroid());

			if (distanceFromHClass < distanceFromMClass) {
				System.out.println("Document : " + fileNames.get(toBeClassifiedDocVector.getDocumentId())
						+ " : belongs to Hamilton class");
			} else if (distanceFromMClass < distanceFromHClass) {
				System.out.println("Document : " + fileNames.get(toBeClassifiedDocVector.getDocumentId())
						+ " : belongs to Madison class");
			} else {
				System.out.println("Document : " + fileNames.get(toBeClassifiedDocVector.getDocumentId())
						+ " : belongs to both Hamilton and Madison class");
			}
		}

	}

	private void doBayesianClassification() {
		
		// Do feature selection: Calculate Mutual Information and keep the highest values in a priority queue
		int k = 500;
		PriorityQueue<MutualInformation> pQueue  = new PriorityQueue<MutualInformation>(500);
		double N = fileNames.size();
		
		List<Set<String>> classifiedDocsList = new ArrayList<Set<String>>();
		classifiedDocsList.add(setOfHamiltonDocs);
		classifiedDocsList.add(setOfMadisonDocs);
		classifiedDocsList.add(setOfJayDocs);
		
		for (String term : corpusVocab) {

			// All docs containing this term
			Set<String> matchingDocs = new HashSet<String>();
			
			for (PositionalPosting posting : pInvertedIndex.getPostings(term)) {
				matchingDocs.add(fileNames.get(posting.getDocumentId()));
			}
			
			// iterate through all classes (Hamilton, Madison, Jay)
			for (int i = 0; i < classifiedDocsList.size(); i++) {
				
				Set<String> classifiedDocs = classifiedDocsList.get(i);
				
				Set<String> intersection = new HashSet<String>(matchingDocs);
				intersection.retainAll(classifiedDocs);
				double N11 = intersection.size();
				
				Set<String> difference = new HashSet<String>(matchingDocs);
				difference.removeAll(classifiedDocs);
				double N10 = difference.size();
				
				double N01 = classifiedDocs.size() - N11;
				double N00 = fileNames.size() - classifiedDocs.size() - N10;

				// Calculate I(t,c)
				double Itc = (N11 / N) * log2((N * N11) / ((N11 + N10) * (N11 + N01)))
						+ (N01 / N) * log2((N * N01) / ((N01 + N00) * (N11 + N01)))
						+ (N10 / N) * log2((N * N10) / ((N11 + N10) * (N10 + N00)))
						+ (N00 / N) * log2((N * N00) / ((N01 + N00) * (N10 + N00)));
				pQueue.add(new MutualInformation(term, Itc));
			}
		}
		
		// Build Discriminating Set of vocab terms
		Set<String> terms = new HashSet<String>();
		for (Object mi : pQueue.toArray()) {
			terms.add(((MutualInformation) mi).getTerm());
		}
		
		
		// get term counts in classified (trainer) docs		
		double[] classifiedDocsTermCount = new double[classifiedDocsList.size()];
		Set<String> hamiltonTerms = getTerms(hamiltonDocs);
		Set<String> madisonTerms = getTerms(madisonDocs);
		Set<String> jayTerms = getTerms(jayDocs);
		
		// only use terms in discriminating set
		hamiltonTerms.retainAll(terms);
		madisonTerms.retainAll(terms);
		jayTerms.retainAll(terms);
		
		classifiedDocsTermCount[0] = hamiltonTerms.size();
		classifiedDocsTermCount[1] = madisonTerms.size();
		classifiedDocsTermCount[2] = jayTerms.size();
		
		// Calculate p(t|c)
		// maps term to list of p(t|c) for each class
		Map<String, List<Double>> ptc = new HashMap<String, List<Double>>();
		
		for (String term : terms) {
			ptc.put(term, new ArrayList<Double>());
			for (int i = 0; i < classifiedDocsList.size(); i++) {
				// All docs containing this term
				Set<String> matchingDocs = new HashSet<String>();
				
				for (PositionalPosting posting : pInvertedIndex.getPostings(term)) {
					matchingDocs.add(fileNames.get(posting.getDocumentId()));
				}
				matchingDocs.retainAll(classifiedDocsList.get(i));
				ptc.get(term).set(i, (matchingDocs.size() + 1 ) / (classifiedDocsTermCount[i] + terms.size()));
			}
		}
		
		// visit each unclassified document
		try {
			Path currentWorkingPath = Paths.get(toBeClassified).toAbsolutePath();
			// This is our standard "walk through all .txt files" code.
			Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					// make sure we only process the current working directory
					if (currentWorkingPath.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

					if (file.toString().endsWith(".txt")) {

						TokenStream tokenStream = null;
						// terms in this document
						Set<String> docTerms = new HashSet<String>();
						try {
							tokenStream = Utils.getTokenStreams(file.toFile());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						while (tokenStream.hasNextToken()) {
							String token = Utils.processWord(tokenStream.nextToken().trim(), false);
							docTerms.add(token);
						}
						// only keep terms in discriminating set
						docTerms.retainAll(terms);
						
						// calculate log(p(c) + sum(log(p(ti|c))) for each class
						double[] cd = new double[classifiedDocsList.size()];
						for (int i = 0; i < classifiedDocsList.size(); i++) {
							double sum = 0;
							for (String term : docTerms) {
								sum += Math.log(ptc.get(term).get(i));
							}
							
							cd[i] = Math.log(classifiedDocsTermCount[i] / terms.size()) + sum;
						}
						
						if (cd[0] > cd[1] && cd[0] > cd[2]) {
							System.out.println("Document : " + file.getFileName().toString() + " : belongs to Hamilton");
						} else if (cd[1] > cd[0] && cd[1] > cd[2]) {
							System.out.println("Document : " + file.getFileName().toString() + " : belongs to Madison");
						} else if (cd[2] > cd[0] && cd[2] > cd[1]) {
							System.out.println("Document : " + file.getFileName().toString() + " : belongs to Jay");
						} else {
							System.out.println("I don't know whom document : " + file.getFileName().toString() + " : belongs to.");
						}
					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static double log2(double val) {
		return Math.log(val) / Math.log(2);
	}
	
	
	public static Set<String> populateAlreadyClassifiedDocsList(String dirPath) {
		Set<String> classifiedFileNames = new HashSet<String>();
		try {
			Path currentWorkingPath = Paths.get(dirPath).toAbsolutePath();
			// This is our standard "walk through all .txt files" code.
			Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					// make sure we only process the current working directory
					if (currentWorkingPath.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

					if (file.toString().endsWith(".txt")) {
						classifiedFileNames.add(file.getFileName().toString());					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return classifiedFileNames;
	}
	
	public static Set<String> getTerms (String dirPath) {
		Set<String> terms = new HashSet<String>();
		try {
			Path currentWorkingPath = Paths.get(dirPath).toAbsolutePath();
			// This is our standard "walk through all .txt files" code.
			Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					// make sure we only process the current working directory
					if (currentWorkingPath.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

					if (file.toString().endsWith(".txt")) {

						TokenStream tokenStream = null;
						try {
							tokenStream = Utils.getTokenStreams(file.toFile());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						while (tokenStream.hasNextToken()) {

							String token = Utils.processWord(tokenStream.nextToken().trim(), false);
							terms.add(token);
						}
					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return terms;
	}
}
