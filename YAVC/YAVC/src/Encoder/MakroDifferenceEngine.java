package Encoder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Main.config;

public class MakroDifferenceEngine {
	private ColorManager COLOR_MANAGER = new ColorManager();
	
	/*
	 * Purpose: Get the differences between the MakroBlocks of two lists
	 * Return Type: ArrayList<MakroBlocks> => List of differences
	 * Params: ArrayList<MakroBlock> list1 => List1 to be compared with list2;
	 * 			ArrayList<MakroBlock> list2 => List2 to be compared with list1;
	 * 			BufferedImage img => Image in which the current MakroBlocks are located in;
	 */
	public ArrayList<MakroBlock> get_MakroBlock_difference(ArrayList<MakroBlock> list1, ArrayList<MakroBlock> list2, BufferedImage img) {
		ArrayList<MakroBlock> diffs = new ArrayList<MakroBlock>();
		ArrayList<Future<MakroBlock>> fmbs = new ArrayList<Future<MakroBlock>>();
		
		if (list1.size() != list2.size()) {
			System.err.println("List size not equal!");
			return null;
		}
		
		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		
		try {
			for (int i = 0; i < list1.size(); i++) {
				final int index = i;
				
				Callable<MakroBlock> task = () -> {
					int colors1[][] = list1.get(index).getColors();
					int colors2[][] = list2.get(index).getColors();
					
					for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
						for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
							YCbCrColor col1 = this.COLOR_MANAGER.convert_RGB_to_YCbCr(new Color(colors1[y][x]));
							YCbCrColor col2 = this.COLOR_MANAGER.convert_RGB_to_YCbCr(new Color(colors2[y][x]));
							
							double deltaY = Math.abs(col1.getY() - col2.getY());
							double deltaCb = Math.abs(col1.getCb() - col2.getCb());
							double deltaCr = Math.abs(col1.getCr() - col2.getCr());
							
							if (deltaY > 3 || deltaCb > 8 || deltaCr > 8) {
								if (img != null) {
									list2.get(index).setEdgeBlock(is_edge(list2.get(index), img, 50));
								}
								
								return list2.get(index);
							}
						}
					}
					
					return null;
				};
				
				fmbs.add(executor.submit(task));
			}
			
			for (Future<MakroBlock> f : fmbs) {
				try {
					MakroBlock mb = f.get();
					
					if (mb != null) {
						diffs.add(mb);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			executor.shutdown();
			
			executor.shutdown();
		} catch (Exception e) {
			executor.shutdown();
			e.printStackTrace();
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
	private static int[][] sobelX = {{1, 0, -1}, {2, 0, +2}, {1, 0, -1}};
    private static int[][] sobelY = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};
	
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
