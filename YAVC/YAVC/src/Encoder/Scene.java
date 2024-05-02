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

import java.awt.Color;
import java.util.HashSet;

import Utils.PixelRaster;

public class Scene {
	private HashSet<Color> currentColors = new HashSet<Color>();
	/*
	 * Purpose: Detect whether the scene has changed / shot has changed between two images
	 * Return Type: boolean => true = scene changes; false = scene almost the same
	 * Params: BufferedImage img1 => Previous image;
	 * 			BufferedImage img2 => Current image
	 * Note: THIS ALGORITHM MIGHT FAIL OR FALSE TRIGGER SINCE IMAGES CAN'T BE "COMPARED"
	 */
	public boolean scene_change_detected(int[][] histogram1, int[][]histogram2) {
		this.currentColors.clear();
		int difference = compute_difference_between_histograms(histogram1, histogram2);
		
		//normalize the values
		double normalDiff = difference / (double)((histogram1.length * histogram1[0].length * 2));
		
		//If the normalized values are above 1.0 the scene might have changed
		if (normalDiff > 1.0) {
			return true;
		}
		
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
		int sum = 0;
		
		for (int i = 0; i < hist1.length; i++) {
			for (int n = 0; n < hist1[i].length; n++) {
				sum += Math.abs(hist1[i][n] - hist2[i][n]);
			}
		}
		
		return sum;
	}
	
	public int get_color_count() {
		return this.currentColors.size();
	}
}
