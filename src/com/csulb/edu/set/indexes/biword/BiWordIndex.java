package com.csulb.edu.set.indexes.biword;

import java.util.ArrayList;
import java.util.List;

import com.csulb.edu.set.indexes.Index;

public class BiWordIndex extends Index<Integer>{
	
	public BiWordIndex(){
		super();
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
}
