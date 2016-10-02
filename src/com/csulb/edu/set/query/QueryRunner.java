package com.csulb.edu.set.query;

import java.util.ArrayList;
import java.util.List;

import com.csulb.edu.set.PorterStemmer;
import com.csulb.edu.set.exception.InvalidQueryException;
import com.csulb.edu.set.indexes.biword.BiWordIndex;
import com.csulb.edu.set.indexes.pii.PositionalInvertedIndex;
import com.csulb.edu.set.indexes.pii.PositionalPosting;
import com.csulb.edu.set.utils.Utils;

public class QueryRunner {

	// run the list of queries (eg. A + B + C)
	public static List<Integer> runQueries(String queryInput, PositionalInvertedIndex pInvertedIndex,
			BiWordIndex biWordIndex) throws InvalidQueryException {
		System.out.println("Running the query");
		List<Integer> docIds = new ArrayList<Integer>();
		// parse String queries into a list of query objects
		List<Query> queries = QueryParser.parseQuery(queryInput);

		for (Query query : queries) {
			docIds = getUnion(docIds, getdocIdsMatchingQuery(query, pInvertedIndex, biWordIndex));
		}

		return docIds;
	}

	private static List<Integer> getdocIdsMatchingQuery(Query query, PositionalInvertedIndex pInvertedIndex,
			BiWordIndex biWordIndex) {
		List<Integer> results = new ArrayList<Integer>();
		for (QueryLiteral queryLiteral : query.getQueryLiterals()) {
			// docIds that match the current query literal that is being
			// processed
			List<Integer> docIds = new ArrayList<Integer>();
			if (!queryLiteral.isPhrase()) {
				// use positional inverted index
				List<PositionalPosting> positionalPostings = pInvertedIndex
						.getPostings(PorterStemmer.processToken(Utils.removeHyphens(Utils.processWord(queryLiteral.getTokens().get(0)))));
				if (positionalPostings != null) {
					for (PositionalPosting positionalPosting : positionalPostings) {
						docIds.add(positionalPosting.getDocumentId());
					}
				}
			} else if (queryLiteral.isPhrase() && queryLiteral.getTokens().size() == 2) {
				// use bi-word index
				List<Integer> postings = biWordIndex.getPostings(PorterStemmer.processToken(Utils.processWord(queryLiteral.getTokens().get(0)))
						+ PorterStemmer.processToken(Utils.removeHyphens(Utils.processWord(queryLiteral.getTokens().get(1)))));
				if (postings != null) {
					docIds.addAll(postings);
				}
			} else {
				// use positional inverted index
				List<PositionalPosting> postings = new ArrayList<PositionalPosting>();
				for (int i = 0; i < queryLiteral.getTokens().size(); i++) {
					String token = queryLiteral.getTokens().get(i);
					List<PositionalPosting> currentPostings = pInvertedIndex
							.getPostings(Utils.removeHyphens(PorterStemmer.processToken(Utils.processWord(token))));
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
									currentPostingsPositions.set(l, currentPostingsPositions.get(l) - i);
								}
								List<Integer> newPostingsPositions = getIntersection(postingsPositions,
										currentPostingsPositions);
								if (!newPostingsPositions.isEmpty()) {
									newPostings.add(new PositionalPosting(postings.get(j).getDocumentId(),
											newPostingsPositions));
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

}
