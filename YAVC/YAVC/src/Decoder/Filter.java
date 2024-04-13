package Decoder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Filter {
	public BufferedImage apply_gaussian_blur(BufferedImage img, int radius) {
		BufferedImage render = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		double sigma = Math.max(((double)radius / 2), 1);
		double[][] kernel = new double[radius][radius];
		int mean = radius / 2;
		
		double sum = 0.0D;
		
		for (int x = 0; x < radius; x++) {
			for (int y = 0; y < radius; y++) {
				double value = Math.exp(-0.5 * (Math.pow((x - mean) / sigma, 2) + Math.pow((y - mean) / sigma, 2)) / (2 * Math.PI * sigma * sigma));
				
				kernel[x][y] = value;
				sum += value;
			}
		}
		
		for (int x = 0; x < kernel.length; x++) {
			for (int y = 0; y < kernel[x].length; y++) {
				kernel[x][y] /= sum;
			}
		}
		
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int r = 0, g = 0, b = 0;
				
				for (int kx = 0; kx < radius; kx++) {
					for (int ky = 0; ky < radius; ky++) {
						int pX = Math.min(Math.max(x + kx, 0), img.getWidth() - 1);
						int pY = Math.min(Math.max(y + ky, 0), img.getHeight() - 1);
						Color col = new Color(img.getRGB(pX, pY));
						
						double kVal = kernel[kx][ky];
						r += (int)(col.getRed() * kVal);
						g += (int)(col.getGreen() * kVal);
						b += (int)(col.getBlue() * kVal);
					}
				}
				
				render.setRGB(x, y, new Color(r, g, b).getRGB());
			}
		}
		
		return render;
	}
}
