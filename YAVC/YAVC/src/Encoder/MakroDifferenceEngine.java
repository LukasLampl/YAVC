package Encoder;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Main.config;

public class MakroDifferenceEngine {
	/*
	 * Purpose: Get the differences between the MakroBlocks of two lists
	 * Return Type: ArrayList<MakroBlocks> => List of differences
	 * Params: ArrayList<MakroBlock> list1 => List1 to be compared with list2;
	 * 			ArrayList<MakroBlock> list2 => List2 to be compared with list1;
	 * 			BufferedImage img => Image in which the current MakroBlocks are located in;
	 * 			int EDGE_TOLERANCE => Tolerance with which edges are filtered
	 */
	public ArrayList<MakroBlock> get_MakroBlock_difference(ArrayList<MakroBlock> list1, ArrayList<MakroBlock> list2, BufferedImage img, int EDGE_TOLERANCE) {
		ArrayList<MakroBlock> diffs = new ArrayList<MakroBlock>();
		
		if (list1.size() != list2.size()) {
			System.err.println("List size not equal!");
			return null;
		}
		
		for (int i = 0; i < list1.size(); i++) {
			int colors1[][] = list1.get(i).getColors();
			int colors2[][] = list2.get(i).getColors();
			
			for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
				for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
					if (colors1[y][x] != colors2[y][x]) {
						list2.get(i).setEdgeBlock(is_edge(list2.get(i), img, EDGE_TOLERANCE));
						diffs.add(list2.get(i));
						y = config.MAKRO_BLOCK_SIZE;
						break;
					}
				}
			}
		}
		
		return diffs;
	}
	
	/*
	 * Purpose: Validate whether the MakroBlock is an edge or outline or not
	 * Return Type: boolean => true = is edge or outline; false = not an edge or outline
	 * Params: MakroBlock block => MakroBlock to be validated;
	 * 			BufferedImage img => Image in which the MakroBlock can be found;
	 * 			int EDGE_TOLERANCE => Tolerance with which edges are filtered
	 */
	private boolean is_edge(MakroBlock block, BufferedImage img, int EDGE_TOLERANCE) {
		int strength = 0;
		
		for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
			for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
				strength += calculate_sobel_operator(img, x + block.getPosition().x, y + block.getPosition().y);
			}
		}
		
		return strength > ((config.MBS_SQ * 64) / EDGE_TOLERANCE) ? true : false;
	}
	
	/*
	 * Purpose: Calculate the sobel operator for edge and outline detection
	 * Return Type: int => magnitude of the found edges
	 * Params: BufferedImage img => Image to get the pixels from;
	 * 			int x => Position x in the image;
	 * 			int y => Position y int the image
	 */
	private static int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    private static int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
	
	private int calculate_sobel_operator(BufferedImage img, int x, int y) {
		int gx = 0, gy = 0;
		
		for (int j = -1; j <= 1; j++) {
            for (int i = -1; i <= 1; i++) {
                int pixelX = Math.min(Math.max(x + i, 0), img.getWidth() - 1);
                int pixelY = Math.min(Math.max(y + j, 0), img.getHeight() - 1);
                int gray = img.getRGB(pixelX, pixelY) & 0xFF;
                gx += sobelX[j + 1][i + 1] * gray;
                gy += sobelY[j + 1][i + 1] * gray;
            }
        }
		
        return Math.abs(gx) + Math.abs(gy);
	}
}
