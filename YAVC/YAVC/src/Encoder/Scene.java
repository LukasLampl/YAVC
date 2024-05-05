/////////////////////////////////////////////////////////////
///////////////////////    LICENSE    ///////////////////////
/////////////////////////////////////////////////////////////
/*
The YAVC video / frame compressor compresses frames.
Copyright (C) 2024  Lukas Nian En Lampl

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package Encoder;

import java.awt.Dimension;

public class Scene {
	private int[][] prevHist = new int[3][256];
	private int[][] curHist = new int[3][256];
	
	/*
	 * Purpose: Detect whether the scene has changed / shot has changed between two images
	 * Return Type: boolean => true = scene changes; false = scene almost the same
	 * Params: int[][] histogram1 => Previous histogram;
	 * 			int[][] histogram2 => Current histogram
	 * Note: THIS ALGORITHM MIGHT FAIL OR FALSE TRIGGER SINCE IMAGES CAN'T BE "COMPARED"
	 */
	public boolean scene_change_detected(Dimension dim) {
		if (this.prevHist == null || this.curHist == null) {
			System.err.println("Can't evaluate " + this.prevHist + " and " + this.curHist + "; Due one is NULL!");
			return true;
		}
		
		int difference = compute_difference_between_histograms(this.prevHist, this.curHist);
		System.out.println("SCD: " + difference);
		//normalize the values
		double normalDiff = difference / (double)((dim.width * dim.height));
		System.out.println("SCD: " + normalDiff);
		//If the normalized values are above 1.0 the scene might have changed
		if (normalDiff > 1.0) return true;
		
		return false;
	}
	
	/*
	 * Purpose: Get the absolute difference between two histograms.
	 * 			The difference is calculated by evaluating the values at the same position.
	 * Return Type: int => Absolute difference value
	 * Params: int[][] hist1 => First histogram;
	 * 			int[][] hist2 => Second histogram
	 */
	private int compute_difference_between_histograms(int[][] hist1, int[][] hist2) {
		int deltaRed = 0;
		int deltaGreen = 0;
		int deltaBlue = 0;
		
		for (int n = 0; n < hist1[0].length; n++) {
			deltaRed += Math.abs(hist1[0][n] - hist2[0][n]);
			deltaGreen += Math.abs(hist1[1][n] - hist2[1][n]);
			deltaBlue += Math.abs(hist1[2][n] - hist2[2][n]);
		}
		
		return deltaRed + deltaGreen + deltaBlue;
	}
	
	public void clone_histogram(int[][] target, int[][] org) {
		for (int i = 0; i < org.length; i++) {
			for (int n = 0; n < org[i].length; n++) {
				target[i][n] = org[i][n];
				org[i][n] = 0;
			}
		}
	}
	
	public int[][] get_current_histogram() {
		return this.curHist;
	}
	
	public void shift_histogram() {
		clone_histogram(this.prevHist, this.curHist);
	}	
}
