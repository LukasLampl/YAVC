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
	public ArrayList<Vector> calculate_movement_vectors(BufferedImage prevFrame, ArrayList<YCbCrMakroBlock> diff, int maxMADTolerance) {
		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		
		ArrayList<Vector> vectors = new ArrayList<Vector>(diff.size());
		ArrayList<Future<Vector>> fvecs = new ArrayList<Future<Vector>>(vectors.size());
		
		try {
			for (YCbCrMakroBlock block : diff) {
				Callable<Vector> task = () -> {
					if (block.isEdgeBlock() == true) {
						return null;
					}
					
					YCbCrMakroBlock mostEqual = get_most_equal_MakroBlock(block, prevFrame, maxMADTolerance);
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
	private YCbCrMakroBlock get_most_equal_MakroBlock(YCbCrMakroBlock blockToBeSearched, BufferedImage prevFrame, int maxMADTolerance) {
		YCbCrMakroBlock mostEqualBlock = null;
		MakroBlockEngine makroBlockEngine = new MakroBlockEngine();
		int searchWindow = 32;
		
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
				YCbCrMakroBlock YCbCrBlockAtPosP = makroBlockEngine.convert_MakroBlock_to_YCbCrMarkoBlock(blockAtPosP);
				double sad = get_SAD_of_colors(YCbCrBlockAtPosP.getColors(), blockToBeSearched.getColors());
				
				if (sad < lowestSAD) {
					lowestSAD = sad;
					mostEqualBlock = YCbCrBlockAtPosP;
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
			YCbCrMakroBlock YCbCrBlockAtPosP = makroBlockEngine.convert_MakroBlock_to_YCbCrMarkoBlock(blockAtPosP);
			double sad = get_SAD_of_colors(YCbCrBlockAtPosP.getColors(), blockToBeSearched.getColors());
			
			if (sad < lowestSAD) {
				lowestSAD = sad;
				mostEqualBlock = YCbCrBlockAtPosP;
			}
		}
		
//		if (lowestMAD > maxMADTolerance
//			|| mostEqualBlock == null) {
//			return null;
//		}
//		
//		double grad1 = compute_gradient_ratio(blockToBeSearched.getColors());
//		double grad2 = compute_gradient_ratio(mostEqualBlock.getColors());
//		System.out.println(Math.abs(grad1 - grad2));
//		if (Math.abs(grad1 - grad2) > 0.08) {
//			return null;
//		}
		
		return mostEqualBlock;
	}
	
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
		
		for (int y = 0; y < colors2.length; y++) {
			for (int x = 0; x < colors1.length; x++) {
				YCbCrColor prevCol = colors1[y][x];
				YCbCrColor curCol = colors2[y][x];
				
				resY += Math.abs(prevCol.getY() - curCol.getY());
				resCb += Math.abs(prevCol.getCb() - curCol.getCb());
				resCr += Math.abs(prevCol.getCr() - curCol.getCr());
			}
		}
		
		return (resY + resCb + resCr) / Math.pow(Math.pow(config.MAKRO_BLOCK_SIZE, 2), 2);
	}
	
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
			
			if (vec.getSpanX() > 0 && vec.getSpanY() > 0) {
				g2d.setColor(Color.BLUE);
			} else if (vec.getSpanX() > 0 && vec.getSpanY() < 0) {
				g2d.setColor(Color.RED);
			} else if (vec.getSpanX() < 0 && vec.getSpanY() < 0) {
				g2d.setColor(Color.GREEN);
			} else if (vec.getSpanX() < 0 && vec.getSpanY() > 0) {
				g2d.setColor(Color.YELLOW);
			} else {
				g2d.setColor(Color.black);
			}
			
			g2d.drawLine(vec.getStartingPoint().x, vec.getStartingPoint().y, vecEndX, vecEndY);
		}
		
		g2d.dispose();
		
		return render;
	}
}