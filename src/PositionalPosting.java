
import java.util.ArrayList;
import java.util.List;

public class PositionalPosting {
	private int documentId;
	private List<Integer> positions;	

	public PositionalPosting() {
		this.positions = new ArrayList<Integer>();
	}

	public int getDocumentId() {
		return this.documentId;
	}

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	public List<Integer> getPositions() {
		return this.positions;
	}

	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}
}
