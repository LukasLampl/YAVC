package Decoder;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Encoder.MakroBlock;
import Encoder.MakroBlockEngine;
import Encoder.Vector;

public class DataPipeEngine {
	private DataGrabber grabber = null;
	private Dimension DIMENSION = null;
	
	private int MAX_FRAMES = 0;
	private String CURRENT_FRAME_DATA = "";
	
	public DataPipeEngine(DataGrabber grabber) {
		this.grabber = grabber;
		scrape_meta_data(grabber.get_metadata());
		
		System.out.println("META: " + DIMENSION + ", " + MAX_FRAMES);
	}
	
	/*
	 * Purpose: Get the first frame of the video and build it (Has no vectors)
	 * Return Type: BufferedImage => Built image
	 * Params: void
	 */
	public BufferedImage scrape_main_image(String startFrameContent) {
		BufferedImage render = new BufferedImage(this.DIMENSION.width, this.DIMENSION.height, BufferedImage.TYPE_INT_ARGB);
		String[] stepInfos = startFrameContent.split("\\.");

		int x = 0;
		int y = 0;
		
		for (String s : stepInfos) {
			String[] rle_dec = s.split("\\~");
			
			int value = 0;
			int count = 0;
			
			if (rle_dec.length > 1) {
				value = Integer.parseInt(rle_dec[0]);
				count = Integer.parseInt(rle_dec[1]);
			} else {
				value = Integer.parseInt(s);
			}
				
			for (int i = 0; i < count; i++) {
				if (x >= render.getWidth()) {
					y++;
					x = 0;
				}
				
				if (y + 1 >= render.getHeight()) {
					break;
				}
				
//				System.out.println("Setting px: " + value + "; at: " + x + ", " + y + "; i:" + i + "/" + count);
				render.setRGB(x++, y, value);
			}
		}
		
		return render;
	}
	
	/*
	 * Purpose: Checks if the DataPipeEngine has already reached the end of the file
	 * Return Type: boolean => true = has next frame; false = EOF reached
	 * Params: int frameNumber => Currently read frame
	 */
	public boolean hasNext(int frameNumber) {
		return frameNumber + 1 >= this.MAX_FRAMES ? false : true;
	}
	
	/*
	 * Purpose: Build the next frame based on the FRAME_START_POSITION
	 * Return Type: BufferedImage => Built image
	 * Params: void
	 */
	public BufferedImage scrape_next_frame(int frameNumber) {
		BufferedImage render = new BufferedImage(this.DIMENSION.width, this.DIMENSION.height, BufferedImage.TYPE_INT_ARGB);
		this.CURRENT_FRAME_DATA = this.grabber.get_frame(frameNumber);
		
		if (this.CURRENT_FRAME_DATA == null) {
			return null;
		}
		
		int end = this.CURRENT_FRAME_DATA.indexOf("$V$");
		
		if (end == -1) {
			end = this.CURRENT_FRAME_DATA.length() - 1;
		}
		
		String[] set = this.CURRENT_FRAME_DATA.substring(0, end).split("\\.");
		
		int x = 0;
		int y = 0;
		
		for (String s : set) {
			if (x >= render.getWidth()) {
				y++;
				x = 0;
			} else if (y >= render.getHeight()) {
				break;
			}
			
			String[] rle_dec = s.split("\\~");
			
			if (rle_dec.length > 1) {
				int count = Integer.parseInt(rle_dec[1]) + 1;
				
				for (int i = 0; i < count; i++) {
					if (x >= render.getWidth()) {
						y++;	
						x = 0;
					} else if (y + 1 >= render.getHeight()) {
						break;
					}
					
					render.setRGB(x++, y, Integer.parseInt(rle_dec[0]));
				}
			} else {
				render.setRGB(x++, y, Integer.parseInt(rle_dec[0]));
			}
		}
		
		return render;
	}
	
	/*
	 * Purpose: Get the vectors of the current frame
	 * Return Type: ArrayList<Vector> => Found vectors
	 * Params: int frameNumber => frame number for the vectors
	 */
	public ArrayList<Vector> scrape_vectors(int frameNumber) {
		ArrayList<Vector> vecs = new ArrayList<Vector>();
		
		if (this.CURRENT_FRAME_DATA == null) {
			System.err.println("No more data! (abort)");
			return null;
		}
		
		int start = this.CURRENT_FRAME_DATA.indexOf("$V$");
		
		if (start == -1) {
			System.err.println("No Vector indicies found! (" + frameNumber + ")");
			return null;
		}
		
		String data = this.CURRENT_FRAME_DATA.substring(start, this.CURRENT_FRAME_DATA.length());
		System.out.println(frameNumber);
		if (data == null) {
			System.err.println("MISSING VECTOR FOUND!!! (F_" + frameNumber + ")");
			return null;
		}
		
		String[] streamSet = data.split("\\*");
		
		for (int i = 1; i < streamSet.length; i++) {
			String s = streamSet[i];
			String[] drawBackPart = s.split("\\_");
			String[] parts = drawBackPart[0].split("\\;");
			
			String[] startPos = parts[0].split("\\,");
			String[] spanSize = parts[1].split("\\,");

			Vector vec = new Vector();
			vec.setStartingPoint(new Point(Integer.parseInt(startPos[0]), Integer.parseInt(startPos[1])));
			vec.setSpanX(Integer.parseInt(spanSize[0]));
			vec.setSpanY(Integer.parseInt(spanSize[1]));
			vec.setReferenceDrawback(Integer.parseInt(drawBackPart[1]));
			vecs.add(vec);
		}
		
		return vecs;
	}
	
	/*
	 * Purpose: Composite the vectors, differences and previous frame
	 * Return Type: BufferedImage => Composited image
	 * Params: ArrayList<Vector> vecs => Vectors in the diffs;
	 * 			BufferedImage prevImg => Previous frame;
	 * 			BufferedImage currImg => Current frame (differences)
	 */
	public BufferedImage build_frame(ArrayList<Vector> vecs, ArrayList<BufferedImage> referenceImages, BufferedImage prevImg, BufferedImage currImg) {
		MakroBlockEngine makroBlockEngine = new MakroBlockEngine();
		BufferedImage render = new BufferedImage(prevImg.getWidth(), prevImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage vectors = new BufferedImage(prevImg.getWidth(), prevImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		if (vecs != null) {
			for (Vector vec : vecs) {
				MakroBlock block = makroBlockEngine.get_single_makro_block(vec.getStartingPoint(), referenceImages.get(referenceImages.size() - vec.getReferenceDrawback()));
				
				for (int y = 0; y < block.getColors().length; y++) {
					for (int x = 0; x < block.getColors()[y].length; x++) {
						int vecEndX = vec.getStartingPoint().x + vec.getSpanX();
						int vecEndY = vec.getStartingPoint().y + vec.getSpanY();
						
						if (vecEndX + x >= render.getWidth()
							|| vecEndY + y >= render.getHeight()
							|| vecEndX + x < 0 || vecEndY + y < 0) {
//							System.err.println("Out of boundaries!");
							continue;
						}
						
						if (block.getColors()[y][x] == 89658667) { //ASCII for YAVC
							continue;
						}
						
						vectors.setRGB(vecEndX + x, vecEndY + y, block.getColors()[y][x]);
					}
				}
			}
		}
		
		Graphics2D g2d = render.createGraphics();
		g2d.drawImage(prevImg, 0, 0, prevImg.getWidth(), prevImg.getHeight(), null, null);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g2d.drawImage(currImg, 0, 0, currImg.getWidth(), currImg.getHeight(), null, null);
		g2d.drawImage(vectors, 0, 0, vectors.getWidth(), vectors.getHeight(), null, null);
		g2d.dispose();
		
		return render;
	}
	
	private void scrape_meta_data(String metaFileContent) {
		int start = metaFileContent.indexOf("META[D[");
		StringBuilder rawDimension = new StringBuilder(16);
		
		for (int i = start + 7; i < metaFileContent.length(); i++) {
			if (metaFileContent.charAt(i) == ']') {
				break;
			}
			
			rawDimension.append(metaFileContent.charAt(i));
		}
		
		String[] dims = rawDimension.toString().split("\\,");
		this.DIMENSION = new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
		
		start = metaFileContent.indexOf("]FC[");
		
		StringBuilder rawFC = new StringBuilder(16);
		
		for (int i = start + 4; i < metaFileContent.length(); i++) {
			if (metaFileContent.charAt(i) == ']') {
				break;
			}
			
			rawFC.append(metaFileContent.charAt(i));
		}
		
		this.MAX_FRAMES = Integer.parseInt(rawFC.toString());
	}
}
