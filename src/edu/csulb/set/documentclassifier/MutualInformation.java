package edu.csulb.set.documentclassifier;

public class MutualInformation implements Comparable<MutualInformation> {
	
	public MutualInformation(String term, double mutualInformation) {
		this.term = term;
		this.mutualInformation = mutualInformation;
	}

	private String term;
	private double mutualInformation;

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public double getMutualInformation() {
		return mutualInformation;
	}

	public void setMutualInformation(double mutualInformation) {
		this.mutualInformation = mutualInformation;
	}

	public int compareTo(MutualInformation other) {
		return Double.compare(this.mutualInformation, other.mutualInformation);
	}

}
