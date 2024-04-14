package Encoder;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Scene {
	/*
	 * Purpose: Detect whether the scene has changed / shot has changed between two images
	 * Return Type: boolean => true = scene changes; false = scene almost the same
	 * Params: BufferedImage img1 => Previous image;
	 * 			BufferedImage img2 => Current image
	 * Note: THIS ALGORITHM MIGHT FAIL OR FALSE TRIGGER SINCE IMAGES CAN'T BE "COMPARED"
	 */
	public boolean scene_change_detected(BufferedImage img1, BufferedImage img2) {
		int[][] histogram1 = get_histogram(img1);
		int[][] histogram2 = get_histogram(img2);
		
		int difference = compute_difference_between_histograms(histogram1, histogram2);
		
		//normalize the values
		double normalDiff = difference / (double)((img1.getWidth() * img1.getHeight() * 2));
		
		System.out.println(normalDiff);
		
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
	
	/*
	 * Purpose: Creates a representative histogram of the given image
	 * Return Type: int[][] => 3 histograms with all 3 colors (r, g, b)
	 * Params: BufferedImage img => Image from which a histogram should be created
	 */
	private int[][] get_histogram(BufferedImage img) {
		int[][] histogram = new int[3][256]; //3 for 'r', 'g', 'b'
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				Color col = new Color(img.getRGB(x, y));
				histogram[0][col.getRed()]++;
				histogram[1][col.getGreen()]++;
				histogram[2][col.getBlue()]++;
			}
		}
		
		return histogram;
	}
}
