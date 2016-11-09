package com.csulb.edu.set.indexes.diskindex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csulb.edu.set.indexes.Index;
import com.csulb.edu.set.indexes.biword.BiWordIndex;
import com.csulb.edu.set.indexes.kgram.KGramIndex;
import com.csulb.edu.set.indexes.pii.PositionalInvertedIndex;
import com.csulb.edu.set.indexes.pii.PositionalPosting;

/**
 * Class that writes indexes to disk
 *
 */
public class DiskIndexWriter {
	
	/**
	 * Store K-Gram index on disk.
	 * 
	 * @param dirLocation directory to store the index in.
	 * @param kGramIndex in-memory K-Gram index
	 */
	public static void storeKGramIndexOnDisk(String dirLocation, KGramIndex kGramIndex) {
		
		ObjectOutputStream kGramOutputStream = null;
		try {
			// serialize K-Gram index object and save as file
			kGramOutputStream = new ObjectOutputStream(new FileOutputStream(new File(dirLocation, "kGrams.ser")));
			kGramOutputStream.writeObject(kGramIndex);
			kGramOutputStream.close();
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Store Bi-Word index on disk. The process is similar to buildPositionalIndexOnDisk()
	 * 
	 * @param dirLocation directory to store the index in.
	 * @param biWordIndex in-memory Bi-Word index
	 * @param corpusSize number of documents in the corpus
	 */
	public static void storeBiWordIndexOnDisk(String dirLocation, BiWordIndex biWordIndex, int corpusSize) {
		String[] dictionary = biWordIndex.getDictionary();
		long[] vocabPositions = new long[dictionary.length];
		buildVocabFile(dirLocation, dictionary, vocabPositions, DiskIndexEnum.BI_WORD_INDEX);
		buildPostingsFile(dirLocation, biWordIndex, dictionary, vocabPositions, DiskIndexEnum.BI_WORD_INDEX, corpusSize);
		
	}
	
	/**
	 * Save document weights on disk.
	 * 
	 * @param dirLocation directory to store document weights
	 * @param docWeights map that maps document id to weight
	 * @param corpusSize number of documents in the corpus
	 */
	public static void saveDocumentWeightsOnDisk(String dirLocation, Map<Integer, Double> docWeights, int corpusSize) {
		
		FileOutputStream weights = null;
		try {
			// first build the vocabulary list: a file of each vocab word
			// concatenated together.
			// also build an array associating each term with its byte location
			// in this file.
			weights = new FileOutputStream(new File(dirLocation, "docWeights.bin"));
			for (int i = 0; i < corpusSize; i++) {
				 byte[] buffer = ByteBuffer.allocate(8).putDouble(docWeights.get(i) != null ? Math.sqrt(docWeights.get(i)) : 0).array();
				 weights.write(buffer, 0, buffer.length);
			}
		} catch (Exception ex) {
			
		}
	}
	
	/**
	 * Build positional inverted index on disk
	 * 
	 * Create the following files on disk: 
	 * 1. vocab.bin -> all the vocabularies
	 * 2. vocabTable.bin -> map the position of each vocab in vocab.bin to the
	 *                      position of the corresponding list of postings in postings.bin
	 * 3. positngs.bin -> list of postings with additional information for each vocab
	 * 
	 * @param dirLocation
	 *            directory to store the index in
	 * @param pInvertedIndex
	 *            in-memory positional inverted index
	 * @param corpusSize
	 *            number of documents in corpus
	 */
	public static void buildPositionalIndexOnDisk(String dirLocation, PositionalInvertedIndex pInvertedIndex, int corpusSize) {

		// the array of terms
		String[] dictionary = pInvertedIndex.getDictionary();
		// an array of positions in the vocabulary file
		long[] vocabPositions = new long[dictionary.length];

		buildVocabFile(dirLocation, dictionary, vocabPositions, DiskIndexEnum.POSITIONAL_INDEX);
		buildPostingsFile(dirLocation, pInvertedIndex, dictionary, vocabPositions, DiskIndexEnum.POSITIONAL_INDEX, corpusSize);
	}
	
	/**
	 * Creates a vocab.bin file on the disk.
	 * The file is located at the folder represented by the folder variable
	 * The file contains the whole lexicographically sorted vocabulary of the corpus as a contiguous sequence of characters
	 * Parallel to this vocab.bin, a new list of vocabPositions[] of type long[] is created
	 * The ith entry in the list contains the byte location of the first character of the ith word in the vocab.bin file.
	 * The length of the ith term would then be (byte_location_of_i+1th_word - byte_location_of_ith_word)
	 * At the end of the execution of this method, the vocabPositions list will be there in the main memory and will be used in the 
	 * creation of vocabTable
	 * 
	 * @param dirLocation directory where the vocab file is going to be stored in
	 * @param dictionary array of all vocabs
	 * @param vocabPositions array of positions of each vocab
	 * @param indexType type of index (positional/bi-word)
	 */
	private static void buildVocabFile(String dirLocation, String[] dictionary, long[] vocabPositions, DiskIndexEnum indexType) {
		OutputStreamWriter vocabList = null;
		try {
			// first build the vocabulary list: a file of each vocab word
			// concatenated together.
			// also build an array associating each term with its byte location
			// in this file.
			int vocabIndex = 0;
			vocabList = new OutputStreamWriter(new FileOutputStream(new File(dirLocation, indexType.getVocabFileName())), "ASCII");

			int vocabPos = 0;
			for (String vocabWord : dictionary) {
				// for each String in dictionary, save the byte position where
				// that term will start in the vocab file.
				vocabPositions[vocabIndex] = vocabPos;
				
				vocabList.write(vocabWord); // then write the String
				
				vocabIndex++;
				vocabPos += vocabWord.length();
			}
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		} catch (UnsupportedEncodingException ex) {
			System.out.println(ex.toString());
		} catch (IOException ex) {
			System.out.println(ex.toString());
		} finally {
			try {
				vocabList.close();
			} catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
	}
	
	/**
	 * Build postings file
	 * 
	 * @param dirLocation directory where postings file is stored in
	 * @param index positional index / bi-word index object
	 * @param dictionary array of all vocabs
	 * @param vocabPositions array of positions of all vocabs in vocab file
	 * @param indexType type of disk index (positional / bi-word)
	 * @param corpusSize number of documents in the corpus
	 */
	@SuppressWarnings("unchecked")
	private static <T> void buildPostingsFile(String dirLocation, Index<T> index, String[] dictionary, long[] vocabPositions, DiskIndexEnum indexType, int corpusSize) {
		
		FileOutputStream postingsFile = null;
		Map<Integer, Double> docWeights = new HashMap<Integer, Double>();
		
		try {
			postingsFile = new FileOutputStream(new File(dirLocation, indexType.getPostingsFileName()));

			// simultaneously build the vocabulary table on disk, mapping a term index to a
			// file location in the postings file.
			FileOutputStream vocabTable = new FileOutputStream(new File(dirLocation, indexType.getVocabTableFileName()));

			// the first thing we must write to the vocabTable file is the number of vocab term i.e the size of the corpus dictionary
			
			// We allocated a 4 byte sized buffer to store the length of the size of the dictionary.
			// Are we assuming that the size of the dictionary never exceeds a value greater than Integer.MAX_VALUE
			byte[] tSize = ByteBuffer.allocate(4).putInt(dictionary.length).array();
			
			// Writing the dictionary size as a 4 byte value in the vocab table
			vocabTable.write(tSize, 0, tSize.length);
			
			// Creating an index to get the vocabPosition of this word which we placed in the vocabPositions array 
			// while converting the dictionary into the vocab.bin file
			int vocabIndex = 0;
			
			// Processing each word from the corpus dictionary one by one and writing it to the vocabTable.bin file
			// Each single iteration of this for loop writes 16 bytes of data in the vocabTable.bin file
			// The first 8 bytes represents the 
			for (String s : dictionary) {
				
				// write the vocab table entry for this term: 
				// byte location of the term in the vocablist file <---> and the byte location of the postings for the term in the postings file.
				byte[] vPositionBytes = ByteBuffer.allocate(8).putLong(vocabPositions[vocabIndex]).array();
								
				// Writes the 8 byte representation of the position of the first character of the word 's' in the vocabTable
				vocabTable.write(vPositionBytes, 0, vPositionBytes.length);
				
				byte[] pPositionBytes = ByteBuffer.allocate(8).putLong(postingsFile.getChannel().position()).array();
				
				// The address from where the postings list of this term starts in postings.bin file				
				vocabTable.write(pPositionBytes, 0, pPositionBytes.length);

				/**
				 *  Write the postings.bin file.
				 *  1. Fetch the postings list of this term from the index created in main memory
				 *  2. Get the size of the postings list => Gives you the number of documents in which this term occurs => doc frequency
				 *  3. Convert the decimal value of the size of the postings list into a 4 byte value
				 *  4. Write those 4 bytes (doc frequency) into the the postings.bin file. => 
				 *  	the next 4 bytes represents the id of the first document in which this term occurs
				 *  5. Get the term frequency in the document i.e. the size of the term positional list 
				 *  6. Convert the decimal value of the size into 4 bytes value
				 *  7. Now write the first position i.e the first value of the positional list => the positions where this term occurs first in the document
				 */
				
				// write the postings file for this term. first, the document
				// frequency for the term, then the document IDs, encoded as gaps.
				
				// for each String in dictionary, retrieve the list of postings which gives you the document frequency.
				List<T> postings = index.getPostings(s);
				
				// Now convert the size of the postings list into a 4 byte value
				byte[] docFreqBytes = ByteBuffer.allocate(4).putInt(postings.size()).array();
				
				// Write the size of the postings list, converted into the 4 byte value in above step, into postings.bin file
				postingsFile.write(docFreqBytes, 0, docFreqBytes.length);

				int lastDocId = 0;
				
				if (indexType == DiskIndexEnum.POSITIONAL_INDEX) {
					// Write additional information for positional index
					for (PositionalPosting positionalPosting : (List<PositionalPosting>) postings) {
						
						int docId = positionalPosting.getDocumentId();
						
						// encode a gap, not a docID
						byte[] docIdBytes = ByteBuffer.allocate(4).putInt(docId - lastDocId).array(); 
						postingsFile.write(docIdBytes, 0, docIdBytes.length);
						
						/**
						 * Get the list of positions where this term occurs in the document and then write all the positions. 
						 * No need to encode positions as gaps. Even in the worst case scenario, a document will not contain the same word 
						 * repeated (Integer.MAX_VALUE) number of times
						 */	
						
						// Get the term frequency i.e. the number of times this particular terms occurs in this docId
						int termFrequency = positionalPosting.getPositions().size();
						
						// for this term calculate the square of wdt for all the docs in the postings
						// add the square of wdt to the corresponding doc in the HashMap
						double wdt = 1 + Math.log(termFrequency);					
						
						if (docWeights.containsKey(positionalPosting.getDocumentId())) {						
							docWeights.put(positionalPosting.getDocumentId(), docWeights.get(positionalPosting.getDocumentId()) + Math.pow(wdt, 2));
						} else {
							docWeights.put(positionalPosting.getDocumentId(), Math.pow(wdt, 2));
						}				
						
						// Convert the double representation of the wdt into its corresponding byteFrequency
						byte[] wdtBytes = ByteBuffer.allocate(8).putDouble(wdt).array(); 
						
						// Write the byte representation of the wdt into the file
						postingsFile.write(wdtBytes, 0, wdtBytes.length);
						
						// Convert the integer representation of the termFrequency into its corresponding byteFrequency
						byte[] termFreqBytes = ByteBuffer.allocate(4).putInt(termFrequency).array(); 
						
						// Write the byte representation of the term frequency into the file
						postingsFile.write(termFreqBytes, 0, termFreqBytes.length);
						
						for (Integer pos : positionalPosting.getPositions()) {
							byte[] posBytes = ByteBuffer.allocate(4).putInt(pos).array(); 
							postingsFile.write(posBytes, 0, posBytes.length);
						}
						lastDocId = docId;
					}
				} else if (indexType == DiskIndexEnum.BI_WORD_INDEX) {
					// only write doc IDs in gaps if it's bi-word index
					for (int docId : (List<Integer>) postings) {
						byte[] docIdBytes = ByteBuffer.allocate(4).putInt(docId - lastDocId).array(); 
						postingsFile.write(docIdBytes, 0, docIdBytes.length);
						lastDocId = docId;
					}
				}
				vocabIndex++;
			}
			
			// Create docWeights.bin file
			if (indexType == DiskIndexEnum.POSITIONAL_INDEX) {
				saveDocumentWeightsOnDisk(dirLocation, docWeights, corpusSize);
			}
			
			vocabTable.close();
			postingsFile.close();
			
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		} finally {
			try {
				postingsFile.close();
			} catch (IOException ex) {
			}
		}
	}

}
