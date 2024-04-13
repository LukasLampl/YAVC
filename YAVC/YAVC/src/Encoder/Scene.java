package Encoder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Scene {
	private static int[][] sobelX = {{1, 0, -1}, {2, 0, +2}, {1, 0, -1}};
    private static int[][] sobelY = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};	
	
    private int edges = 0;
    
	public boolean scene_change_detected(BufferedImage img1, BufferedImage img2) {
		BufferedImage edge = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		int magnitudes[][] = new int[img1.getWidth()][img1.getHeight()];
		
		for (int y = 1; y < edge.getHeight() - 1; y++) {
			for (int x = 1; x < edge.getWidth() - 1; x++) {
				int gX = 0;
				int gY = 0;
				
				for (int sx = -1; sx < 1; sx++) {
					for (int sy = - 1; sy < 1; sy++) {
						int pixel = img1.getRGB(x + sx, y + sy) & 0xFF;
						gX += sobelX[sx + 1][sy + 1] * pixel;
						gY += sobelY[sx + 1][sy + 1] * pixel;
					}
				}
				
				int mag = (int)Math.sqrt(gX * gX + gY * gY);
				mag = Math.max(Math.min(mag, 255), 0);
				magnitudes[x][y] = mag;
			}
		}
		
		for (int x = 0; x < magnitudes.length; x++) {
			for (int y = 0; y < magnitudes[x].length; y++) {
				int highest = Integer.MIN_VALUE;
				
				for (int ix = -3; ix < 3; ix++) {
					for (int iy = -3; iy < 3; iy++) {
						if (x + ix < 0 || x + ix >= img1.getWidth()
							|| y + iy < 0 || y + iy >= img1.getHeight()) {
							continue;
						}
						
						if (magnitudes[x][y] > highest) {
							highest = magnitudes[x][y];
						}
					}
				}
				
				magnitudes[x][y] = highest;
			}
		}
		
		for (int x = 0; x < img1.getWidth(); x++) {
			for (int y = 0; y < img1.getHeight(); y++) {
				edge.setRGB(x, y, new Color(magnitudes[x][y], magnitudes[x][y], magnitudes[x][y]).getRGB());
			}
		}
		
		try {
			ImageIO.write(edge, "png", new File("C:\\Users\\Lukas Lampl\\Documents\\Res\\" + edges++ + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
}
