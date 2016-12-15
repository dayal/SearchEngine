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
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
	
	String rootFolder;
	String allDocs;
	String hamiltonDocs;
	String jayDocs;
	String madisonDocs;
	String hamiltonAndMadison;
	String toBeClassified;

	// A list to store all the filenames. The index of the list maps to the documentID stored in the postings list of the inverted index
	List<String> fileNames = new ArrayList<String>();

	// Now index all the documents in the ALL folder
	PositionalInvertedIndex pInvertedIndex;

	Set<String> setOfHamiltonDocs;
	Set<String> setOfMadisonDocs;
	Set<String> setOfJayDocs;
	Set<String> setOfHamiltonAndMadisonDocs;

	String[] corpusVocab;

	/**
	 * 1. Index the documents 
	 * 2. Calculate wdt
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		ClassifyDocuments docsClassification = new ClassifyDocuments();
		
		// Ask user for input to the  root directory where all the folders are located
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter the path of the directory where all the folders are located\n");
		docsClassification.rootFolder = scan.next();
		
		// Initialize all the other path variables
		docsClassification.allDocs = docsClassification.rootFolder + "\\ALL";
		docsClassification.hamiltonDocs = docsClassification.rootFolder + "\\HAMILTON";
		docsClassification.jayDocs = docsClassification.rootFolder + "\\JAY";
		docsClassification.madisonDocs = docsClassification.rootFolder + "\\MADISON";
		docsClassification.hamiltonAndMadison = docsClassification.rootFolder + "\\HAMILTON AND MADISON";
		docsClassification.toBeClassified = docsClassification.rootFolder + "\\HAMILTON OR MADISON";

		// Create the invertedIndex
		docsClassification.createIndex();
		
		long beginTime = System.currentTimeMillis();

		// Call doRocchioClassification
		System.out.println("As per Rocchio Classification:");
		docsClassification.doRocchioClassification();

		// Call doBayesianClassification
		System.out.println("As per Bayesian Classification:");
		docsClassification.doBayesianClassification();
		
		System.out.println("Time taken : " + (System.currentTimeMillis() - beginTime) + "ms");

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
		setOfHamiltonAndMadisonDocs = populateAlreadyClassifiedDocsList(hamiltonAndMadison);
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

			// Checks if this document belongs to the Hamilton class. If yes then adds the document vector to the corresponding class.
			if (setOfHamiltonDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
				hamiltonDocClass.getDocVectors().add(documentVector);
				isDocClassified = true;
			}
			
			// Checks if this document belongs to the Jay class. If yes then adds the document vector to the corresponding class.
			if (setOfJayDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
				jayDocClass.getDocVectors().add(documentVector);
				isDocClassified = true;
			}
			
			// Checks  if the current document belongs to the Madison class. If yes then adds the document vector to the corresponding class.
			if (setOfMadisonDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
				madisonDocClass.getDocVectors().add(documentVector);
				isDocClassified = true;
			}
			
			// Adds the unclassified documents to this set but ignores the set of documents that are known to be written by both Hamilton and Madison.
			if (!isDocClassified && !setOfHamiltonAndMadisonDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
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
		
		// Do feature selection: Calculate Mutual Information
		List<MutualInformation> mutualInformationList = new ArrayList<MutualInformation>();
		int k = 5;
		System.out.println("k = " + k);
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
				if (!Double.isNaN(Itc)) {
					mutualInformationList.add(new MutualInformation(term, Itc));
				}
			}
		}
		
		// Build Discriminating Set of vocab terms by sorting mutual information
		// list and keep the first k values
		Set<String> terms = new HashSet<String>();
		Collections.sort(mutualInformationList, Collections.reverseOrder());
		for (int i = 0; i < k; i++) {
			terms.add(mutualInformationList.get(i).getTerm());
		}
		
		// get term counts in classified (trainer) docs		
		int[] classifiedDocsTermFreq = new int[classifiedDocsList.size()];
		
		classifiedDocsTermFreq[0] = getTermFreq(hamiltonDocs, terms);
		classifiedDocsTermFreq[1] = getTermFreq(madisonDocs, terms);
		classifiedDocsTermFreq[2] = getTermFreq(jayDocs, terms);
		
		// Calculate p(t|c)
		// maps term to list of p(t|c) for each class
		Map<String, List<Double>> ptc = new HashMap<String, List<Double>>();
		
		for (String term : terms) {
			ptc.put(term, new ArrayList<Double>());
			for (int i = 0; i < classifiedDocsList.size(); i++) {
				
				int ftc = 0;
				for (PositionalPosting posting : pInvertedIndex.getPostings(term)) {
					if (classifiedDocsList.get(i).contains(fileNames.get(posting.getDocumentId()))) {
						ftc += posting.getPositions().size();
					}
				}
				ptc.get(term).add((double) (ftc + 1) / (double) (classifiedDocsTermFreq[i] + terms.size()));
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
							
							cd[i] = Math.log(classifiedDocsTermFreq[i] / terms.size()) + sum;
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
	
	// Get total frequency of all terms provided
	public static int getTermFreq (String dirPath, Set<String> terms) {
		AtomicInteger freq = new AtomicInteger(0);
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
							if (terms.contains(token)) {
								freq.incrementAndGet();
							}
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
		
		return freq.get();
	}
}
