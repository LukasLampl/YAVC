package Decoder;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Encoder.ColorManager;
import Encoder.DCTObject;
import Encoder.MakroBlock;
import Encoder.MakroBlockEngine;
import Encoder.Vector;
import Encoder.YCbCrColor;
import Encoder.YCbCrMakroBlock;

public class DataPipeEngine {
	private DataGrabber GRABBER = null;
	private MakroBlockEngine MAKRO_BLOCK_ENGINE = new MakroBlockEngine();
	private ColorManager COLOR_MANAGER = new ColorManager();
	private Dimension DIMENSION = null;
	private int MBS = 4;
	
	private int MAX_FRAMES = 0;
	private String CURRENT_FRAME_DATA = "";
	
	public DataPipeEngine(DataGrabber grabber) {
		this.GRABBER = grabber;
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
			if (x >= this.DIMENSION.width) {
				x = 0;
				y++;
			}
			
			if (y >= this.DIMENSION.height) {
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
		return frameNumber + 2 >= this.MAX_FRAMES ? false : true;
	}
	
	/*
	 * Purpose: Build the next frame based on the FRAME_START_POSITION
	 * Return Type: BufferedImage => Built image
	 * Params: void
	 */
	public BufferedImage scrape_next_frame(int frameNumber) {
		BufferedImage render = new BufferedImage(this.DIMENSION.width, this.DIMENSION.height, BufferedImage.TYPE_INT_ARGB);
		this.CURRENT_FRAME_DATA = this.GRABBER.get_frame(frameNumber);

		if (this.CURRENT_FRAME_DATA == null) {
			return null;
		}
		
		String[] splitVecs = this.CURRENT_FRAME_DATA.split("$V$");
		
		String POSREGEX = "$DCT_P$";
		String YREGEX = "$DCT_Y$";
		String CbREGEX = "$DCT_CB$";
		String CrREGEX = "$DCT_CR$";
		
		int PosStart = splitVecs[0].indexOf(POSREGEX);
		int YStart = splitVecs[0].indexOf(YREGEX);
		int CbStart = splitVecs[0].indexOf(CbREGEX);
		int CrStart = splitVecs[0].indexOf(CrREGEX);
		
		String PosComp = splitVecs[0].substring(PosStart + POSREGEX.length(), YStart);
		String YComp = splitVecs[0].substring(YStart + YREGEX.length(), CbStart);
		String CbComp = splitVecs[0].substring(CbStart + CbREGEX.length(), CrStart);
		String CrComp = splitVecs[0].substring(CrStart + CrREGEX.length(), splitVecs[0].length());
		
		String[] Pos = PosComp.split(":");
		String[] YBlocks = YComp.split(":");
		String[] CbBlocks = CbComp.split(":");
		String[] CrBlocks = CrComp.split(":");

		//No data in file
		if (Pos.length == 1 || YBlocks.length == 1 || CbBlocks.length == 1 || CrBlocks.length == 1) {
			return render;
		}
		
		YCbCrMakroBlock[] blocks = new YCbCrMakroBlock[YBlocks.length];
		
		for (int i = 0; i < YBlocks.length; i++) {
			String Ycols[] = YBlocks[i].split("&");
			String CbCols[] = CbBlocks[i].split("&");
			String CrCols[] = CrBlocks[i].split("&");
			
			double[][] YCo = new double[this.MBS][this.MBS];
			double[][] CbCo = new double[this.MBS / 2][this.MBS / 2];
			double[][] CrCo = new double[this.MBS / 2][this.MBS / 2];
			
			for (int y = 0; y < CbCols.length; y++) {
				String[] CbRows = CbCols[y].split("\\.");
				String[] CrRows = CrCols[y].split("\\.");
				
				for (int x = 0; x < CbRows.length; x++) {
					CbCo[y][x] = Integer.parseInt(CbRows[x]);
					CrCo[y][x] = Integer.parseInt(CrRows[x]);
				}
			}
			
			for (int y = 0; y < Ycols.length; y++) {
				String[] rows = Ycols[y].split("\\.");
				
				for (int x = 0; x < rows.length; x++) {
					YCo[y][x] = Integer.parseInt(rows[x]);
				}
			}
			
			String pos = Pos[i];
			String[] cords = pos.split("\\.");
			Point p = new Point(Integer.parseInt(cords[0]), Integer.parseInt(cords[1]));
			blocks[i] = this.MAKRO_BLOCK_ENGINE.apply_IDCT(new DCTObject(YCo, CbCo, CrCo, p));
		}
		
		for (YCbCrMakroBlock b : blocks) {
			Point p = b.getPosition();
			YCbCrColor[][] cols = b.getColors();
			
			for (int y = 0; y < cols.length; y++) {
				if (p.y + y >= this.DIMENSION.height) {
					continue;
				}
				
				for (int x = 0; x < cols[y].length; x++) {
					if (p.x + x >= this.DIMENSION.width) {
						continue;
					}
					
					render.setRGB(p.x + x, p.y + y, this.COLOR_MANAGER.convert_YCbCr_to_RGB(cols[y][x]).getRGB());
				}
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
		
		start = metaFileContent.indexOf("]MBS[");
		this.MAX_FRAMES = Integer.parseInt(rawFC.toString());
		
		StringBuilder rawMBS = new StringBuilder(16);
		
		for (int i = start + 5; i < metaFileContent.length(); i++) {
			if (metaFileContent.charAt(i) == ']') {
				break;
			}
			
			rawFC.append(metaFileContent.charAt(i));
		}
		
		this.MBS = Integer.parseInt(rawMBS.toString());
	}
}
