import java.util.List;

public class QueryLiteral {
	public boolean isPhrase() {
		return isPhrase;
	}

	public void setPhrase(boolean isPhrase) {
		this.isPhrase = isPhrase;
	}

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	private boolean isPhrase;
	private List<String> tokens;
}