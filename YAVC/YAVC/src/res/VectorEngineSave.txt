package Encoder;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Main.config;

public class VectorEngine {
	/*
	 * Purpose: Get the MovementVectors between two frames and removes a match from the differences
	 * 			THIS FUNCTION IS IN DEVELOPMENT (NO BLOCK-MOTION-ESTIMATION-ALGORITHM USED!!!)
	 * Return Type: ArrayList<Vector> => Movement vectors
	 * Params: ArrayList<MakroBlock> prevFrameBlocks => MakroBlocks from the previous frame;
	 * 			ArrayList<MakroBlock> diff => Differences between the previous and current frame
	 * 			(Vectors are applied to the differences);
	 * 			int grayscaleTolerance => Tolerance for edge detection;
	 * 			double magnitudeTolerance => Tolerance for gradient matching
	 */
	public ArrayList<Vector> calculate_movement_vectors(ArrayList<MakroBlock> prevFrameBlocks, ArrayList<MakroBlock> diff, int grayscaleTolerance, double magnitudeTolerance) {
		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		
		ArrayList<Vector> vectors = new ArrayList<Vector>(diff.size());
		ArrayList<Future<Vector>> fvecs = new ArrayList<Future<Vector>>(vectors.size());
		
		for (MakroBlock block : diff) {
			Callable<Vector> task = () -> {
				MakroBlock mostEqual = get_most_equal_MakroBlock(block, prevFrameBlocks, grayscaleTolerance, magnitudeTolerance);
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
		
		return vectors;
	}
	
	/*
	 * Purpose: Search the most equal MakroBlock from the previous frame
	 * Return Type: MakroBlock => Most similar MakroBlock
	 * Params: MakroBlock blockToBeSearched => MakroBlock to be matched in the previous frame;
	 * 			ArrayList<MakroBlock> diff => Differences between previous and current frame;
	 * 			int grayscaleTolerance => Tolerance for edge detection;
	 * 			double magnitudeTolerance => Tolerance for gradient matching	
	 */
	private MakroBlock get_most_equal_MakroBlock(MakroBlock blockToBeSearched, ArrayList<MakroBlock> diff, int grayscaleTolerance, double magnitudeTolerance) {
		MakroBlock mostEqual = null;
		int lowestSAD = Integer.MAX_VALUE;
		int searchRange = 8; //Range in Blocks x + searchRange; x - searchRange; y + searchRange; y - searchRange;
		
		int minX = blockToBeSearched.getPosition().x - (config.MAKRO_BLOCK_SIZE * searchRange);
		int maxX = blockToBeSearched.getPosition().x + (config.MAKRO_BLOCK_SIZE * searchRange);
		int minY = blockToBeSearched.getPosition().y - (config.MAKRO_BLOCK_SIZE * searchRange);
		int maxY = blockToBeSearched.getPosition().y + (config.MAKRO_BLOCK_SIZE * searchRange);

		for (MakroBlock block : diff) {
			int posX = block.getPosition().x;
			int posY = block.getPosition().y;
			
			if (posX < minX
				&& posY < minY) {
				continue;
			} else if (posX > maxX
				&& posY > maxY) {
				break;
			} else if (posX > maxX
				|| posY > maxY) {
				continue;
			}
			
			int SAD = get_SAD_of_colors(blockToBeSearched.getColors(), block.getColors(), grayscaleTolerance, magnitudeTolerance);
			
			if (SAD < lowestSAD) {
				lowestSAD = SAD;
				mostEqual = block;
			}
		}
		
		return mostEqual;
	}
	/*
	 * Purpose: Calculate the equality between an array of colors (in Integer form)
	 * Return Type: int => Equality; The lower the more equal the colors; -1 = out of tolerance
	 * Params: int[][] colors1 => List 1 of int colors;
	 * 			int[][] colors2 => Second list of int colors;
	 * 			int grayscaleTolerance => Tolerance for edge detection;
	 * 			double magnitudeTolerance => Tolerance for gradient matching
	 */
	public int get_SAD_of_colors(int[][] colors1, int[][] colors2, int grayscaleTolerance, double magnitudeTolerance) {
		int res = 0;
		int tolerance = 6;
		
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
				
				//Color validation (rgbA - Colorspace)
				int deltaRed = Math.abs(prevRed - curRed);
				int deltaGreen = Math.abs(prevGreen - curGreen);
				int deltaBlue = Math.abs(prevBlue - curBlue);
				
				//Edge detection
				int prevGray = (int) (0.299 * prevRed + 0.587 * prevGreen + 0.114 * prevBlue);
				int curGray = (int) (0.299 * curRed + 0.587 * curGreen + 0.114 * curBlue);
				int deltaGrayscale = Math.abs(prevGray - curGray);
				
				//Gradient validation
				double prevGrad = compute_gradient_magnitude(colors1);
				double curGrad = compute_gradient_magnitude(colors2);
				double deltaMagnitude = (double)Math.abs(prevGrad - curGrad);
				
				if (deltaGrayscale > grayscaleTolerance
					|| deltaMagnitude > magnitudeTolerance) {
					return Integer.MAX_VALUE;
				}
				
				if (deltaRed > tolerance
					|| deltaGreen > tolerance
					|| deltaBlue > tolerance) {
					return Integer.MAX_VALUE;
				}
				
				res += deltaRed + deltaGreen + deltaBlue;
			}
		}
		
		return res;
	}
	
	/*
	 * Purpose: Calculate the gradient magnitude by ratio of the Block edges with the Euclid distance
	 * Return Type: double => Euclid distance
	 * Params: int[][] colors => Color array from the MakroBlock
	 */
    private static double compute_gradient_magnitude(int[][] colors) {
    	double displacement = 50.0D;
    	double[] results = new double[10];
        int colTL = colors[0][0]; //TOP-LEFT
        int colTM = colors[(int)config.MAKRO_BLOCK_SIZE / 2][0]; //TOP-MIDDLE
        int colTR = colors[config.MAKRO_BLOCK_SIZE - 1][0]; //TOP-RIGHT
        int colLM = colors[0][(int)config.MAKRO_BLOCK_SIZE / 2]; //LEFT-MIDDLE
        int colBL = colors[0][config.MAKRO_BLOCK_SIZE - 1]; //BOTTOM-LEFT
        int colBM = colors[(int)config.MAKRO_BLOCK_SIZE / 2][config.MAKRO_BLOCK_SIZE - 1]; //BOTTOM-MIDDLE
        int colBR = colors[config.MAKRO_BLOCK_SIZE - 1][config.MAKRO_BLOCK_SIZE - 1]; //BOTTOM-RIGHT
        int colRM = colors[config.MAKRO_BLOCK_SIZE - 1][(int)config.MAKRO_BLOCK_SIZE / 2]; //RIGHT-MIDDLE
        
        results[0] = ((double)colTL / (double)colTM) * displacement; //TOP-LEFT-X
        results[1] = ((double)colTM / (double)colTR) * displacement; //TOP-RIGHT-X
        results[2] = ((double)colLM / (double)colRM) * displacement; //MIDDLE-X
        results[3] = ((double)colBL / (double)colBM) * displacement; //BOTTOM-LEFT-X
        results[4] = ((double)colBM / (double)colBR) * displacement; //BOTTOM-RIGHT-X
        results[5] = ((double)colTL / (double)colLM) * displacement; //LEFT-TOP-Y
        results[6] = ((double)colLM / (double)colBL) * displacement; //LEFT-BOTTOM-Y
        results[7] = ((double)colTM / (double)colBM) * displacement; //CENTER-Y
        results[8] = ((double)colTR / (double)colRM) * displacement; //RIGHT-TOP-Y
        results[9] = ((double)colRM / (double)colBR) * displacement; //RIGHT-BOTTOM-Y

        double x = (results[0] + results[1]) * (results[2] + results[3]) + results[4];
        double y = (results[5] + results[6]) * (results[7] + results[8]) + results[9];
        
        double gradX = x - y;
        double gradY = y - x;
        
        return Math.sqrt(gradX * gradX + gradY * gradY);
    }
}