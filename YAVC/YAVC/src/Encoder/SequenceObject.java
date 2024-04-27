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

package Encoder;

import java.util.ArrayList;

import Utils.DCTObject;
import Utils.Vector;

public class SequenceObject {
	private ArrayList<DCTObject> dct = null;
	private ArrayList<Vector> vecs = null;
	
	public ArrayList<DCTObject> getDCT() {
		return dct;
	}
	public void setDifferences(ArrayList<DCTObject> dct) {
		this.dct = dct;
	}
	public ArrayList<Vector> getVecs() {
		return vecs;
	}
	public void setVecs(ArrayList<Vector> vecs) {
		this.vecs = vecs;
	}
}
