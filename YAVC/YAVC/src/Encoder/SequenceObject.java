package Encoder;

import java.util.ArrayList;

public class SequenceObject {
	private ArrayList<DCTObject> dct = null;
	private ArrayList<Vector> vecs = null;
	
	public ArrayList<DCTObject> getDCT() {
		return dct;
	}
	public void setDifferences(ArrayList<DCTObject> dct) {
		this.dct = dct;
	}
	public ArrayList<Vector> getVecs() {
		return vecs;
	}
	public void setVecs(ArrayList<Vector> vecs) {
		this.vecs = vecs;
	}
}
