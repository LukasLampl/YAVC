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

package Decoder;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Encoder.MakroBlockEngine;
import Main.config;
import Utils.ColorManager;
import Utils.DCTObject;
import Utils.PixelRaster;
import Utils.Vector;
import Utils.YCbCrColor;
import Utils.YCbCrMakroBlock;

public class DataPipeEngine {
	private DataGrabber GRABBER = null;
	private MakroBlockEngine MAKRO_BLOCK_ENGINE = new MakroBlockEngine();
	private ColorManager COLOR_MANAGER = new ColorManager();
	private Dimension DIMENSION = null;
	
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
		String[] stepInfo = startFrameContent.split("\\.");
		int x = 0, y = 0;
		
		for (String s : stepInfo) {
			if (x >= this.DIMENSION.width) {
				x = 0;
				y++;
			}
			
			if (y >= this.DIMENSION.height) {
				continue;
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
		return frameNumber + 3 >= this.MAX_FRAMES ? false : true;
	}
	
	/*
	 * Purpose: Build the next frame based on the FRAME_START_POSITION
	 * Return Type: BufferedImage => Built image
	 * Params: int frameNumber => Number of frame to scrape
	 */
	public BufferedImage scrape_next_frame(int frameNumber) {
		BufferedImage render = new BufferedImage(this.DIMENSION.width, this.DIMENSION.height, BufferedImage.TYPE_INT_ARGB);
		this.CURRENT_FRAME_DATA = this.GRABBER.get_frame(frameNumber);

		if (this.CURRENT_FRAME_DATA == null) {
			return null;
		}
		
		ArrayList<YCbCrMakroBlock> blocks = new ArrayList<YCbCrMakroBlock>();
		
		String[] splitVecs = this.CURRENT_FRAME_DATA.split(Character.toString(config.V_DEF_S));
		splitVecs[0] = splitVecs[0].replaceFirst(Character.toString(config.DCT_DEF_S), "");
		
		if (splitVecs.length <= 0) {
			return render;
		}
		
		String[] matrices = splitVecs[0].split(Character.toString(config.DCT_POS_END_DEF));
		
		if (matrices.length <= 1) {
			return render;
		}
		
		for (String matrix : matrices) {
			int YEnd = matrix.indexOf(config.DCT_Y_END_DEF);
			int CbEnd = matrix.indexOf(config.DCT_CB_END_DEF);
			int CrEnd = matrix.indexOf(config.DCT_CR_END_DEF);
			
			String YComp = matrix.substring(0, YEnd);
			String CbComp = matrix.substring(YEnd + 1, CbEnd);
			String CrComp = matrix.substring(CbEnd + 1, CrEnd);
			String Pos = matrix.substring(CrEnd + 1, matrix.length());

			String[] YCoef = YComp.split(Character.toString(config.DCT_MATRIX_NL_DEF));
			String[] CbCoef = CbComp.split(Character.toString(config.DCT_MATRIX_NL_DEF));
			String[] CrCoef = CrComp.split(Character.toString(config.DCT_MATRIX_NL_DEF));
			
			double[][] YCols = new double[YCoef.length][YCoef.length];
			double[][] CbCols = new double[CbCoef.length][CbCoef.length];
			double[][] CrCols = new double[CrCoef.length][CrCoef.length];
			
			//Luma handling
			for (int y = 0; y < YCoef.length; y++) {
				for (int x = 0; x < YCoef[y].length(); x++) {
					char t = YCoef[y].charAt(x);
					int res = shift_DCT_back(t);

					YCols[y][x] = res;
				}
			}
			
			//Chroma handling
			for (int y = 0; y < CbCoef.length && y < CrCoef.length; y++) {
				for (int x = 0; x < CbCoef.length && x < CrCoef.length; x++) {
					char cb = CbCoef[y].charAt(x);
					char cr = CrCoef[y].charAt(x);
					
					int valCb = shift_DCT_back(cb);
					int valCr = shift_DCT_back(cr);
					
					CbCols[y][x] = valCb;
					CrCols[y][x] = valCr;
				}
			}
			
			Point pos = new Point(0, 0);
			
			if (Pos.length() > 2 || Pos.length() < 2) {
				System.err.println("Position longer than 2? Position available?");
			} else {
				int x = (int)(Pos.charAt(0)) - config.RESERVED_TABLE_SIZE;
				int y = (int)(Pos.charAt(1)) - config.RESERVED_TABLE_SIZE;
				pos.setLocation(x, y);
			}
			
			blocks.add(this.MAKRO_BLOCK_ENGINE.apply_IDCT(new DCTObject(YCols, CbCols, CrCols, pos)));
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
		System.out.println("Blocks: " + blocks.size());
		return render;
	}
	
	private int shift_DCT_back(char val) {
		if (((val >> 14) & 0x1) == 1) {
			return -1 * (((int)val - config.RESERVED_TABLE_SIZE) & 0xFFF);
		}
		
		return (((int)val - config.RESERVED_TABLE_SIZE) & 0xFFF);
	}
	
	/*
	 * Purpose: Get the vectors of the current frame
	 * Return Type: ArrayList<Vector> => Found vectors
	 * Params: int frameNumber => frame number for the vectors
	 */
	public ArrayList<Vector> scrape_vectors(int frameNumber) {
		ArrayList<Vector> vecs = new ArrayList<Vector>();
		
		if (this.CURRENT_FRAME_DATA == null) {
			System.err.println("No more data! > abort");
			return null;
		}
		
		int start = this.CURRENT_FRAME_DATA.indexOf(config.V_DEF_S);
		
		if (start == -1) {
			System.err.println("No Vector indicies found! (" + frameNumber + ")");
			return null;
		}
		
		int vecCount = ((int)this.CURRENT_FRAME_DATA.charAt(start + 1) << 16) | (int)this.CURRENT_FRAME_DATA.charAt(start + 2);
		String data = this.CURRENT_FRAME_DATA.substring(start + 3, this.CURRENT_FRAME_DATA.length());

		if (data == null) {
			System.err.println("MISSING VECTOR FOUND!!! (F_" + frameNumber + ")");
			return null;
		}
		
		int vecNum = 0;
		
		//+4 since vectors are composed of 4 chars
		for (int i = 0; i < data.length(); i += 5, vecNum++) {
			char sPointX = data.charAt(i);
			char sPointY = data.charAt(i + 1);
			char vSpanX = data.charAt(i + 2);
			char vSpanY = data.charAt(i + 3);
			char info = data.charAt(i + 4);
			
			int x = ((int)sPointX) - config.RESERVED_TABLE_SIZE;
			int y = ((int)sPointY) - config.RESERVED_TABLE_SIZE;
			int spanX = shift_vec_span_back(vSpanX);
			int spanY = shift_vec_span_back(vSpanY);
			int ref = (((((int)info) - config.RESERVED_TABLE_SIZE) >> 8) & 0xFF);
			int size = (((int)info) - config.RESERVED_TABLE_SIZE) & 0xFF;
			
			vecs.add(new Vector(new Point(x, y), spanX, spanY, ref, size));
		}
		
		if (vecNum < vecCount) {
			System.out.println(vecNum);
			System.out.println("Less vectors than there should be..");
		}
		
		return vecs;
	}
	
	private int shift_vec_span_back(char span) {
		if (((span >> 14) & 0x1) == 1) {
			return (-1 * (((int)span & 0xFFF) - config.RESERVED_TABLE_SIZE));
		}
		
		return ((int)span) - config.RESERVED_TABLE_SIZE;
	}
	
	/*
	 * Purpose: Composite the vectors, differences and previous frame
	 * Return Type: BufferedImage => Composited image
	 * Params: ArrayList<Vector> vecs => Vectors in the diffs;
	 * 			BufferedImage prevImg => Previous frame;
	 * 			BufferedImage currImg => Current frame (differences)
	 */
	public BufferedImage build_frame(ArrayList<Vector> vecs, ArrayList<BufferedImage> referenceImages, BufferedImage prevImg, BufferedImage currImg) {
		BufferedImage render = new BufferedImage(prevImg.getWidth(), prevImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage vectors = new BufferedImage(prevImg.getWidth(), prevImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		if (vecs != null) {
			ArrayList<PixelRaster> rasterSet = new ArrayList<PixelRaster>();
			
			for (int i = 0; i < referenceImages.size(); i++) {
				rasterSet.add(new PixelRaster(referenceImages.get(i)));
			}
			
			for (Vector vec : vecs) {
				PixelRaster ref = rasterSet.get(referenceImages.size() - vec.getReferenceDrawback());
				YCbCrMakroBlock block = this.MAKRO_BLOCK_ENGINE.get_single_makro_block(vec.getStartingPoint(), ref, vec.getReferenceSize());
				YCbCrColor[][] cols = block.getColors();
				
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
						
						if (cols[y][x].getA() == 255) { //ASCII for YAVC
							continue;
						}
						
						vectors.setRGB(vecEndX + x, vecEndY + y, this.COLOR_MANAGER.convert_YCbCr_to_RGB(cols[y][x]).getRGB());
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
	
	/*
	 * Purpose: Read in the Metadata
	 * Return Type: void
	 * Params: String metaFileContent => Content of the Metafile
	 */
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
	
	public int get_max_frame_number() {
		return this.MAX_FRAMES;
	}
}
