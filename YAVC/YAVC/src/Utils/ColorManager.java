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

public class ColorManager {
	/*
	 * Purpose: Convert a RGB color to an YCbCr color
	 * Return Type: YUVColor => Converted color
	 * Params: Color color => Color to be converted
	 */
	public double[] convert_RGB_to_YCbCr(Color color) {
		double Y = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
		double Cb = 128 - 0.168736 * color.getRed() - 0.331264 * color.getGreen() + 0.5 * color.getBlue();
		double Cr = 128 + 0.5 * color.getRed() - 0.418688 * color.getGreen() - 0.081312 * color.getBlue();
		return new double[] {Y, Cb, Cr};
	}
	
	/*
	 * Purpose: Convert an int RGB color to an YCbCr color
	 * Return Type: YUVColor => Converted color
	 * Params: int color => Color to be converted
	 */
	public double[] convert_RGB_to_YCbCr(int color) {
		return convert_RGB_to_YCbCr(new Color(color));
	}
	
	/*
	 * Purpose: Convert a YCbCr color to an RGB color
	 * Return Type: YUVColor => Converted color
	 * Params: YCbCrColor color => Color to be converted
	 */
	public Color convert_YCbCr_to_RGB(double Y, double Cb, double Cr) {		
		int red = (int)Math.round(Y + 1.402 * (Cr - 128));
		int green = (int)Math.round(Y - 0.344136 * (Cb - 128) - 0.714136 * (Cr - 128));
		int blue = (int)Math.round(Y + 1.772 * (Cb - 128));
		return new Color(Math.min(Math.max(red, 0), 255), Math.min(Math.max(green, 0), 255), Math.min(Math.max(blue, 0), 255));
	}
	
	public Color convert_YCbCr_to_RGB(double[] YCbCr) {		
		return convert_YCbCr_to_RGB(YCbCr[0], YCbCr[1], YCbCr[2]);
	}
	
	/*
	 * Purpose: Convert an int RGB color to grayscale
	 * Return Type: int => Grayscale value
	 * Params: int argb => ARGB value to convert
	 */
	public int convert_RGB_to_GRAYSCALE(int argb) {
		Color col = new Color(argb);
		return (int)Math.round(col.getRed() * 0.299 + col.getGreen() * 0.587 + col.getBlue() * 0.114);
	}
}