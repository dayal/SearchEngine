package com.csulb.edu.set.query;
import java.util.List;

public class QueryLiteral {
	
	private boolean isPhrase;
	private List<String> tokens;
	private boolean isPositive;
	
	public boolean isPositive() {
		return isPositive;
	}

	public void setPositive(boolean isPositive) {
		this.isPositive = isPositive;
	}

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

}