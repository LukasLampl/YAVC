package Encoder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
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
	public ArrayList<Vector> calculate_movement_vectors(ArrayList<BufferedImage> refs, ArrayList<YCbCrMakroBlock> diff, int maxSADTolerance) {
		int threads = Runtime.getRuntime().availableProcessors();
		final int maxGuesses = refs.size();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		
		ArrayList<Vector> vectors = new ArrayList<Vector>(diff.size());
		ArrayList<Future<Vector>> fvecs = new ArrayList<Future<Vector>>(vectors.size());
		
		try {
			for (YCbCrMakroBlock block : diff) {
				Callable<Vector> task = () -> {
					if (block.isEdgeBlock() == true) {
						return null;
					}
					
					YCbCrMakroBlock[] bestGuesses = new YCbCrMakroBlock[maxGuesses];
					
					for (int i = 0; i < maxGuesses; i++) {
						bestGuesses[i] = get_most_equal_MakroBlock(block, refs.get(i), maxSADTolerance);
						
						if (bestGuesses[i] != null) {
							bestGuesses[i].setReferenceDrawback(maxGuesses - i);
						}
					}
					
					YCbCrMakroBlock bestGuess = evaluate_best_guesses(bestGuesses);
					Vector vec = null;
					
					if (bestGuess != null) {
						vec = new Vector();
						vec.setAppendedBlock(block);
						vec.setMostEqualBlock(bestGuess);
						vec.setReferenceDrawback(bestGuess.getReferenceDrawback());
						vec.setStartingPoint(bestGuess.getPosition());
						vec.setSpanX(block.getPosition().x - bestGuess.getPosition().x);
						vec.setSpanY(block.getPosition().y - bestGuess.getPosition().y);
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
			executor.shutdown();
		}
		
		return vectors;
	}
	
	private YCbCrMakroBlock evaluate_best_guesses(YCbCrMakroBlock bestGuesses[]) {
		YCbCrMakroBlock bestGuess = null;
		
		for (int i = 0; i < bestGuesses.length; i++) {
			if (bestGuesses[i] == null) {
				continue;
			}
			
			if (bestGuess == null) {
				bestGuess = bestGuesses[i];
			} else if (bestGuesses[i].getSAD() < bestGuess.getSAD()) {
				bestGuess = bestGuesses[i];
			}
		}
		
		return bestGuess;
	}
	
	/*
	 * Purpose: Search the most equal MakroBlock from the previous frame using Three-Step-Search
	 * Return Type: YCbCrMakroBlock => Most similar MakroBlock
	 * Params: YCbCrMakroBlock blockToBeSearched => MakroBlock to be matched in the previous frame;
	 * 			BufferedImage prevFrame => Image of the previous frame;
	 * 			int maxSADTolerance => Max tolerance of the SAD
	 */
	private YCbCrMakroBlock get_most_equal_MakroBlock(YCbCrMakroBlock blockToBeSearched, BufferedImage prevFrame, int maxSADTolerance) {
		YCbCrMakroBlock mostEqualBlock = null;
		MakroBlockEngine makroBlockEngine = new MakroBlockEngine();
		int searchWindow = 48;
		
		Point blockCenter = new Point(blockToBeSearched.getPosition().x + config.MAKRO_BLOCK_SIZE / 2, blockToBeSearched.getPosition().y + config.MAKRO_BLOCK_SIZE / 2);
		Point minPos = new Point(blockCenter.x - searchWindow, blockCenter.y - searchWindow);
		Point maxPos = new Point(blockCenter.x + searchWindow, blockCenter.y + searchWindow);
		
		Point[] searchPositions = new Point[7];
		
		int step = 4;
		Point initPos = new Point(0, 0);
		double lowestSAD = Double.MAX_VALUE;
		
		while (step > 1) {
			searchPositions = get_hexagon_points(step, blockCenter);
			
			for (Point p : searchPositions) {
				if (p.x < minPos.x || p.y < minPos.y
					|| p.x > maxPos.x || p.y > maxPos.y) {
					continue;
				}
				
				MakroBlock blockAtPosP = makroBlockEngine.get_single_makro_block(p, prevFrame);
				YCbCrMakroBlock YCbCrBlockAtPosP = makroBlockEngine.convert_single_MakroBlock_to_YCbCrMakroBlock(blockAtPosP);
				double sad = get_SAD_of_colors(YCbCrBlockAtPosP.getColors(), blockToBeSearched.getColors());
				
				if (sad < lowestSAD) {
					lowestSAD = sad;
					mostEqualBlock = YCbCrBlockAtPosP;
					mostEqualBlock.setSAD(sad);
					initPos = p;
				}
			}
			
			if (initPos.equals(blockCenter)) {
				step = step / 2;
				continue;
			}
			
			blockCenter = initPos;
		}
		
		searchPositions = new Point[5];
		searchPositions[0] = blockCenter;
		searchPositions[1] = new Point(blockCenter.x + step, blockCenter.y);
		searchPositions[2] = new Point(blockCenter.x - step, blockCenter.y);
		searchPositions[3] = new Point(blockCenter.x, blockCenter.y + step);
		searchPositions[4] = new Point(blockCenter.x, blockCenter.y - step);
		
		for (Point p : searchPositions) {
			MakroBlock blockAtPosP = makroBlockEngine.get_single_makro_block(p, prevFrame);
			YCbCrMakroBlock YCbCrBlockAtPosP = makroBlockEngine.convert_single_MakroBlock_to_YCbCrMakroBlock(blockAtPosP);
			double sad = get_SAD_of_colors(YCbCrBlockAtPosP.getColors(), blockToBeSearched.getColors());
			
			if (sad < lowestSAD) {
				lowestSAD = sad;
				mostEqualBlock = YCbCrBlockAtPosP;
				mostEqualBlock.setSAD(sad);
			}
		}
		
		if (lowestSAD > maxSADTolerance) {
			return null;
		}
		
		return mostEqualBlock;
	}
	
	/*
	 * Purpose: Get all search points for hexagon search
	 * Return Type: Point[] => Point array with all positions
	 * Params: int radius => Radius of hexagon;
	 * 			Point center => Center of the hexagon
	 */
	private Point[] get_hexagon_points(int radius, Point center) {
		Point[] points = new Point[7];
		points[6] = center;
		
		for (int i = 0; i < 6; i++) {
			double rad = Math.PI / 3 * (i + 1);
			points[i] = new Point((int)(Math.cos(rad) * radius + center.x), (int)(Math.sin(rad) * radius + center.y));
		}
		
		return points;
	}
	
	/*
	 * Purpose: Calculate the equality between an array of colors (in Integer form)
	 * Return Type: int => Equality; The lower the more equal the colors; -1 = out of tolerance
	 * Params: int[][] colors1 => List 1 of int colors;
	 * 			int[][] colors2 => Second list of int colors;
	 */
	public double get_SAD_of_colors(YCbCrColor[][] colors1, YCbCrColor[][] colors2) {
		double resY = 0;
		double resCb = 0;
		double resCr = 0;
		double resA = 0;
		
		for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
			for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
				YCbCrColor prevCol = colors1[y][x];
				YCbCrColor curCol = colors2[y][x];

				resY += Math.abs(prevCol.getY() - curCol.getY());
				resCb += Math.abs(prevCol.getCb() - curCol.getCb());
				resCr += Math.abs(prevCol.getCr() - curCol.getCr());
				resA += Math.abs(prevCol.getA() - curCol.getA());
			}
		}
		
		return (Math.pow(resY, 6) + Math.pow(resCb, 2) + Math.pow(resCr, 2) + Math.pow(resA, resA)) / (double)(config.MAKRO_BLOCK_SIZE * config.MAKRO_BLOCK_SIZE);
	}
	
	/*
	 * Purpose: Creates a BufferedImage with all the vector paths visualized
	 * Return Type: BufferedImage => Image with all vectors on it
	 * Params: Dimension dim => Dimension of the image;
	 * 			ArrayList<Vector> vecs => Vectors to be drawn
	 * Note: THIS FUNCTION IS NOT A CRUCIAL PART OF THE PROGRAM
	 */
	public BufferedImage construct_vector_path(Dimension dim, ArrayList<Vector> vecs) {
		BufferedImage render = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = render.createGraphics();
		g2d.setColor(Color.red);
		
		for (Vector vec : vecs) {
			int vecEndX = vec.getStartingPoint().x + vec.getSpanX();
			int vecEndY = vec.getStartingPoint().y + vec.getSpanY();
			
			if (vecEndX >= render.getWidth()
				|| vecEndY >= render.getHeight()) {
				continue;
			}
			
			switch (vec.getReferenceDrawback()) {
			case 0:
				g2d.setColor(Color.ORANGE);
				break;
			case 1:
				g2d.setColor(Color.YELLOW);
				break;
			case 2:
				g2d.setColor(Color.BLUE);
				break;
			case 3:
				g2d.setColor(Color.RED);
				break;
			case 4:
				g2d.setColor(Color.GREEN);
				break;
			default:
				g2d.setColor(Color.MAGENTA);
				break;
			}
			
			g2d.drawLine(vec.getStartingPoint().x, vec.getStartingPoint().y, vecEndX, vecEndY);
		}
		
		g2d.dispose();
		
		return render;
	}
}