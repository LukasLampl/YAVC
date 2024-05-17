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

package Main;

public class config {
	//Size of a MakroBlock in the encoding and decoding process
	public static final int SUPER_BLOCK = 64;
	public static final int SMALL_BLOCK = 4;
	
	public static final int MAX_BACK_REF = 7;
	
	public static final int RESERVED_TABLE_SIZE = 45;
	public static final char V_DEF_S = (char)1;
	public static final char DCT_DEF_S = (char)2;
	public static final char DCT_Y_END_DEF = (char)3;
	public static final char DCT_CB_END_DEF = (char)4;
	public static final char DCT_CR_END_DEF = (char)5;
	public static final char DCT_MATRIX_NL_DEF = (char)6;
	public static final char DCT_POS_END_DEF = (char)7;
	public static final char NEW_FRAME_DEF = (char)8;
}
