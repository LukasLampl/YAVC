package Encoder;

import java.util.ArrayList;

public class MakroDifferenceEngine {
	/*
	 * Purpose: Get the differences between the MakroBlocks of two lists
	 * Return Type: ArrayList<MakroBlocks> => List of differences
	 * Params: ArrayList<MakroBlock> list1 => List1 to be compared with list2;
	 * 			ArrayList<MakroBlock> list2 => List2 to be compared with list1
	 */
	public ArrayList<MakroBlock> get_MakroBlock_difference(ArrayList<MakroBlock> list1, ArrayList<MakroBlock> list2) {
		ArrayList<MakroBlock> diffs = new ArrayList<MakroBlock>();
		
		if (list1.size() != list2.size()) {
			System.err.println("List size not equal!");
			return null;
		}
		
		for (int i = 0; i < list1.size(); i++) {
			if (!list1.get(i).getID().equals(list2.get(i).getID())) {
				diffs.add(list2.get(i));
			}
		}
		
		return diffs;
	}
}
