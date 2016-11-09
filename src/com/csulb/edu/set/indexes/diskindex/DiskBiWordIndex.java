package com.csulb.edu.set.indexes.diskindex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *	Biword Index on Disk 
 */
public class DiskBiWordIndex extends DiskIndex<Integer> {

	/**
	 * Constructs biWordIndex instance
	 * 
	 * @param path path that contains index files
	 */
	public DiskBiWordIndex(String path) {
		try {
			mVocabList = new RandomAccessFile(new File(path, DiskIndexEnum.BI_WORD_INDEX.getVocabFileName()), "r");
			mPostings = new RandomAccessFile(new File(path, DiskIndexEnum.BI_WORD_INDEX.getPostingsFileName()), "r");
			mVocabTable = readVocabTable(path);
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		}
	}

	/**
	 * Get postings given a term
	 */
	public List<Integer> getPostings(String term) {
		long postingsPosition = binarySearchVocabulary(term);
		if (postingsPosition >= 0) {
			return readPostingsFromFile(mPostings, postingsPosition);
		}
		return null;
	}
	
	/** 
	 * Get document IDs that match a term by reading postings file with a given postings position
	 * 
	 * @param postings postings file
	 * @param postingsPosition position in postings file where the postings we are looking for starts
	 * @return list of document IDs
	 */
	private static List<Integer> readPostingsFromFile(RandomAccessFile postings, long postingsPosition) {
		try {
			List<Integer> docList = new ArrayList<Integer>();

			// seek to the position in the file where the postings start.
			postings.seek(postingsPosition);

			// read the 4 bytes for the document frequency
			byte[] buffer = new byte[4];
			postings.read(buffer, 0, buffer.length);

			int documentFrequency = ByteBuffer.wrap(buffer).getInt();

			// read 4 bytes at a time from the file. After each read, convert
			// the bytes to an int posting. this value
			// is the GAP since the last posting. decode the document ID from
			// the gap and put it in the array.
			//
			// repeat until all postings are read.

			int docId = 0;
			int lastDocId = 0;

			byte docIdsBuffer[] = new byte[4];

			for (int docIdIndex = 0; docIdIndex < documentFrequency; docIdIndex++) {

				// Reads the 4 bytes of the docId into docIdsBuffer
				postings.read(docIdsBuffer, 0, docIdsBuffer.length);

				docId = ByteBuffer.wrap(docIdsBuffer).getInt() + lastDocId;
				lastDocId = docId;
				docList.add(docId);
			}
			return docList;
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	/**
	 * Create a vocab table array by reading the vocab table file
	 * 
	 * @param indexPath path where the vocab table file is at
	 * @return vocab table array that has the positions of each word in vocab files
	 */
	private static long[] readVocabTable(String indexPath) {
		try {
			long[] vocabTable;

			RandomAccessFile tableFile = new RandomAccessFile(new File(indexPath, DiskIndexEnum.BI_WORD_INDEX.getVocabTableFileName()), "r");

			byte[] byteBuffer = new byte[4];

			// Reads the first 4 bytes of data from the vocabTable.bin file into
			// the byteBuffer array
			// The first 4 bytes of data in the vocab table represents the
			// size of the corpus vocabulary
			tableFile.read(byteBuffer, 0, byteBuffer.length);

			vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];

			int tableIndex = 0;

			byteBuffer = new byte[8];

			while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { 
				// while we keep reading 8 bytes
				vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
				tableIndex++;
			}
			tableFile.close();
			return vocabTable;
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}
}