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

public class YCbCrColor {
	private double Y = 0.0D;
	private double Cb = 0.0D;
	private double Cr = 0.0D;
	private int A = 0;
	
	public YCbCrColor(double Y, double Cb, double Cr) {
		this.Y = Y;
		this.Cb = Cb;
		this.Cr = Cr;
	}
	
	public YCbCrColor(double Y, double Cb, double Cr, int A) {
		this.Y = Y;
		this.Cb = Cb;
		this.Cr = Cr;
		this.A = A;
	}
	
	public int getA() {
		return A;
	}
	
	public void setA(int a) {
		this.A = a;
	}

	public double getY() {
		return Y;
	}

	public void setY(double y) {
		this.Y = y;
	}

	public double getCb() {
		return Cb;
	}

	public void setCb(double Cb) {
		this.Cb = Cb;
	}

	public double getCr() {
		return Cr;
	}

	public void setCr(double Cr) {
		this.Cr = Cr;
	}
}
