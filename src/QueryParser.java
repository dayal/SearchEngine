import java.util.ArrayList;
import java.util.List;

public class QueryParser {
	// private static final String singleToken = "[a-zA-Z0-9]+";
	// private static final String phrase = "\"([a-zA-Z0-9]+ )*[a-zA-Z0-9]+\"";
	// private static final String literal = singleToken + "|" + phrase;
	// private static final String query = "((" + literal + ") )*" + "(" +
	// literal + ")";

	public static List<Query> parseQuery(String queryInput) {
		List<Query> queryList = new ArrayList<Query>();
		String[] queryStrings = queryInput.split(" \\+ ");
		for (String queryString : queryStrings) {
			Query query = new Query();
			List<QueryLiteral> queryLiterals = new ArrayList<QueryLiteral>();
			String[] literalStrings = queryString.split(" ");

			int i = 0;
			while (i < literalStrings.length) {
				QueryLiteral queryLiteral = new QueryLiteral();
				queryLiteral.setTokens(new ArrayList<String>());
				if (literalStrings[i].startsWith("\"")) {
					queryLiteral.setPhrase(true);
					queryLiteral.getTokens().add(literalStrings[i].substring(1));
					i++;

					while (!literalStrings[i].endsWith("\"")) {
						queryLiteral.getTokens().add(literalStrings[i]);
						i++;
					}
					
					queryLiteral.getTokens().add(literalStrings[i].substring(0, literalStrings[i].length() - 1));
					i++;
				} else {
					queryLiteral.setPhrase(false);
					queryLiteral.getTokens().add(literalStrings[i]);
					i++;
				}
				queryLiterals.add(queryLiteral);
			}
			query.setQueryLiterals(queryLiterals);
			queryList.add(query);
		}

		return queryList;
	}

	public static void main(String[] args) {
		List<Query> queries = parseQuery("shakes \"Jamba Juice\" + smoothies mango");
		for (Query query : queries) {
			System.out.println("Query:");
			for (QueryLiteral queryLiteral : query.getQueryLiterals()) {
				System.out.println("phrase: " + queryLiteral.isPhrase());
				System.out.println(queryLiteral.getTokens());
			}
		}
	}
}
