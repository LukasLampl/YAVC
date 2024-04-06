package Encoder;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Main.config;

public class VectorEngine {
	/*
	 * Purpose: Get the MovementVectors between two frames and removes a match from the differences
	 * Return Type: ArrayList<Vector> => Movement vectors
	 * Params: BufferedImage prevFrame => Previous frame image;
	 * 			ArrayList<MakroBlock> diff => Differences between the previous and current frame
	 * 			(Vectors are applied to the differences);
	 * 			int maxMADTolerance => Max MAD tolerance
	 */
	public ArrayList<Vector> calculate_movement_vectors(BufferedImage prevFrame, ArrayList<MakroBlock> diff, int maxMADTolerance) {
		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		
		ArrayList<Vector> vectors = new ArrayList<Vector>(diff.size());
		ArrayList<Future<Vector>> fvecs = new ArrayList<Future<Vector>>(vectors.size());
		
		try {
			for (MakroBlock block : diff) {
				Callable<Vector> task = () -> {
					if (block.isEdgeBlock()) {
						return null;
					}
					
					MakroBlock mostEqual = get_most_equal_MakroBlock(block, prevFrame, maxMADTolerance);
					Vector vec = null;
					
					if (mostEqual != null) {
						vec = new Vector();
						vec.setAppendedBlock(block);
						vec.setMostEqualBlock(mostEqual);
						vec.setStartingPoint(mostEqual.getPosition());
						vec.setSpanX(block.getPosition().x - mostEqual.getPosition().x);
						vec.setSpanY(block.getPosition().y - mostEqual.getPosition().y);
					}
					
					return vec;
				};
				
				fvecs.add(executor.submit(task));
			}
			
			for (Future<Vector> f : fvecs) {
				try {
					Vector vec = f.get();
					
					if (vec != null) {
						vectors.add(vec);
						diff.remove(vec.getAppendedBlock());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			executor.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			executor.shutdown();
		}
		
		return vectors;
	}
	
	/*
	 * Purpose: Search the most equal MakroBlock from the previous frame using Three-Step-Search
	 * Return Type: MakroBlock => Most similar MakroBlock
	 * Params: MakroBlock blockToBeSearched => MakroBlock to be matched in the previous frame;
	 * 			BufferedImage prevFrame => Image of the previous frame;
	 * 			int maxMADTolerance => Max tolerance of the MAD
	 */
	private MakroBlock get_most_equal_MakroBlock(MakroBlock blockToBeSearched, BufferedImage prevFrame, int maxMADTolerance) {
		MakroBlock mostEqualBlock = null;
		MakroBlockEngine makroBlockEngine = new MakroBlockEngine();
		
		Point blockCenter = new Point(blockToBeSearched.getPosition().x + config.MAKRO_BLOCK_SIZE / 2, blockToBeSearched.getPosition().y + config.MAKRO_BLOCK_SIZE / 2);
		Point[] searchPositions = new Point[5];
		
		int step = 8;
		double lowestMAD = Double.MAX_VALUE;
		Point initPos = blockCenter;
		
		while (step > 1) {
			searchPositions[0] = blockCenter;
			searchPositions[1] = new Point(blockCenter.x + step, blockCenter.y);
			searchPositions[2] = new Point(blockCenter.x - step, blockCenter.y);
			searchPositions[3] = new Point(blockCenter.x, blockCenter.y + step);
			searchPositions[4] = new Point(blockCenter.x, blockCenter.y - step);
			
			for (Point p : searchPositions) {
				MakroBlock blockAtPosP = makroBlockEngine.get_single_makro_block(p, prevFrame);
				double mad = get_MAD_of_colors(blockAtPosP.getColors(), blockToBeSearched.getColors());
				
				if (mad < lowestMAD) {
					lowestMAD = mad;
					initPos = p;
				}
			}
			
			if (initPos.equals(blockCenter)) {
				step = step / 2;
				continue;
			}
			
			blockCenter = initPos;
		}
		
		searchPositions = new Point[9];
		searchPositions[0] = blockCenter;
		searchPositions[1] = new Point(blockCenter.x + step, blockCenter.y);
		searchPositions[2] = new Point(blockCenter.x - step, blockCenter.y);
		searchPositions[3] = new Point(blockCenter.x, blockCenter.y + step);
		searchPositions[4] = new Point(blockCenter.x, blockCenter.y - step);
		searchPositions[5] = new Point(blockCenter.x + step, blockCenter.y - step);
		searchPositions[6] = new Point(blockCenter.x - step, blockCenter.y - step);
		searchPositions[7] = new Point(blockCenter.x - step, blockCenter.y + step);
		searchPositions[8] = new Point(blockCenter.x + step, blockCenter.y + step + step);
		
		for (Point p : searchPositions) {
			MakroBlock blockAtPosP = makroBlockEngine.get_single_makro_block(p, prevFrame);
			double mad = get_MAD_of_colors(blockAtPosP.getColors(), blockToBeSearched.getColors());
			
			if (mad < lowestMAD) {
				lowestMAD = mad;
				mostEqualBlock = blockAtPosP;
			}
		}
		
		if (lowestMAD > maxMADTolerance) {
			return null;
		}
		
		return mostEqualBlock;
	}
	
	/*
	 * Purpose: Calculate the equality between an array of colors (in Integer form)
	 * Return Type: int => Equality; The lower the more equal the colors; -1 = out of tolerance
	 * Params: int[][] colors1 => List 1 of int colors;
	 * 			int[][] colors2 => Second list of int colors;
	 */
	public double get_MAD_of_colors(int[][] colors1, int[][] colors2) {
		int resR = 0;
		int resG = 0;
		int resB = 0;
		
		for (int y = 0; y < colors2.length; y++) {
			for (int x = 0; x < colors1.length; x++) {
				int prevCol = colors1[y][x];
				int curCol = colors2[y][x];
				
				int prevRed = (prevCol >> 16) & 0xFF;
				int prevGreen = (prevCol >> 8) & 0xFF;
				int prevBlue = prevCol & 0xFF;
				
				int curRed = (curCol >> 16) & 0xFF;
				int curGreen = (curCol >> 8) & 0xFF;
				int curBlue = curCol & 0xFF;
				
				resR += Math.abs(prevRed - curRed);
				resG += Math.abs(prevGreen - curGreen);
				resB += Math.abs(prevBlue - curBlue);
			}
		}
		
		return Math.sqrt(Math.pow(resR, 2) + Math.pow(resG, 2) + Math.pow(resB, 2));
	}
}