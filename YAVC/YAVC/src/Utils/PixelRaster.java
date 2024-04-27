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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

public class PixelRaster {
	private int[] data = null;
	private Dimension dim = null;
	
	public PixelRaster(BufferedImage img) {
		this.dim = new Dimension(img.getWidth(), img.getHeight());
		this.data = new int[img.getWidth() * img.getHeight()];
		
		if (img.getRaster().getDataBuffer() instanceof DataBufferInt) {
			this.data = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		} else if (img.getRaster().getDataBuffer() instanceof DataBufferByte) {
			byte[] buffer = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
			
			boolean hasAlpha = img.getAlphaRaster() != null ? true : false;
			
			for (int i = 0, index = 0; i < buffer.length; i++) {
				int argb = -16777216;
				
				if (hasAlpha) {
					argb += (((int)buffer[i++] & 0xff) << 24); //Alpha
				}
				
				argb += ((int)buffer[i++] & 0xff); //Blue
				argb += (((int)buffer[i++] & 0xff) << 8); //Green
				argb += (((int)buffer[i] & 0xff) << 16); //Red
				
				this.data[index++] = argb;
			}
		}
	}
	
	public int getRGB(int x, int y) {
		if (x >= this.dim.width) {
			throw new ArrayIndexOutOfBoundsException("(X) " + x + " is bigger than " + this.dim.width);
		} else if (y >= this.dim.height) {
			throw new ArrayIndexOutOfBoundsException("(Y) " + y + " is bigger than " + this.dim.height);
		}
		
		int pos = y * this.dim.width + x;
		return this.data[pos];
	}
	
	public void setRGB(int x, int y, int rgb) {
		int pos = y * this.dim.width + x;
		this.data[pos] = rgb;
	}
	
	public int getWidth() {
		return this.dim.width;
	}
	
	public int getHeight() {
		return this.dim.height;
	}
}
