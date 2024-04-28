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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Utils.PixelRaster;
import Utils.Vector;
import Utils.YCbCrColor;
import Utils.YCbCrMakroBlock;

public class VectorEngine {
	private MakroBlockEngine MAKRO_BLOCK_ENGINE = new MakroBlockEngine();
	/*
	 * Purpose: Get the MovementVectors between two frames and removes a match from the differences
	 * Return Type: ArrayList<Vector> => Movement vectors
	 * Params: BufferedImage prevFrame => Previous frame image;
	 * 			ArrayList<MakroBlock> diff => Differences between the previous and current frame
	 * 			(Vectors are applied to the differences);
	 * 			int maxMADTolerance => Max MAD tolerance
	 */
	public ArrayList<Vector> calculate_movement_vectors(ArrayList<PixelRaster> refs, ArrayList<YCbCrMakroBlock> diff, int maxSADTolerance) {
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
	private YCbCrMakroBlock get_most_equal_MakroBlock(YCbCrMakroBlock blockToBeSearched, PixelRaster prevFrame, int maxSADTolerance) {
		YCbCrMakroBlock mostEqualBlock = null;
		HashSet<Point> set = new HashSet<Point>();
		int searchWindow = 64;
		int blockSize = blockToBeSearched.getSize();
		
		Point blockCenter = new Point(blockToBeSearched.getPosition().x + blockSize / 2, blockToBeSearched.getPosition().y + blockSize / 2);
		Point minPos = new Point(blockCenter.x - searchWindow, blockCenter.y - searchWindow);
		Point maxPos = new Point(blockCenter.x + searchWindow, blockCenter.y + searchWindow);
		
		Point[] searchPositions = new Point[7];
		
		int step = 4;
		Point initPos = new Point(0, 0);
		double lowestSAD = Double.MAX_VALUE;
		
		while (step > 1) {
			searchPositions = get_hexagon_points(step, blockCenter);
			
			for (Point p : searchPositions) {
				if (set.contains(p)) {
					continue;
				}
				
				if (p.x < minPos.x || p.y < minPos.y
					|| p.x > maxPos.x || p.y > maxPos.y
					|| p.x < 0 || p.x >= prevFrame.getWidth()
					|| p.y < 0 || p.y >= prevFrame.getHeight()) {
					continue;
				}
				
				set.add(p);
				YCbCrMakroBlock blockAtPosP = this.MAKRO_BLOCK_ENGINE.get_single_makro_block(p, prevFrame, blockSize);
				double sad = get_SAD_of_colors(blockAtPosP.getColors(), blockToBeSearched.getColors());
				
				if (sad < lowestSAD) {
					lowestSAD = sad;
					mostEqualBlock = blockAtPosP;
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
			if (p.x < minPos.x || p.y < minPos.y
				|| p.x > maxPos.x || p.y > maxPos.y
				|| p.x < 0 || p.x >= prevFrame.getWidth()
				|| p.y < 0 || p.y >= prevFrame.getHeight()) {
				continue;
			}
			
			YCbCrMakroBlock blockAtPosP = this.MAKRO_BLOCK_ENGINE.get_single_makro_block(p, prevFrame, blockSize);
			double sad = get_SAD_of_colors(blockAtPosP.getColors(), blockToBeSearched.getColors());
			
			if (sad < lowestSAD) {
				lowestSAD = sad;
				mostEqualBlock = blockAtPosP;
				mostEqualBlock.setSAD(sad);
			}
		}
		
		switch (blockSize) {
		case 32:
			//Low filtering (Reduce distortion)
			mostEqualBlock = (lowestSAD > maxSADTolerance * 6.8) ? null : mostEqualBlock;
			break;
		case 16:
			//Moderate filtering (Reduce distortion; Get details)
			mostEqualBlock = (lowestSAD > maxSADTolerance * 1.74) ? null : mostEqualBlock;
			break;
		case 8:
			//High filtering (Reduce distortion; Get details; Get Edges)
			mostEqualBlock = (lowestSAD > maxSADTolerance * 0.43) ? null : mostEqualBlock;
			break;
		case 4:
			//Super High filtering (Reduce distortion; Get details; Get Edges; Move necessary)
			mostEqualBlock = (lowestSAD > maxSADTolerance * 0.043) ? null : mostEqualBlock;
			break;
		default:
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
		
		for (int y = 0; y < colors1.length; y++) {
			for (int x = 0; x < colors1[y].length; x++) {
				YCbCrColor prevCol = colors1[y][x];
				YCbCrColor curCol = colors2[y][x];

				resY += Math.abs(prevCol.getY() - curCol.getY());
				resCb += Math.abs(prevCol.getCb() - curCol.getCb());
				resCr += Math.abs(prevCol.getCr() - curCol.getCr());
				resA += Math.abs(prevCol.getA() - curCol.getA());
			}
		}
		
		return (Math.pow(resY, 2) + Math.pow(resCb, 2) + Math.pow(resCr, 2) + Math.pow(resA, resA)) / (double)(colors1.length * colors1.length);
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