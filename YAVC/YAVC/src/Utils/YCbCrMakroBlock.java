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

import java.awt.Point;

public class YCbCrMakroBlock {
	private YCbCrColor[][] colors = null;
	private Point position = new Point(0, 0);
	private double SAD = Float.MAX_VALUE;
	private int referenceDrawback = 0;
	private int size = 0;
	private boolean edgeBlock = false;

	public YCbCrMakroBlock(YCbCrColor[][] colors, Point position, int size) {
		this.colors = colors;
		this.position = position;
		this.size = size;
	}
	
	public YCbCrColor[][] getColors() {
		return colors;
	}
	
	public Point getPosition() {
		return position;
	}
	
	public void setPosition(Point position) {
		this.position = position;
	}

	public boolean isEdgeBlock() {
		return edgeBlock;
	}

	public void setEdgeBlock(boolean edgeBlock) {
		this.edgeBlock = edgeBlock;
	}

	public double getSAD() {
		return SAD;
	}

	public void setSAD(double sAD) {
		SAD = sAD;
	}

	public int getReferenceDrawback() {
		return referenceDrawback;
	}

	public void setReferenceDrawback(int referenceDrawback) {
		this.referenceDrawback = referenceDrawback;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public YCbCrMakroBlock[] splitToSmaller(int size) {
		if (this.colors == null) {
			System.err.println("Can't execute split!");
			System.err.println("Colors is NULL, nothing to split!");
			System.exit(0);
		}
		
		YCbCrMakroBlock[] b = new YCbCrMakroBlock[colors.length / size * colors[0].length / size];
		int index = 0;
		
		for (int y = 0; y < this.colors.length; y += size) {
			for (int x = 0; x < this.colors[y].length; x += size) {
				b[index++] = getSubBlock(size, x, y, index);
			}
		}
		
		return b;
	}
	
	private YCbCrMakroBlock getSubBlock(int size, int px, int py, int index) {
		YCbCrColor[][] cols = new YCbCrColor[size][size];
		
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				if (py + y >= this.colors.length
					|| px + x >= this.colors[0].length) {
					cols[y][x] = new YCbCrColor(0, 0, 0, 255);
					continue;
				}
				
				cols[y][x] = this.colors[y + py][x + px];
			}
		}
		
		return new YCbCrMakroBlock(cols, new Point(this.position.x + px, this.position.y + py), size);
	}
}
