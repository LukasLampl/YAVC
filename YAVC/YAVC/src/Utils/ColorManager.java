package Utils;

import java.awt.Color;

import Encoder.YCbCrComp;

public class ColorManager {
	/*
	 * Purpose: Convert a RGB color to an YUV color
	 * Return Type: YUVColor => Converted color
	 * Params: Color color => Color to be converted
	 */
	public YCbCrColor convert_RGB_to_YCbCr(Color color) {
		double Y = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
		double Cb = 128 - 0.168736 * color.getRed() - 0.331264 * color.getGreen() + 0.5 * color.getBlue();
		double Cr = 128 + 0.5 * color.getRed() - 0.418688 * color.getGreen() - 0.081312 * color.getBlue();
		return new YCbCrColor(Y, Cb, Cr);
	}
	
	public YCbCrColor convert_RGB_to_YCbCr(int color) {
		return convert_RGB_to_YCbCr(new Color(color));
	}
	
	public Color convert_YCbCr_to_RGB(YCbCrColor color) {
		int red = (int)Math.round(color.getY() + 1.402 * (color.getCr() - 128));
		int green = (int)Math.round(color.getY() - 0.344136 * (color.getCb() - 128) - 0.714136 * (color.getCr() - 128));
		int blue = (int)Math.round(color.getY() + 1.772 * (color.getCb() - 128));
		return new Color(Math.min(Math.max(red, 0), 255), Math.min(Math.max(green, 0), 255), Math.min(Math.max(blue, 0), 255));
	}
	
	public int convert_RGB_to_GRAYSCALE(int rgb) {
		Color col = new Color(rgb);
		return (int)Math.round(col.getRed() * 0.299 + col.getGreen() * 0.587 + col.getBlue() * 0.114);
	}
	
	public double[][] get_YCbCr_comp_sub_sample(YCbCrColor[][] color, YCbCrComp comp, int size) {
		double[][] sub = new double[size / 2][size / 2];
		
		int subY = 0;
		int subX = 0;
		
		for (int y = 0; y < size; y += 2) {
			for (int x = 0; x < size; x += 2) {
				if (subX >= sub.length) {
					subY++;
					subX = 0;
				}
				
				if (color[y][x] == null) {
					continue;
				}
				
				if (comp == YCbCrComp.CB) {
					sub[subY][subX++] = color[y][x].getCb();
				} else if (comp == YCbCrComp.CR) {
					sub[subY][subX++] = color[y][x].getCr();
				}
			}
		}
		
		return sub;
	}
}