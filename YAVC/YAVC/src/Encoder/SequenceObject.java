package Encoder;

import java.util.ArrayList;

public class SequenceObject {
	private ArrayList<YCbCrMakroBlock> differences = null;
	private ArrayList<Vector> vecs = null;
	
	public ArrayList<YCbCrMakroBlock> getDifferences() {
		return differences;
	}
	public void setDifferences(ArrayList<YCbCrMakroBlock> differences) {
		this.differences = differences;
	}
	public ArrayList<Vector> getVecs() {
		return vecs;
	}
	public void setVecs(ArrayList<Vector> vecs) {
		this.vecs = vecs;
	}
}
