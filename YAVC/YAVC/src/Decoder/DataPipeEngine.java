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
	private String data = null;
	private Dimension DIMENSION = null;
	private boolean hasNext = true;
	
	private int MAX_FRAMES = 0;
	private int FRAME_NUMBER_START = 0;
	private int FRAME_NUMBER_END = 0;
	
	public DataPipeEngine(String content) {
		this.data = content;
		this.DIMENSION = scrape_meta_data();
	}
	
	/*
	 * Purpose: Get the first frame of the video and build it (Has no vectors)
	 * Return Type: BufferedImage => Built image
	 * Params: void
	 */
	public BufferedImage scrape_main_image() {
		BufferedImage render = new BufferedImage(this.DIMENSION.width, this.DIMENSION.height, BufferedImage.TYPE_INT_ARGB);
		
		int start = this.data.indexOf("$S$") + 3;
		int end = start;
		
		for (int i = start; i < this.data.length(); i++) {
			if (this.data.charAt(i) != '?') {
				end++;
			} else {
				break;
			}
		}

		String stream = this.data.substring(start, end);
		String[] stepInfos = stream.split("\\.");
		
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
	 * Purpose: Let the pointer jump to the desired frameNumber
	 * Return Type: void
	 * Params: int frameNumber => Frame to jump to
	 */
	public void jump_to_frame_number(int frameNumber) {
		String regex = "$P" + frameNumber + "$";
		int startPos = this.data.indexOf(regex);
		
		if (startPos == -1) {
			this.hasNext = false;
			return;
		}
		
		this.FRAME_NUMBER_START = startPos + regex.length();
		
		for (int i = this.FRAME_NUMBER_START; i < this.data.length(); i++) {
			if (this.data.charAt(i) == '?') {
				this.FRAME_NUMBER_END = i;
				break;
			}
		}
	}
	
	/*
	 * Purpose: Build the next frame based on the FRAME_START_POSITION
	 * Return Type: BufferedImage => Built image
	 * Params: void
	 */
	public BufferedImage scrape_next_frame() {
		BufferedImage render = new BufferedImage(this.DIMENSION.width, this.DIMENSION.height, BufferedImage.TYPE_INT_ARGB);
		String stream = this.data.substring(this.FRAME_NUMBER_START, this.FRAME_NUMBER_END);
		String[] set = stream.split("\\.");
		
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
		String regex = "$V" + frameNumber + "$";
		int start = this.data.indexOf(regex);
		
		if (start == -1) {
			System.err.println("MISSING VECTOR FOUND!!! (" + regex + ")");
			return null;
		}
		
		start += regex.length();
		int end = start + 1;

		for (int i = end; i < this.data.length(); i++) {
			if (this.data.charAt(i) == '$') {
				end = i;
				break;
			} else if (i + 1 == this.data.length()) {
				end = i;
			}
		}
		
		String stream = this.data.substring(start, end);
		String[] streamSet = stream.split("\\[");
		
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
		Graphics2D g2d = render.createGraphics();
		g2d.drawImage(prevImg, 0, 0, prevImg.getWidth(), prevImg.getHeight(), null, null);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
		g2d.drawImage(currImg, 0, 0, currImg.getWidth(), currImg.getHeight(), null, null);
		g2d.dispose();
		
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
					
					render.setRGB(p.x + vec.getSpanX() + x, p.y + vec.getSpanY() + y, block.getColors()[y][x]);
				}
			}
		}
		
		return render;
	}
	
	private Dimension scrape_meta_data() {
		int start = this.data.indexOf("META[D[");
		StringBuilder rawDimension = new StringBuilder(16);
		
		for (int i = start + 7; i < this.data.length(); i++) {
			if (this.data.charAt(i) == ']') {
				break;
			}
			
			rawDimension.append(this.data.charAt(i));
		}
		
		start = this.data.indexOf("]FC[");
		StringBuilder rawFC = new StringBuilder(16);
		
		for (int i = start + 4; i < this.data.length(); i++) {
			if (this.data.charAt(i) == ']') {
				break;
			}
			
			rawFC.append(this.data.charAt(i));
		}
		
		this.MAX_FRAMES = Integer.parseInt(rawFC.toString());
		
		String[] dims = rawDimension.toString().split("\\,");
		return new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
	}
}
