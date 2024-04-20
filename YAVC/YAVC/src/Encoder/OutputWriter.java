package Encoder;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import Main.config;
import UI.Frame;

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
	public void bake_meta_data(BufferedImage originalImage, int frameNum) {
		if (originalImage == null) {
			return;
		}
		
		try {
			String meta = "META["
					+ "D[" + originalImage.getWidth() + "," + originalImage.getHeight() + "]"
					+ "FC[" + frameNum + "]"
					+ "MBS[" + config.MAKRO_BLOCK_SIZE + "]"
					+ "]";
			
			File metaFile = new File(this.COMPRESS_DIR.getAbsolutePath() + "/META.DESC");
			metaFile.createNewFile();
			
			Files.write(Path.of(metaFile.getAbsolutePath()), meta.getBytes(), StandardOpenOption.WRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Purpose: Writes the first frame of the video into the YAVC file
	 * Return Type: void
	 * Params: BufferedImage img => First frame
	 */
	public void bake_start_frame(BufferedImage img) {
		StringBuilder imgInChars = new StringBuilder();

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				imgInChars.append(img.getRGB(x, y) + ".");
			}
		}
		
		try {
			File startFrameFile = new File(this.COMPRESS_DIR.getAbsolutePath() + "/SF.YAVCF");
			startFrameFile.createNewFile();
			
			Files.write(Path.of(startFrameFile.getAbsolutePath()), imgInChars.toString().getBytes(), StandardOpenOption.WRITE);
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
		
		StringBuilder DCTPos = new StringBuilder();
		StringBuilder CbCoStr = new StringBuilder();
		StringBuilder CrCoStr = new StringBuilder();
		StringBuilder YCoStr = new StringBuilder();

		for (DCTObject dct : DCTList) {
			for (int y = 0; y < dct.getCbDCT().length; y++) {
				for (int x = 0; x < dct.getCbDCT()[y].length; x++) {
					CbCoStr.append((int)dct.getCbDCT()[y][x]);
					CrCoStr.append((int)dct.getCrDCT()[y][x]);
					
					if (x + 1 < dct.getCbDCT()[y].length) {
						CbCoStr.append(".");
						CrCoStr.append(".");
					}
				}
				
				if (y + 1 < dct.getCbDCT().length) {
					CbCoStr.append("&");
					CrCoStr.append("&");
				}
			}
			
			for (int y = 0; y < dct.getY().length; y++) {
				for (int x = 0; x < dct.getY()[y].length; x++) {
					YCoStr.append((int)dct.getY()[y][x]);
					
					if (x + 1 < dct.getY()[y].length) {
						YCoStr.append(".");
					}
				}
				
				if (y + 1 < dct.getY().length) {
					YCoStr.append("&");
				}
			}
			
			DCTPos.append(dct.getPosition().x + "." + dct.getPosition().y + ":");
			CbCoStr.append(":");
			CrCoStr.append(":");
			YCoStr.append(":");
		}
		
		File frameFile = new File(this.COMPRESS_DIR.getAbsolutePath() + "/F_" + outputFrames++ + ".YAVCF");
		StringBuilder imgInChars = new StringBuilder();
		
		imgInChars.append("$DCT_P$");
		imgInChars.append(DCTPos);
		imgInChars.append("$DCT_Y$");
		imgInChars.append(YCoStr);
		imgInChars.append("$DCT_CB$");
		imgInChars.append(CbCoStr);
		imgInChars.append("$DCT_CR$");
		imgInChars.append(CrCoStr);
		
		try {
			frameFile.createNewFile();
			Files.write(Path.of(frameFile.getAbsolutePath()), imgInChars.toString().getBytes(), StandardOpenOption.WRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return frameFile;
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
			vecRes.append("$V$");
			
			for (Vector vec : movementVectors) {
				vecRes.append("*" + vec.getStartingPoint().x + "," + vec.getStartingPoint().y + ";" + vec.getSpanX() + "," + vec.getSpanY() + "_" + vec.getReferenceDrawback());
			}
			
			Files.write(Path.of(frameFile.getAbsolutePath()), vecRes.toString().getBytes(), StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public BufferedImage reconstruct_DCT_image(ArrayList<DCTObject> objs, BufferedImage img) {
		BufferedImage render = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		for (DCTObject obj : objs) {
			YCbCrMakroBlock block = this.MAKRO_BLOCK_ENGINE.apply_IDCT(obj);
			
			for (int y = 0; y < block.getColors().length; y++) {
				for (int x = 0; x < block.getColors()[y].length; x++) {
					if (block.getPosition().x + x >= img.getWidth()
						|| block.getPosition().y + y >= img.getHeight()) {
						continue;
					}
					
					int col = this.COLOR_MANAGER.convert_YCbCr_to_RGB(block.getColors()[y][x]).getRGB();
					
					if (block.getColors()[y][x].getA() == 1.0) {
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
	
	public BufferedImage build_Frame(BufferedImage org, ArrayList<YCbCrMakroBlock> diffs, ArrayList<Vector> vecs, int diff) {
		BufferedImage img = new BufferedImage(org.getWidth(), org.getHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage img_v = new BufferedImage(org.getWidth(), org.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		BufferedImage vectors = new BufferedImage(org.getWidth(), org.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D v_g2d = vectors.createGraphics();
		v_g2d.setColor(Color.red);
		
		for (YCbCrMakroBlock block : diffs) {
			for (int y = 0; y < block.getColors().length; y++) {
				for (int x = 0; x < block.getColors()[y].length; x++) {
					if (block.getPosition().x + x >= img.getWidth()
						|| block.getPosition().y + y >= img.getHeight()) {
						continue;
					}
					
					int col = this.COLOR_MANAGER.convert_YCbCr_to_RGB(block.getColors()[y][x]).getRGB();
					
					if (block.getColors()[y][x].getA() == 1.0) {
						continue;
					}
					
					img.setRGB(block.getPosition().x + x, block.getPosition().y + y, col);
				}
			}
		}
		
		if (vecs != null) {
			for (Vector vec : vecs) {
				YCbCrColor[][] cols = vec.getMostEqualBlock().getColors();
				
				for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
					for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
						int vecEndX = vec.getStartingPoint().x + vec.getSpanX();
						int vecEndY = vec.getStartingPoint().y + vec.getSpanY();
						
						if (vecEndX + x >= img_v.getWidth()
							|| vecEndY + y >= img_v.getHeight()
							|| vecEndX + x < 0
							|| vecEndY + y < 0) {
							continue;
						}
						
						if (cols[y][x].getA() == 1.0) {
							continue;
						}
						
						int color = this.COLOR_MANAGER.convert_YCbCr_to_RGB(cols[y][x]).getRGB();
						
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
				g2d.drawImage(org, 0, 0, img.getWidth(), img.getHeight(), null, null);
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
}