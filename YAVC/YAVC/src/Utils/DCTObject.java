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

public class DCTObject {
	private double[][] Y = null;
	private double[][] CbDCT = null;
	private double[][] CrDCT = null;
	private final int size = 4;
	private Point Position = null;
	
	public DCTObject(double[][] YDCT, double[][] CbDCT, double[][] CrDCT, Point pos) {
		this.Y = YDCT;
		this.CbDCT = CbDCT;
		this.CrDCT = CrDCT;
		this.Position = pos;
	}
	
	public double[][] getY() {
		return Y;
	}
	public void setY(double[][] y) {
		Y = y;
	}
	public double[][] getCbDCT() {
		return CbDCT;
	}
	public void setCbDCT(double[][] cbDCT) {
		CbDCT = cbDCT;
	}
	public double[][] getCrDCT() {
		return CrDCT;
	}
	public void setCrDCT(double[][] crDCT) {
		CrDCT = crDCT;
	}
	public void setPosition(Point position) {
		Position = position;
	}
	public Point getPosition() {
		return Position;
	}
	public int getSize() {
		return this.size;
	}
}
