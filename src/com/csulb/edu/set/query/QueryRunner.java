package com.csulb.edu.set.query;

import java.util.ArrayList;
import java.util.List;

import com.csulb.edu.set.exception.InvalidQueryException;
import com.csulb.edu.set.indexes.biword.BiWordIndex;
import com.csulb.edu.set.indexes.pii.PositionalInvertedIndex;
import com.csulb.edu.set.indexes.pii.PositionalPosting;
import com.csulb.edu.set.utils.PorterStemmer;
import com.csulb.edu.set.utils.Utils;

/**
 * Run Queries using a positional inverted index and a bi-word index
 *
 */
public class QueryRunner {

	/**
	 * Parse query input into Query objects and execute the queries
	 * 
	 * @param queryInput
	 *            query input
	 * @param pInvertedIndex
	 *            positional inverted index
	 * @param biWordIndex
	 *            bi-word index
	 * @return a list of document ids that match the queries
	 * @throws InvalidQueryException
	 *             when query input is invalid
	 */
	public static List<Integer> runQueries(String queryInput, PositionalInvertedIndex pInvertedIndex,
			BiWordIndex biWordIndex) throws InvalidQueryException {
		System.out.println("Running the query");
		List<Integer> docIds = new ArrayList<Integer>();
		// parse query input into a list of query objects
		List<Query> queries = QueryParser.parseQuery(queryInput);

		for (Query query : queries) {
			// get the union of the results returned from each individual query
			// (Qi)
			docIds = getUnion(docIds, getdocIdsMatchingQuery(query, pInvertedIndex, biWordIndex));
		}

		return docIds;
	}

	/**
	 * Get document IDs that match the given individual query Qi
	 * 
	 * The overall idea is to get the doc IDs matching each query literal and
	 * get the union of the results (or difference for a negative literal).
	 * Bi-word index is used for phrases of size-2. Positional Inverted Index is
	 * used for single tokens.
	 * 
	 * There is a special algorithm to find doc IDs matching phrases of size
	 * greater than 2: for the ith word in the phrase, we find all the
	 * positional postings, each of which include a doc ID and a list of
	 * positions, and minus each position by i. Then we return a document id
	 * only if all of the words in the phrase match the document id and there is
	 * have at least one position that all the words share in their postings.
	 * 
	 * @param query
	 *            Query object
	 * @param pInvertedIndex
	 *            positional inverted index
	 * @param biWordIndex
	 *            bi-word index
	 * @return a list of document IDs that match the query
	 */
	private static List<Integer> getdocIdsMatchingQuery(Query query, PositionalInvertedIndex pInvertedIndex,
			BiWordIndex biWordIndex) {
		// final results
		List<Integer> results = new ArrayList<Integer>();
		for (QueryLiteral queryLiteral : query.getQueryLiterals()) {
			// docIds that match the current query literal that is being
			// processed
			List<Integer> docIds = new ArrayList<Integer>();
			if (!queryLiteral.isPhrase()) {
				// use positional inverted index for single tokens
				// get all the postings that match the token
				List<PositionalPosting> positionalPostings = pInvertedIndex.getPostings(PorterStemmer
						.processToken(Utils.removeHyphens(Utils.processWord(queryLiteral.getTokens().get(0)))));
				if (positionalPostings != null) {
					for (PositionalPosting positionalPosting : positionalPostings) {
						// add the docId in each posting to docIds
						docIds.add(positionalPosting.getDocumentId());
					}
				}
			} else if (queryLiteral.isPhrase() && queryLiteral.getTokens().size() == 2) {
				// use bi-word index for 2-word-phrases
				List<Integer> postings = biWordIndex.getPostings(
						PorterStemmer.processToken(Utils.processWord(queryLiteral.getTokens().get(0))) + PorterStemmer
								.processToken(Utils.removeHyphens(Utils.processWord(queryLiteral.getTokens().get(1)))));
				if (postings != null) {
					docIds.addAll(postings);
				}
			} else {
				// use positional inverted index for other phrases
				// postings: postings that match all the query literals that
				// have been processed
				List<PositionalPosting> postings = new ArrayList<PositionalPosting>();
				for (int i = 0; i < queryLiteral.getTokens().size(); i++) {
					String token = queryLiteral.getTokens().get(i);
					// currentPostings: postings that match the query literals
					// that is currently being processed
					List<PositionalPosting> currentPostings = pInvertedIndex
							.getPostings(Utils.removeHyphens(PorterStemmer.processToken(Utils.processWord(token))));
					if (currentPostings == null) {
						// no possible results
						break;
					}

					if (postings.isEmpty()) {
						// when we are processing the first literal
						postings = currentPostings;
					} else {
						// Use two pointers, j and k, to point to postings and
						// currentPostings, respectively. Increment the pointers
						// until they point to postings with the same
						// documentID, and repeat.
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
									// minus each position in
									// currentPostingsPositions by i
									currentPostingsPositions.set(l, currentPostingsPositions.get(l) - i);
								}
								// get the intersection of the positions
								List<Integer> newPostingsPositions = getIntersection(postingsPositions,
										currentPostingsPositions);
								if (!newPostingsPositions.isEmpty()) {
									// create a new PositionalPosting object and
									// save it in newPostings
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
					// add document id in each posting
					docIds.add(posting.getDocumentId());
				}
			}

			if (results.isEmpty()) {
				results = docIds;
			} else {
				if (queryLiteral.isPositive()) {
					// get the intersection of existing results and result of
					// the current query literal and if query literal is
					// positive
					results = getIntersection(results, docIds);
				} else {
					// get the difference of existing results and result of
					// the current query literal and if query literal is
					// negative
					// a negative query literal cannot be the first literal in a query
					results = getDifference(results, docIds);
				}
			}
		}

		return results;
	}

	/**
	 * Get the union of two sorted integer lists.
	 * 
	 * @param intListA sorted integer list
	 * @param intListB sorted integer list
	 * @return integer list that has the union of two lists
	 */
	private static List<Integer> getUnion(List<Integer> intListA, List<Integer> intListB) {
		int i = 0, j = 0;
		List<Integer> results = new ArrayList<Integer>();
		while (i < intListA.size() && j < intListB.size()) {
			if (intListA.get(i) < intListB.get(j)) {
				results.add(intListA.get(i++));
			} else if (intListA.get(i) > intListB.get(j)) {
				results.add(intListB.get(j++));
			} else {
				results.add(intListA.get(i++));
				j++;
			}
		}
		while (i < intListA.size()) {
			results.add(intListA.get(i++));
		}
		while (j < intListB.size()) {
			results.add(intListB.get(j++));
		}

		return results;
	}

	/**
	 * Get the intersection of two sorted integer lists.
	 * 
	 * @param intListA sorted integer list
	 * @param intListB sorted integer list
	 * @return integer list that has the intersection of two lists
	 */
	private static List<Integer> getIntersection(List<Integer> intListA, List<Integer> intListB) {
		int i = 0, j = 0;
		List<Integer> results = new ArrayList<Integer>();
		while (i < intListA.size() && j < intListB.size()) {
			if (intListA.get(i) < intListB.get(j)) {
				i++;
			} else if (intListA.get(i) > intListB.get(j)) {
				j++;
			} else {
				results.add(intListA.get(i++));
				j++;
			}
		}

		return results;
	}

	/**
	 * Get the difference of two sorted integer lists.
	 * 
	 * @param intListA sorted integer list
	 * @param intListB sorted integer list
	 * @return integer list that has the difference of two lists
	 */
	private static List<Integer> getDifference(List<Integer> intListA, List<Integer> intListB) {
		int i = 0, j = 0;
		List<Integer> results = new ArrayList<Integer>();
		while (i < intListA.size() && j < intListB.size()) {
			if (intListA.get(i) < intListB.get(j)) {
				results.add(intListA.get(i++));
			} else if (intListA.get(i) > intListB.get(j)) {
				j++;
			} else {
				i++;
				j++;
			}
		}

		while (i < intListA.size()) {
			results.add(intListA.get(i++));
		}

		return results;
	}

}
