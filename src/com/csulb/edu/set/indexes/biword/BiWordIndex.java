package com.csulb.edu.set.indexes.biword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BiWordIndex {

	private HashMap<String, List<Integer>> biWordIndex;

	public BiWordIndex() {
		biWordIndex = new HashMap<String, List<Integer>>();
	}

	public void addTerm(String term, int documentID) {
		// TO-DO: add the term to the index hashtable. If the table does not
		// have
		// an entry for the term, initialize a new ArrayList<Integer>, add the
		// docID to the list, and put it into the map. Otherwise add the docID
		// to the list that already exists in the map, but ONLY IF the list does
		// not already contain the docID.
		if (biWordIndex.containsKey(term)) {
			List<Integer> docIDs = biWordIndex.get(term);
			
			// We need to compare the new document id only with the last document id added in the posting list till now.
			if (docIDs.get(docIDs.size() - 1) != documentID) {
				docIDs.add(documentID);
			}
		} else {
			List<Integer> docIDs = new ArrayList<Integer>();
			docIDs.add(documentID);
			biWordIndex.put(term, docIDs);
		}
	}

	public List<Integer> getPostings(String term) {
		// TO-DO: return the postings list for the given term from the index
		// map.

		return biWordIndex.get(term);
	}

	public int getTermCount() {
		// TO-DO: return the number of terms in the index.

		return biWordIndex.size();
	}

	public String[] getDictionary() {
		// TO-DO: fill an array of Strings with all the keys from the hashtable.
		// Sort the array and return it.
		String[] dictionary = new String[getTermCount()];
		Iterator<String> iterator = biWordIndex.keySet().iterator();
		for (int i = 0; iterator.hasNext(); i++) {
			dictionary[i] = (String) iterator.next();
		}
		Arrays.sort(dictionary);
		return dictionary;
	}

}
