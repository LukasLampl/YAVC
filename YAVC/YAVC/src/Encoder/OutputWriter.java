package Encoder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import Main.config;

public class OutputWriter {
	private File META_DIR = null;
	private Dimension META_DIMENSION = null;
	
	public OutputWriter(String path) {
		try {
			this.META_DIR = new File(path + "/YAVC-VIDEO.yavc.part");
			this.META_DIR.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		
		this.META_DIMENSION = new Dimension(originalImage.getWidth(), originalImage.getHeight());
		
		try {
			String meta = "META["
					+ "D[" + originalImage.getWidth() + "," + originalImage.getHeight() + "]"
					+ "FC[" + frameNum + "]"
					+ "]";
			
			Files.write(Path.of(this.META_DIR.getAbsolutePath()), meta.getBytes(), StandardOpenOption.WRITE);
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
		StringBuilder imgInChar = new StringBuilder(img.getHeight() * img.getWidth() + 2);
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				imgInChar.append(img.getRGB(x, y) + ".");
			}
		}
		
		try {
			Files.write(Path.of(this.META_DIR.getAbsolutePath()), "$S$".getBytes(), StandardOpenOption.APPEND);
			Files.write(Path.of(this.META_DIR.getAbsolutePath()), imgInChar.toString().getBytes(), StandardOpenOption.APPEND);
			Files.write(Path.of(this.META_DIR.getAbsolutePath()), "?".getBytes(), StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Purpose: Prepare and write a frame into the YAVC file without the "FIRST FRAME" importance
	 * Return Type: void
	 * Params: ArrayList<MakroBlock> differences => The differences between the previous and current frame
	 */
	private int outputFrames = 0;
	
	public void bake_frame(ArrayList<MakroBlock> differences) {
		BufferedImage render = new BufferedImage(this.META_DIMENSION.width, this.META_DIMENSION.height, BufferedImage.TYPE_INT_ARGB);
		
		for (MakroBlock b : differences) {
			for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
				for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
					if (b.getPosition().x + x >= render.getWidth()
						|| b.getPosition().y + y >= render.getHeight()) {
						continue;
					}
					
					if (b.getColors()[y][x] == 89658667) { //ASCII for YAVC
						continue;
					}
					
					Color col = new Color(b.getColors()[y][x]);
					render.setRGB(b.getPosition().x + x, b.getPosition().y + y, col.getRGB());
				}
			}
		}
		
		StringBuilder imgInChar = new StringBuilder(render.getHeight() * render.getWidth() + 2);
		
		for (int y = 0; y < render.getHeight(); y++) {
			for (int x = 0; x < render.getWidth(); x++) {
				imgInChar.append(render.getRGB(x, y) + ".");
			}
		}
		
		try {
			Files.write(Path.of(this.META_DIR.getAbsolutePath()), ("$P" + (this.outputFrames++) + "$").getBytes(), StandardOpenOption.APPEND);
			Files.write(Path.of(this.META_DIR.getAbsolutePath()), imgInChar.toString().getBytes(), StandardOpenOption.APPEND);
			Files.write(Path.of(this.META_DIR.getAbsolutePath()), "?".getBytes(), StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Purpose: Write calcuated vectors into the YAVC file
	 * Return Type: void
	 * Params: ArrayList<Vector> movementVectors => Calculated vectors of the current frame
	 * 			int frame => Frame number to which the vectors are applied
	 */
	public void bake_vectors(ArrayList<Vector> movementVectors, int frame) {
		if (movementVectors == null) {
			return;
		}
		
		if (this.META_DIR == null) {
			System.err.println("No output dir!");
			return;
		}
		
		try {
			StringBuilder vecRes = new StringBuilder(movementVectors.size() * 2);
			
			for (Vector vec : movementVectors) {
				vecRes.append("[" + vec.getStartingPoint().x + "," + vec.getStartingPoint().y + ";" + vec.getSpanX() + "," + vec.getSpanY() + "]");
			}
			
			Files.write(Path.of(this.META_DIR.getAbsolutePath()), ("$V" + (this.outputFrames - 1) + "$").getBytes(), StandardOpenOption.APPEND);
			Files.write(Path.of(this.META_DIR.getAbsolutePath()), vecRes.toString().getBytes(), StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Purpose: Compress the compressed result even more (using ZIP).
	 * 			File to be compressed is the file that was set during runtime.
	 * Return Type: void
	 * Params: void
	 */
	public void compress_result() {
		try {
			FileInputStream fis = new FileInputStream(this.META_DIR.getAbsolutePath());
	        BufferedInputStream bis = new BufferedInputStream(fis);
	
	        FileOutputStream fos = new FileOutputStream(this.META_DIR.getAbsolutePath().substring(0, this.META_DIR.getAbsolutePath().length() - 5));
	        BufferedOutputStream bos = new BufferedOutputStream(fos);
	        ZipOutputStream zipOut = new ZipOutputStream(bos);
	
	        zipOut.putNextEntry(new ZipEntry(new File(this.META_DIR.getAbsolutePath()).getName()));
	
	        byte[] buffer = new byte[8192];
	        int bytesRead;
	        while ((bytesRead = bis.read(buffer)) != -1) {
	            zipOut.write(buffer, 0, bytesRead);
	        }
	
	        zipOut.closeEntry();
	        bis.close();
	        zipOut.close();
	        this.META_DIR.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Purpose: Builds the encoded frame and writes it to the destination
	 * 			DEBUGGING ONLY!
	 * Return Type: void
	 * Params: BufferedImage org => previous build frame;
	 * 			ArrayList<MakroBlock> diffs => Differencs to the prev img;
	 * 			ArrayList<Vector> vecs => Vectors from the differences
	 */
	public int output = 0;
	
	public void build_Frame(BufferedImage org, ArrayList<MakroBlock> diffs, ArrayList<Vector> vecs) {
		BufferedImage img = new BufferedImage(org.getWidth(), org.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		for (MakroBlock block : diffs) {
			for (int y = 0; y < block.getColors().length; y++) {
				for (int x = 0; x < block.getColors()[y].length; x++) {
					if (block.getPosition().x + x >= img.getWidth()
						|| block.getPosition().y + y >= img.getHeight()) {
						continue;
					}
					
					if (block.getColors()[y][x] == 89658667) { //ASCII for YAVC
						continue;
					}
					
					Color col = new Color(block.getColors()[y][x]);
					img.setRGB(block.getPosition().x + x, block.getPosition().y + y, col.getRGB());
				}
			}
		}
		
		if (vecs != null) {
			for (Vector vec : vecs) {
				int[][] cols = vec.getMostEqualBlock().getColors();
				
				for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
					for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
						int vecEndX = vec.getStartingPoint().x + vec.getSpanX();
						int vecEndY = vec.getStartingPoint().y + vec.getSpanY();
						
						if (vecEndX + x >= img.getWidth()
							|| vecEndY + y >= img.getHeight()) {
							continue;
						}
						
						img.setRGB(vecEndX + x, vecEndY + y, cols[y][x]);
					}
				}
			}
		}
		
		File out = new File("C:\\Users\\Lukas Lampl\\Documents\\FRAMES\\out" + output++ + ".png");
		
		try {
			ImageIO.write(img, "png", out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
