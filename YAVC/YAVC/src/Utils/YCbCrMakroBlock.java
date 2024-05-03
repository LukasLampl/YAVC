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
	private double[][] Y = null;
	private double[][] Cb = null;
	private double[][] Cr = null;
	private double[][] A = null;
	
	private Point position = new Point(0, 0);
	private double SAD = Float.MAX_VALUE;
	private int referenceDrawback = 0;
	private int size = 0;
	private boolean edgeBlock = false;
	private int complexity = 0;

	public YCbCrMakroBlock(double[][] Y, double[][] Cb, double[][] Cr, double[][] A, Point position, int size) {
		this.Y = Y;
		this.Cb = Cb;
		this.Cr = Cr;
		this.A = A;
		this.position = position;
		this.size = size;
	}
	
	public YCbCrMakroBlock(Point position, int size) {
		this.Y = new double[size][size];
		this.Cb = new double[size / 2][size / 2];
		this.Cr = new double[size / 2][size / 2];
		this.A = new double[size][size];
		this.position = position;
		this.size = size;
	}
	
	public double[][] getLuma() {
		return this.Y;
	}
	
	public double[][] getChromaCb() {
		return this.Cb;
	}
	
	public double[][] getCromaCr() {
		return this.Cr;
	}
	
	public void setYAndA(int x, int y, double Y, double A) {
		if (this.Y == null || this.A == null) {
			System.err.println("No YCbCr component!");
			return;
		}
		
		if (A > 0) {
			this.A[x][y] = A;
			return;
		}
		
		this.Y[x][y] = Y;
		this.A[x][y] = A;
	}
	
	public void setColor(int x, int y, double Y, double Cb, double Cr, double A) {
		if (this.Y == null || this.Cb == null || this.Cr == null || this.A == null) {
			System.err.println("No YCbCr component!");
			return;
		}
		
		if (A > 0) {
			this.A[x][y] = A;
			return;
		}
		
		this.Y[x][y] = Y;
		this.Cb[x][y] = Cb;
		this.Cr[x][y] = Cr;
		this.A[x][y] = A;
	}
	
	public double[] getReversedSubSampleColor(int x, int y) {
		if (x < 0 || x >= this.size) {
			System.err.println("Can't reverse subsampling, x out of bounds!");
			return null;
		} else if (y < 0 || y >= this.size) {
			System.err.println("Can't reverse subsampling, y out of bounds!");
			return null;
		}
		
		int subSPosX = Math.round(x / 2);
		int subSPosY = Math.round(y / 2);
		return new double[] {this.Y[x][y], this.Cb[subSPosX][subSPosY], this.Cr[subSPosX][subSPosY], this.A[x][y]};
	}
	
	public void setYVal(int x, int y, double Y) {
		if (this.Y == null) {
			System.err.println("No YCbCr component!");
			return;
		}
		
		this.Y[x][y] = Y;
	}
	
	public double getYVal(int x, int y) {
		return this.Y[x][y];
	}
	
	public double[][] getYValues() {
		return this.Y;
	}
	
	public void setYValues(double[][] YValues) {
		this.Y = YValues;
	}
	
	public void setChroma(int x, int y, double Cb, double Cr) {
		if (this.Cb == null || this.Cr == null) {
			System.err.println("No YCbCr component!");
			return;
		}
		
		this.Cb[x][y] = Cb;
		this.Cr[x][y] = Cr;
	}
	
	public double getAVal(int x, int y) {
		return this.A[x][y];
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
	
	/*
	 * Purpose: Splits a bigger MakroBlock to a smaller one
	 * Return Type: YCbCrMakroBlock[] => Array of smaller blocks
	 * Params: int size => Split size
	 */
	public YCbCrMakroBlock[] splitToSmaller(int size) {
		if (this.Y == null || this.Cb == null || this.Cr == null) {
			System.err.println("Can't execute split!");
			System.err.println("Colors is NULL, nothing to split!");
			return null;
		} else if (size <= 0) {
			System.err.println("Can't execute split!");
			System.err.println("Split to size 0 not possible!");
			return null;
		}
		
		YCbCrMakroBlock[] b = new YCbCrMakroBlock[this.size / size * this.size / size];
		int index = 0;
		
		for (int y = 0; y < this.size; y += size) {
			for (int x = 0; x < this.size; x += size) {
				b[index++] = getSubBlock(size, x, y);
			}
		}
		
		return b;
	}
	
	/*
	 * Purpose: Gets a Subblock from the current block
	 * Return Type: YCbCrMakroBlock => Smaller block
	 * Params: int size => Size of the smaller block;
	 * 			int px => Position X from color array;
	 * 			int py => Position Y from color array
	 */
	private YCbCrMakroBlock getSubBlock(int size, int px, int py) {
		YCbCrMakroBlock block = new YCbCrMakroBlock(new Point(this.position.x + px, this.position.y + py), size);
		
		for (int y = 0; y < size; y++) {
			int posY = py + y;
			
			for (int x = 0; x < size; x++) {
				int posX = px + x;
				
				if (posY >= this.size
					|| posX >= this.size) {
					block.setColor(y, x, 0, 0, 0, 255);
					continue;
				}
				
				block.setYAndA(x, y, this.Y[posX][posY], this.A[posX][posY]);
				block.setChroma(x / 2, y / 2, this.Cb[posX / 2][posY / 2], this.Cr[posX / 2][posY / 2]);
			}
		}
		
		return block;
	}

	public int getComplexity() {
		return complexity;
	}

	public void setComplexity(int complexity) {
		this.complexity = complexity;
	}
}
