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

public class Vector {
	private Point startingPoint = new Point(0, 0);
	private int spanX = 0;
	private int spanY = 0;
	private int referenceDrawback = 0;
	private int referenceSize = 4;
	private YCbCrMakroBlock appendedBlock = null;
	private YCbCrMakroBlock mostEqualBlock = null;
	
	public Vector() {}
	
	public Vector(Point start, int spanX, int spanY, int refB, int refS) {
		this.startingPoint = start;
		this.spanX = spanX;
		this.spanY = spanY;
		this.referenceDrawback = refB;
		this.referenceSize = refS;
	}
	
	public Point getStartingPoint() {
		return startingPoint;
	}
	
	public void setStartingPoint(Point startingPoint) {
		this.startingPoint = startingPoint;
	}
	
	public int getSpanX() {
		return this.spanX;
	}
	
	public void setSpanX(int span) {
		this.spanX = span;
	}
	
	public int getSpanY() {
		return this.spanY;
	}
	
	public void setSpanY(int span) {
		this.spanY = span;
	}

	public YCbCrMakroBlock getAppendedBlock() {
		return appendedBlock;
	}

	public void setAppendedBlock(YCbCrMakroBlock appendedBlock) {
		this.appendedBlock = appendedBlock;
	}

	public YCbCrMakroBlock getMostEqualBlock() {
		return mostEqualBlock;
	}

	public void setMostEqualBlock(YCbCrMakroBlock mostEqualBlock) {
		this.mostEqualBlock = mostEqualBlock;
	}

	public int getReferenceDrawback() {
		return referenceDrawback;
	}

	public void setReferenceDrawback(int referenceDrawback) {
		this.referenceDrawback = referenceDrawback;
	}

	public int getReferenceSize() {
		return referenceSize;
	}

	public void setReferenceSize(int referenceSize) {
		this.referenceSize = referenceSize;
	}
}
