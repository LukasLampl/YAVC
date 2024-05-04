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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import Main.config;
import UI.Frame;
import Utils.ColorManager;
import Utils.DCTObject;
import Utils.PixelRaster;
import Utils.Vector;
import Utils.YCbCrMakroBlock;

public class OutputWriter {
	private File COMPRESS_DIR = null;
	private Frame FRAME = null;
	private ColorManager COLOR_MANAGER = new ColorManager();
	private MakroBlockEngine MAKRO_BLOCK_ENGINE = new MakroBlockEngine();
	private ArrayList<SequenceObject> QUEUE = new ArrayList<SequenceObject>(5);
	
	public OutputWriter(String path, Frame f) {
		this.FRAME = f;
		
		try {
			this.COMPRESS_DIR = new File(path + "/YAVC-COMP");
			this.COMPRESS_DIR.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		start_baking_queue();
	}
	
	/*
	 * Purpose: Adds an object to the writing queue
	 * Return Type: void
	 * Params: ArrayList<YCbCrMakroBlock> diffs => Differences between prev and cur frame;
	 * 			ArrayList<Vector> vecs => Movement vectors of the the frame
	 */
	public void add_obj_to_queue(ArrayList<DCTObject> dct, ArrayList<Vector> vecs) {
		SequenceObject obj = new SequenceObject();
		obj.setDifferences(dct);
		obj.setVecs(vecs);
		
		this.QUEUE.add(obj);
	}
	
	/*
	 * Purpose: Prepare and write the metadata of the YAVC file
	 * Return Type: void
	 * Params: BufferedImage originalImage => First frame in the video
	 * 			int frameNum => Number of frames in the video
	 */
	public void bake_meta_data(PixelRaster originalImage, int frameNum) {
		if (originalImage == null) {
			return;
		}
		
		try {
			String meta = "META["
					+ "D[" + originalImage.getWidth() + "," + originalImage.getHeight() + "]"
					+ "FC[" + frameNum + "]"
					+ "]";
			
			File metaFile = new File(this.COMPRESS_DIR.getAbsolutePath() + "/META.DESC");
			metaFile.createNewFile();
			
			Files.write(Path.of(metaFile.getAbsolutePath()), meta.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Purpose: Writes the first frame of the video into the YAVC file
	 * Return Type: void
	 * Params: BufferedImage img => First frame
	 */
	public void bake_start_frame(PixelRaster img) {
		StringBuilder imgInChars = new StringBuilder();

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				imgInChars.append(img.getRGB(x, y) + ".");
			}
		}
		
		try {
			File startFrameFile = new File(this.COMPRESS_DIR.getAbsolutePath() + "/SF.YAVCF");
			startFrameFile.createNewFile();
			
			Files.write(Path.of(startFrameFile.getAbsolutePath()), imgInChars.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Purpose: Prepare and write a frame into the YAVC file without the "FIRST FRAME" importance
	 * Return Type: void
	 * Params: ArrayList<YCbCrMakroBlock> differences => The differences between the previous and current frame
	 */
	private int outputFrames = 0;
	
	private File bake_frame(ArrayList<DCTObject> DCTList) {
		if (DCTList == null) {
			System.err.println("No DCT!");
			return null;
		}
		
		StringBuilder DCT = new StringBuilder();
		DCT.append(config.DCT_DEF_S);

		for (DCTObject dct : DCTList) {
			StringBuilder CbCoStr = new StringBuilder();
			StringBuilder CrCoStr = new StringBuilder();
			StringBuilder YCoStr = new StringBuilder();
			
			for (int y = 0; y < dct.getCbDCT().length; y++) {
				for (int x = 0; x < dct.getCbDCT()[y].length; x++) {
					double Cb = dct.getCbDCT()[y][x];
					double Cr = dct.getCrDCT()[y][x];
					
					char CbC = shift_DCT_bits(Cb);
					char CrC = shift_DCT_bits(Cr);
					
					CbCoStr.append(CbC);
					CrCoStr.append(CrC);
				}
				
				if (y + 1 < dct.getCbDCT().length) {
					CbCoStr.append(config.DCT_MATRIX_NL_DEF);
					CrCoStr.append(config.DCT_MATRIX_NL_DEF);
				}
			}
			
			CbCoStr.append(config.DCT_CB_END_DEF);
			CrCoStr.append(config.DCT_CR_END_DEF);
			
			for (int y = 0; y < dct.getY().length; y++) {
				for (int x = 0; x < dct.getY()[y].length; x++) {
					YCoStr.append((char)shift_DCT_bits(dct.getY()[y][x]));
				}
				
				if (y + 1 < dct.getY().length) {
					YCoStr.append(config.DCT_MATRIX_NL_DEF);
				}
			}
			
			YCoStr.append(config.DCT_Y_END_DEF);
			DCT.append(YCoStr);
			DCT.append(CbCoStr);
			DCT.append(CrCoStr);
			DCT.append((char)(dct.getPosition().x + config.RESERVED_TABLE_SIZE));
			DCT.append((char)(dct.getPosition().y + + config.RESERVED_TABLE_SIZE));
			DCT.append(config.DCT_POS_END_DEF);
		}
		
		File frameFile = new File(this.COMPRESS_DIR.getAbsolutePath() + "/F_" + outputFrames++ + ".YAVCF");

		try {
			frameFile.createNewFile();
			Files.write(Path.of(frameFile.getAbsolutePath()), DCT.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return frameFile;
	}
	
	private char shift_DCT_bits(double val) {
		/*
		 * UTF-8 provides 16 Bits per char.
		 * 00000000 00000000
		 * 
		 * 1) If the number is negative, set the 14th Bit to 1
		 * 00100000 00000000
		 * 
		 * 2) Else let it like it is and fill up the 14 prior Bits with data
		 */
		
		if (val < 0) {
			return (char)(1 << 14 | ((int)Math.abs(val) + config.RESERVED_TABLE_SIZE) & 0xFFF);
		}
		
		return (char)((int)Math.abs(val) + config.RESERVED_TABLE_SIZE);
	}
	
	/*
	 * Purpose: Write calcuated vectors into the YAVC file
	 * Return Type: void
	 * Params: ArrayList<Vector> movementVectors => Calculated vectors of the current frame
	 * 			File frameFile => File to which the vectors correspond to
	 */
	private void bake_vectors(ArrayList<Vector> movementVectors, File frameFile) {
		if (movementVectors == null) {
			return;
		}
		
		try {
			StringBuilder vecRes = new StringBuilder(movementVectors.size() * 2);
			vecRes.append(config.V_DEF_S);
			
			int size = movementVectors.size();
			vecRes.append((char)(size >> 16));
			vecRes.append((char)(size & 0xFFFF));
			
			for (Vector vec : movementVectors) {
				int refAndSizeShift = ((vec.getReferenceDrawback() & 0xFF) << 8 | (vec.getReferenceSize() & 0xFF));
				
				char sPointX = (char)(vec.getStartingPoint().x + config.RESERVED_TABLE_SIZE);
				char sPointY = (char)(vec.getStartingPoint().y + config.RESERVED_TABLE_SIZE);
				char spanX = shift_vec_span(vec.getSpanX());
				char spanY = shift_vec_span(vec.getSpanY());
				char refAndSize = (char)((refAndSizeShift & 0xFFFF) + config.RESERVED_TABLE_SIZE);

				vecRes.append(sPointX);
				vecRes.append(sPointY);
				vecRes.append(spanX);
				vecRes.append(spanY);
				vecRes.append(refAndSize);
			}
			
			Files.write(Path.of(frameFile.getAbsolutePath()), vecRes.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private char shift_vec_span(int span) {
		if (span < 0) {
			return (char)((1 << 14) | ((Math.abs(span) + config.RESERVED_TABLE_SIZE) & 0xFFF));
		}
		
		return (char)(span + config.RESERVED_TABLE_SIZE);
	}
	
	/*
	 * Purpose: Starts a Thread for writing all files (pauses if queue is clear)
	 * Return Type: void
	 * Params: void
	 */
	private void start_baking_queue() {
		Thread writer = new Thread(() -> {
			while (FRAME.canWriterWrite()) {
				if (QUEUE.size() == 0) {
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					continue;
				}
				
				SequenceObject obj = QUEUE.get(0);
				File out = bake_frame(obj.getDCT());

				if (obj.getVecs() != null) {
					bake_vectors(obj.getVecs(), out);
				}
				
				QUEUE.remove(0);
			}
		});
		
		writer.setName("FileWriter");
		writer.start();
	}
	
	/*
	 * Purpose: Compress the compressed result even more (using ZIP).
	 * 			File to be compressed is the file that was set during runtime.
	 * Return Type: void
	 * Params: void
	 */
	public void compress_result() {
		try {
			FileOutputStream fos = new FileOutputStream(this.COMPRESS_DIR.getAbsolutePath() + ".yavc");
	        ZipOutputStream zipOut = new ZipOutputStream(fos);
	        
	        for (File f : this.COMPRESS_DIR.listFiles()) {
	        	zipOut.putNextEntry(new ZipEntry(f.getName()));
	        	
	        	FileInputStream fis = new FileInputStream(f);
	        	byte[] bytes = new byte[4096];
		        int length;
	        	
		        while ((length = fis.read(bytes)) >= 0) {
		        	zipOut.write(bytes, 0, length);
		        }
	        	
		        fis.close();
	        	zipOut.closeEntry();
	        }
	        
	        zipOut.closeEntry();
	        zipOut.close();
	        this.COMPRESS_DIR.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PixelRaster reconstruct_DCT_image(ArrayList<DCTObject> objs, PixelRaster img) {
		if (objs == null || objs.size() == 0) {
			System.err.println("No Objects to reverse (DCT).");
			return img;
		} else if (img == null) {
			System.err.println("No reference > Work without reference (Encoding error?)");
		}
		
		PixelRaster render = img;
		
		for (DCTObject obj : objs) {
			YCbCrMakroBlock block = this.MAKRO_BLOCK_ENGINE.apply_IDCT(obj);
			
			for (int y = 0; y < block.getSize(); y++) {
				for (int x = 0; x < block.getSize(); x++) {
					if (block.getPosition().x + x >= img.getWidth()
						|| block.getPosition().y + y >= img.getHeight()) {
						continue;
					}
					
					int col = this.COLOR_MANAGER.convert_YCbCr_to_RGB(block.getReversedSubSampleColor(x, y)).getRGB();
					
					if (block.getAVal(x, y) == 1.0) {
						continue;
					}
					
					render.setRGB(block.getPosition().x + x, block.getPosition().y + y, col);
				}
			}
		}
		
		return render;
	}
	
	/*
	 * Purpose: Builds the encoded frame and writes it to the destination
	 * 			DEBUGGING ONLY!
	 * Return Type: void
	 * Params: BufferedImage org => previous build frame;
	 * 			ArrayList<MakroBlock> diffs => Differencs to the prev img;
	 * 			ArrayList<Vector> vecs => Vectors from the differences;
	 * 			File outputFile => File to which to write (DIR)
	 */
	public int output = 0;
	
	public BufferedImage build_Frame(PixelRaster org, ArrayList<PixelRaster> refs, ArrayList<YCbCrMakroBlock> diffs, ArrayList<Vector> vecs, int diff) {
		BufferedImage img = new BufferedImage(org.getWidth(), org.getHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage img_v = new BufferedImage(org.getWidth(), org.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		BufferedImage vectors = new BufferedImage(org.getWidth(), org.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D v_g2d = vectors.createGraphics();
		v_g2d.setColor(Color.red);
		
		for (YCbCrMakroBlock block : diffs) {
			for (int y = 0; y < block.getSize(); y++) {
				for (int x = 0; x < block.getSize(); x++) {
					if (block.getPosition().x + x >= img.getWidth()
						|| block.getPosition().y + y >= img.getHeight()) {
						continue;
					}
					
					int col = this.COLOR_MANAGER.convert_YCbCr_to_RGB(block.getReversedSubSampleColor(x, y)).getRGB();
					
					if (block.getAVal(x, y) == 1.0) {
						continue;
					}
					
					img.setRGB(block.getPosition().x + x, block.getPosition().y + y, col);
				}
			}
		}
		
		if (vecs != null) {
			for (Vector vec : vecs) {
				PixelRaster s = refs.get(refs.size() - vec.getReferenceDrawback());
				YCbCrMakroBlock cols = this.MAKRO_BLOCK_ENGINE.get_single_makro_block(vec.getStartingPoint(), s, vec.getReferenceSize(), null);
				int size = vec.getReferenceSize();
				
				for (int y = 0; y < size; y++) {
					for (int x = 0; x < size; x++) {
						int vecEndX = vec.getStartingPoint().x + vec.getSpanX();
						int vecEndY = vec.getStartingPoint().y + vec.getSpanY();
						
						if (vecEndX + x >= img_v.getWidth()
							|| vecEndY + y >= img_v.getHeight()
							|| vecEndX + x < 0
							|| vecEndY + y < 0) {
							continue;
						}
						
						if (cols.getAVal(x, y) == 1.0) {
							continue;
						}
						
						int color = this.COLOR_MANAGER.convert_YCbCr_to_RGB(cols.getReversedSubSampleColor(x, y)).getRGB();
						
						img_v.setRGB(vecEndX + x, vecEndY + y, color);
					}
				}
			}
		}
		
		v_g2d.dispose();
		
		BufferedImage render = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = render.createGraphics();
		
		try {
			if (diff == 1) {
				g2d.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null, null);
			} else if (diff == 2) {
				g2d.drawImage(img_v, 0, 0, img_v.getWidth(), img_v.getHeight(), null, null);
			} else {
				BufferedImage rep = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
				
				for (int y = 0; y < img.getHeight(); y++) {
					for (int x = 0; x < img.getWidth(); x++) {
						rep.setRGB(x, y, new Color(org.getRGB(x, y)).getRGB());
					}
				}
				
				g2d.drawImage(rep, 0, 0, img.getWidth(), img.getHeight(), null, null);
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
				g2d.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null, null);
				g2d.drawImage(img_v, 0, 0, img_v.getWidth(), img_v.getHeight(), null, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		g2d.dispose();
		return render;
	}
	
	public BufferedImage draw_MB_outlines(Dimension dim, ArrayList<YCbCrMakroBlock> diffs) {
		BufferedImage render = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)render.createGraphics();
		g2d.setColor(Color.RED);
		
		for (YCbCrMakroBlock b : diffs) {
			g2d.drawLine(b.getPosition().x, b.getPosition().y, b.getPosition().x + b.getSize(), b.getPosition().y + b.getSize());
			g2d.drawRect(b.getPosition().x, b.getPosition().y, b.getSize(), b.getSize());
		}
		
		g2d.dispose();
		
		return render;
	}
}