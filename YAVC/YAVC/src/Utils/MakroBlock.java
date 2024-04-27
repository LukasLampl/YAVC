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

public class MakroBlock {
	private int[][] colors = null;
	private boolean[][] colorIgnore = null;
	private Point position = new Point(0, 0);
	private int size = 16;
	private boolean isEdgeBlock = false;
	
	public MakroBlock(int[][] colors, Point position, boolean[][] colorIgnore, int size) {
		this.size = size;
		this.colors = colors;
		this.position = position;
		this.colorIgnore = colorIgnore;
	}
	
	public void setColorIgnore(int x, int y, boolean bool) {
		if (this.colorIgnore == null) {
			System.err.println("Color ignore = NULL!");
			return;
		}
		
		this.colorIgnore[x][y] = bool;
	}
	
	public boolean[][] getColorIgnore() {
		if (this.colorIgnore == null) {
			System.err.println("Color ignore = NULL!");
			System.exit(0);
		}
		
		return this.colorIgnore;
	}
	
	public void setColotIgnore(boolean[][] colorIgnore) {
		this.colorIgnore = colorIgnore;
	}
	
	public int[][] getColors() {
		return colors;
	}
	
	public void setColors(int[][] colors) {
		this.colors = colors;
	}
	
	public Point getPosition() {
		return position;
	}
	
	public void setPosition(Point position) {
		this.position = position;
	}

	public boolean isEdgeBlock() {
		return isEdgeBlock;
	}

	public void setEdgeBlock(boolean isEdgeBlock) {
		this.isEdgeBlock = isEdgeBlock;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return this.size;
	}
	public MakroBlock[] splitToSmaller(int size) {
		if (this.colors == null) {
			System.err.println("Can't execute split!");
			System.err.println("Colors is NULL, nothing to split!");
			System.exit(0);
		}
		
		MakroBlock[] b = new MakroBlock[colors.length / size * colors[0].length / size];
		int index = 0;
		
		for (int y = 0; y < this.colors.length; y += size) {
			for (int x = 0; x < this.colors[y].length; x += size) {
				b[index++] = getSubBlock(size, x, y, index);
			}
		}
		
		return b;
	}
	
	private MakroBlock getSubBlock(int size, int px, int py, int index) {
		int[][] cols = new int[size][size];
		boolean[][] colIgn = new boolean[size][size];
		
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				if (py + y >= this.colors.length
					|| px + x >= this.colors[0].length) {
					cols[y][x] = Integer.MAX_VALUE;
					colIgn[y][x] = true;
					continue;
				}

				cols[y][x] = this.colors[y + py][x + px];
			}
		}
		
		return new MakroBlock(cols, new Point(this.position.x + px, this.position.y + py), colorIgnore, size);
	}
}
