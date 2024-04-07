package Decoder;

import java.awt.AlphaComposite;
import java.awt.Color;
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
	private boolean hasNext = true;
	
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
			if (x >= render.getWidth()) {
				y++;	
				x = 0;
			} else if (y + 1 >= render.getHeight()) {
				break;
			}

			render.setRGB(x++, y, Integer.parseInt(s));
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

			render.setRGB(x++, y, Integer.parseInt(s));
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
		int start = this.CURRENT_FRAME_DATA.indexOf("$V$");
		String data = this.CURRENT_FRAME_DATA.substring(start, this.CURRENT_FRAME_DATA.length());
		
		if (data == null) {
			System.err.println("MISSING VECTOR FOUND!!! (F_" + frameNumber + ")");
			return null;
		}
		
		String[] streamSet = data.split("\\[");
		
		for (int i = 1; i < streamSet.length; i++) {
			String s = streamSet[i];
			String[] parts = s.substring(0, s.length() - 1).split("\\;");
			
			String[] startPos = parts[0].split("\\,");
			String[] spanSize = parts[1].split("\\,");
			
			Vector vec = new Vector();
			vec.setStartingPoint(new Point(Integer.parseInt(startPos[0]), Integer.parseInt(startPos[1])));
			vec.setSpanX(Integer.parseInt(spanSize[0]));
			vec.setSpanY(Integer.parseInt(spanSize[1]));
			
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
	public BufferedImage build_frame(ArrayList<Vector> vecs, BufferedImage prevImg, BufferedImage currImg) {
		MakroBlockEngine makroBlockEngine = new MakroBlockEngine();
		BufferedImage render = new BufferedImage(prevImg.getWidth(), prevImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage vectors = new BufferedImage(prevImg.getWidth(), prevImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		if (vecs == null) {
			return render;	
		}
		
		for (Vector vec : vecs) {
			Point p = new Point(vec.getStartingPoint().x, vec.getStartingPoint().y);
			MakroBlock block = makroBlockEngine.get_single_makro_block(p, prevImg);
			
			for (int y = 0; y < block.getColors().length; y++) {
				for (int x = 0; x < block.getColors()[y].length; x++) {
					if (p.x + vec.getSpanX() + x >= render.getWidth()
						|| p.y + vec.getSpanY() + y >= render.getHeight()
						|| p.x < 0 || p.y < 0) {
						System.err.println("Too small!");
						continue;
					}
					
					if (block.getColors()[y][x] == 89658667) { //ASCII for YAVC
						continue;
					}
					
					vectors.setRGB(p.x + vec.getSpanX() + x, p.y + vec.getSpanY() + y, Color.magenta.getRGB());//block.getColors()[y][x]);
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
