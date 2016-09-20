package com.csulb.edu.set;

/**
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PositionalInvertedIndex {

	private HashMap<String, List<PositionalPosting>> pInvertedIndex;

	public PositionalInvertedIndex() {
		this.pInvertedIndex = new HashMap<String, List<PositionalPosting>>();
	}

	public void addTerm(String term, int pos, int documentID) {
		// TO-DO: add the term to the index hashtable. If the table does not
		// have
		// an entry for the term, initialize a new ArrayList<Integer>, add the
		// docID to the list, and put it into the map. Otherwise add the docID
		// to the list that already exists in the map, but ONLY IF the list does
		// not already contain the docID.
		if (this.pInvertedIndex.containsKey(term)) {
			
			boolean isDocPresent = false;
			
			for (PositionalPosting posting : this.pInvertedIndex.get(term)) {
				if (posting.getDocumentId() == documentID) {
					posting.getPositions().add(pos);
					isDocPresent = true;
				}			
			}			
			if (!isDocPresent) {
				PositionalPosting newPosting = new PositionalPosting();			
				newPosting.setDocumentId(documentID);
				newPosting.getPositions().add(pos);			
				this.pInvertedIndex.get(term).add(newPosting);
			}			
		} else {
			PositionalPosting posting = new PositionalPosting();			
			posting.setDocumentId(documentID);
			posting.getPositions().add(pos);
			
			List<PositionalPosting> postingList = new ArrayList<PositionalPosting>();
			postingList.add(posting);

			this.pInvertedIndex.put(term, postingList);
		}
	}

	public List<PositionalPosting> getPostings(String term) {
		// TO-DO: return the postings list for the given term from the index
		// map.

		return this.pInvertedIndex.get(term);
	}

	public int getTermCount() {
		// TO-DO: return the number of terms in the index.

		return this.pInvertedIndex.keySet().size();
	}

	public String[] getDictionary() {
		// TO-DO: fill an array of Strings with all the keys from the hashtable.
		// Sort the array and return it.
		String[] dict = this.pInvertedIndex.keySet().toArray(new String[0]);
		Arrays.sort(dict);
		
		return dict;
	}

}
