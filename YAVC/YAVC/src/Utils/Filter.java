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

package Utils;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import Encoder.MakroBlockEngine;
import Encoder.YCbCrComp;

public class Filter {
	private ColorManager COLOR_MANAGER = new ColorManager();
	private MakroBlockEngine MAKRO_BLOCK_ENGINE = new MakroBlockEngine();
	
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
	
	private int[][] sobelX = {{3, 0, -3}, {10, 0, -10}, {3, 0, -3}};
	private int[][] sobelY = {{3, 10, 3}, {0, 0, 0}, {-3, -10, -3}};
	private BufferedImage sobel_image = null;
	
	public int[][] get_sobel_values(PixelRaster img) {
		int[][] values = new int[img.getWidth()][img.getHeight()];
		sobel_image = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		for (int x = 0; x < img.getWidth() - 2; x++) {
			for (int y = 0; y < img.getHeight() - 2; y++) {
				double gX = (sobelX[0][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y)) +
							sobelX[0][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y)) +
							sobelX[0][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y))) +
							(sobelX[1][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y + 1)) +
							sobelX[1][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y + 1)) +
							sobelX[1][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y + 1))) +
							(sobelX[2][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y + 2)) +
							sobelX[2][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y + 2)) +
							sobelX[2][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y + 2)));
				
				double gY = (sobelY[0][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y)) +
						sobelY[0][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y)) +
						sobelY[0][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y))) +
						(sobelY[1][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y + 1)) +
						sobelY[1][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y + 1)) +
						sobelY[1][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y + 1))) +
						(sobelY[2][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y + 2)) +
						sobelY[2][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y + 2)) +
						sobelY[2][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y + 2)));
				
				int val = (int)Math.sqrt(gX * gX + gY * gY);
				values[x][y] = val;
				sobel_image.setRGB(x, y, new Color(Math.min(val, 255), Math.min(val, 255), Math.min(val, 255)).getRGB());
			}
		}
		
		return values;
	}
	
	public BufferedImage get_sobel_image() {
		return this.sobel_image;
	}
	
	/*
	 * Purpose: Damp the RGB color of ´´´img2´´´ so a lot of redundancy appears
	 * Return Type: void => img2 is set automatically
	 * Params: BufferedImage img1 => Reference image;
	 * 			BufferedImage img2 => Image to damp;
	 */
	public void damp_frame_colors(PixelRaster img1, PixelRaster img2) {
		//Error on dimension mismatching
		if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
			System.err.println("Images are not compareable!");
			return;
		}
		
		int width = img1.getWidth();
		int height = img1.getHeight();
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel1 = img1.getRGB(x, y);
				int pixel2 = img2.getRGB(x, y);
				YCbCrColor prevCol = this.COLOR_MANAGER.convert_RGB_to_YCbCr(pixel1);
				YCbCrColor curCol = this.COLOR_MANAGER.convert_RGB_to_YCbCr(pixel2);
				
				double deltaY = Math.abs(prevCol.getY() - curCol.getY());
				double deltaCb = Math.abs(prevCol.getCb() - curCol.getCb());
				double deltaCr = Math.abs(prevCol.getCr() - curCol.getCr());
				
				if (deltaY > 3.0 || deltaCb > 8.0 || deltaCr > 8.0) {
					continue;
				}
				
				img2.setRGB(x, y, pixel1);
			}
		}
	}
	
	private int c = 0;
	//NEIGHBOURBLOCKS IN FOLLOWING ORDER: TOP, LEFT, BOTTOM, RIGHT!
	public void apply_deblocking_filter(ArrayList<Vector> vecs, PixelRaster builtImg) {
		if (vecs != null) {
			for (Vector v : vecs) {
				Point nP = new Point(v.getStartingPoint().x - v.getReferenceSize(), v.getStartingPoint().y);
				YCbCrColor q[][] = this.MAKRO_BLOCK_ENGINE.get_single_makro_block(v.getStartingPoint(), builtImg, v.getReferenceSize()).getColors();
				YCbCrColor p[][] = this.MAKRO_BLOCK_ENGINE.get_single_makro_block(nP, builtImg, v.getReferenceSize()).getColors();
				int BS = 0;
				
				switch (v.getReferenceSize()) {
				case 32:
					BS = 4;
					break;
				case 16:
					BS = 3;
					break;
				case 8:
					BS = 2;
					break;
				case 4:
					BS = 1;
					break;
				}
				
				deblock(BS, q, p, Direction.VERTICAL);
				deblock(BS, q, p, Direction.HORIZONTAL);
				
				for (int y = 0; y < v.getReferenceSize(); y++) {
					for (int x = 0; x < v.getReferenceSize(); x++) {
						builtImg.setRGB(x + v.getStartingPoint().x, y + v.getStartingPoint().y, this.COLOR_MANAGER.convert_YCbCr_to_RGB(q[y][x]).getRGB());
						builtImg.setRGB(x + nP.x, y + nP.y, this.COLOR_MANAGER.convert_YCbCr_to_RGB(p[y][x]).getRGB());
					}
				}
			}
		}
		
		try {
			ImageIO.write(builtImg.toBufferedImage(), "png", new File("C:\\Users\\Lukas Lampl\\Documents\\result\\UP_" + c++ + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int clip(double min, double max, double x) {
		return (int)Math.max(min, Math.min(x, max));
	}
	
	private void deblock(int BS, YCbCrColor[][] p, YCbCrColor[][]q, Direction dir) {
		double beta = 28;
		double tc = 1;
		
		for (int i = 0; i < p.length && i < q.length; i++) {
			YCbCrColor p0 = null, p1 = null, p2 = null, p3 = null;
			YCbCrColor q0 = null, q1 = null, q2 = null, q3 = null;
			
			if (dir == Direction.VERTICAL) {
				p0 = p[0][i];
				p1 = p[1][i];
				p2 = p[2][i];
				p3 = p[3][i];
				q0 = q[q.length - 1][i];
				q1 = q[q.length - 2][i];
				q2 = q[q.length - 3][i];
				q3 = q[q.length - 4][i];
			} else if (dir == Direction.HORIZONTAL) {
				p0 = p[i][0];
				p1 = p[i][1];
				p2 = p[i][2];
				p3 = p[i][3];
				q0 = q[i][q.length - 1];
				q1 = q[i][q.length - 2];
				q2 = q[i][q.length - 3];
				q3 = q[i][q.length - 4];
			}
			
			if ((p2.getY() - 2 * p1.getY() + p0.getY()) + (p2.getY() - 2 * p1.getY() + p0.getY()) + (q2.getY() - 2 * q1.getY() + q0.getY()) + (q2.getY() - 2 * q1.getY() + q0.getY()) < beta) {
				continue;
			}

			if (BS == 2) {
				if ((p2.getY() - 2 * p1.getY() + p0.getY()) + (q2.getY() - 2 * q1.getY() + q0.getY()) < (beta / 8)
					&& (p3.getY() - p0.getY()) + (q0.getY() - q3.getY()) < (beta / 8)
					&& (p0.getY() - q0.getY()) < (2.5 * tc)) {
					p0.setY((int)(p2.getY() + 2 * p1.getY() + 2 * p0.getY() + 2 * q0.getY() + q1.getY() + 4) >> 3);
					p1.setY((int)(p2.getY() + p1.getY() + p0.getY() + q0.getY() + 2) >> 2);
					p2.setY((int)(2 * p3.getY() + 3 * p2.getY() + p1.getY() + p0.getY() + q0.getY() + 4) >> 3);
				}
				
				if ((q2.getY() - 2 * q1.getY() + q0.getY()) + (p2.getY() - 2 * p1.getY() + p0.getY()) < (beta / 8)
					&& (q3.getY() - q0.getY()) + (p0.getY() - p3.getY()) < (beta / 8)
					&& (q0.getY() - p0.getY()) < (2.5 * tc)) {
					q0.setY((int)(q2.getY() + 2 * q1.getY() + 2 * q0.getY() + 2 * p0.getY() + p1.getY() + 4) >> 3);
					q1.setY((int)(q2.getY() + q1.getY() + q0.getY() + p0.getY() + 2) >> 2);
					q2.setY((int)(2 * q3.getY() + 3 * q2.getY() + q1.getY() + q0.getY() + p0.getY() + 4) >> 3);
				}
			} else {
				if ((p2.getY() - 2 * p1.getY() + p0.getY()) + (p2.getY() - 2 * p1.getY() + p0.getY()) < ((3 / 16) * beta)
					&& (p2.getY() - 2 * p1.getY() + p0.getY()) + (p2.getY() - 2 * p1.getY() + p0.getY()) < ((3 / 16) * beta)) {
					int delta_F = clip(-tc, tc, (int)(9 * (q0.getY() - p0.getY()) - 3 * (q1.getY() - p1.getY()) + 8) >> 4);
					int delta_P1 = clip(-tc, tc, (int)((((int)(p2.getY() + p0.getY() + 1)) >> 1) - p1.getY() + delta_F) >> 1);
					int delta_Q1 = clip(-tc, tc, (int)((((int)(q2.getY() + q0.getY() + 1)) >> 1) - q1.getY() - delta_F) >> 1);
					p0.setY(p0.getY() + delta_F);
					q0.setY(q0.getY() - delta_F);
					p1.setY(p1.getY() + delta_P1);
					q1.setY(q1.getY() + delta_Q1);
				}
			}
		}
	}
	
	private void filter_Cb_Cr(int BS, YCbCrColor[] p, YCbCrColor[] q, YCbCrComp comp) {
		int alpha = 1;
		int beta = 2;
		
		double p0 = comp == YCbCrComp.CR ? p[0].getCr() : p[0].getCb();
		double p1 = comp == YCbCrComp.CR ? p[2].getCr() : p[2].getCb();
		double q0 = comp == YCbCrComp.CR ? q[0].getCr() : q[0].getCb();
		double q1 = comp == YCbCrComp.CR ? q[2].getCr() : q[2].getCb();
		
		if (!(BS != 0 && Math.abs(p0 - q0) < alpha && Math.abs(p1 - p0) < beta && Math.abs(q1 - q0) < beta)) {
			return;
		}
		
		double t_p0 = p0;
		double t_q0 = q0;
		
		if (BS == 4) {
			t_p0 = ((int)(2 * p1 + p0 + q1 + 2) >> 2);
			t_q0 = ((int)(2 * q1 + q0 + p1 + 2) >> 2);
		}
		
		if (comp == YCbCrComp.CR) {
			p[0].setCr(t_p0);
			q[0].setCr(t_q0);	
		} else {
			p[0].setCb(t_p0);
			q[0].setCb(t_q0);
		}
	}
}
