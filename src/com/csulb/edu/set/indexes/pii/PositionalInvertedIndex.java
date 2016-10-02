package com.csulb.edu.set.indexes.pii;

import java.util.ArrayList;
import java.util.List;

import com.csulb.edu.set.indexes.Index;

public class PositionalInvertedIndex extends Index<PositionalPosting> {

	public PositionalInvertedIndex() {
		super();
	}

	public void addTerm(String term, int pos, int documentID) {
		// Checking if there was only a single special character which got
		// removed as part of processing
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
}
