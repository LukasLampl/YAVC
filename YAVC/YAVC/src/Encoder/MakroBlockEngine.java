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

import java.awt.Point;
import java.util.ArrayList;

import Main.config;
import Utils.ColorManager;
import Utils.PixelRaster;
import Utils.YCbCrMakroBlock;

public class MakroBlockEngine {
	private ColorManager COLOR_MANAGER = new ColorManager();
	
	/*
	 * Purpose: Get a list of MakroBlocks out of an image
	 * Return Type: ArrayList<MakroBlock> => MakroBlocks of the image
	 * Params: PixelRaster img => Image from which the MB's should be get out of,
	 * 			int[][] edge => Edges and Textures in original frame;
	 * 			int startSize => Size from which to start dividing down (STD::32x32)
	 */
	public ArrayList<YCbCrMakroBlock> get_CTU_blocks_from_image(PixelRaster img, int[][] edges, int startSize) {
		if (img == null) {
			System.err.println("Frame NULL, can't partition NULL!");
			return null;
		} else if (startSize <= 0) {
			System.err.println("Partitioning in size 0 or lower not possible! > Set size to 32x32.");
			startSize = config.SUPER_BLOCK;
		}
		
		ArrayList<YCbCrMakroBlock> CTUs = new ArrayList<YCbCrMakroBlock>();
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		for (int y = 0; y < height; y += startSize) {
			for (int x = 0; x < width; x += startSize) {
				YCbCrMakroBlock originBlock = get_single_makro_block(new Point(x, y), img, startSize, null);
				CTUs.add(originBlock);
				
				if (edges != null) {
					get_CU_blocks(originBlock, edges, startSize);
				}
			}
		}
		
		return CTUs;
	}
	
	/*
	 * Purpose: Recursively divide down MB's according to their detail level
	 * Return Type: ArrayList<YCbCrMakroBlock> => List of down splitted blocks
	 * Params: YCbCrMakroBlock block => Block to divide down;
	 * 			int edges[][] => Edges in the image;
	 * 			int size => Size of the current down divided MB
	 */
	private void get_CU_blocks(YCbCrMakroBlock block, int[][] edges, int size) {
		int rawDetail = calculate_detail(edges, block.getPosition().x, block.getPosition().y, size);
		
		if (size <= 4) {
			return;
		}
		
		block.slice_CU(size / 2);

		for (YCbCrMakroBlock CU : block.get_CUs()) {
			get_CU_blocks(CU, edges, size / 2);
		}
	}
	
	/*
	 * Purpose: Get the sobel values of the current block
	 * Return Type: double => Value of interest
	 * Params: int edges[][] => Sobel operator result,
	 * 			int x => X coordinate of the block;
	 * 			int y => Y coordinate of the block;
	 * 			int size => Size of the block;
	 */
	private int calculate_detail(int edges[][], int x, int y, int size) {
		int sum = 0;
		
		for (int j = 0; j < size; j++) {
			if (j + x + 2 >= edges.length) continue;
			
			for (int k = 0; k < size; k++) {
				if (k + y + 2 >= edges[j].length) continue;
				
				sum += edges[j + x][k + y];
			}
		}
		
		return sum;
	}
	
	/*
	 * Purpose: Get a single MakroBlock out of the whole image
	 * Return Type: MakroBlock => Analyzed MakroBlock
	 * Params: Point position => Position from where to grab the MakroBlock;
	 * 			PixelRaster img => Image from which the MakroBlock should be grabbed
	 */
	public YCbCrMakroBlock get_single_makro_block(Point position, PixelRaster img, int size, YCbCrMakroBlock blockToFill) {
		/*
		 * Imagine the colors as a table:
		 * +---+---+---+---+---+---+---+---+
		 * | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | <- Column
		 * +---+---+---+---+---+---+---+---+
		 * | d | d | d | d | d | d | d | d | <- Data
		 * +---+---+---+---+---+---+---+---+
		 * | d | d | d | d | d | d | d | d |
		 * +---+---+---+---+---+---+---+---+
		 */
		YCbCrMakroBlock block = null;
		
		if (blockToFill == null) {
			block = new YCbCrMakroBlock(position, size);
		} else {
			block = blockToFill;
			block.setPosition(position);
			block.setSize(size);
		}
		
		int maxX = img.getWidth();
		int maxY = img.getHeight();
		boolean setRestToNil = false;
		
		for (int y = 0; y < size; y++) {
			if (y + position.y >= maxY || y + position.y < 0) {
				block.setColor(0, y, 0, 0, 0, 255);
				setRestToNil = true;
			}
			
			int subSPosY = y / 2;
			
			for (int x = 0; x < size; x++) {
				if (!setRestToNil) {
					if (x + position.x >= maxX || x + position.x < 0) {
						block.setColor(x, 0, 0, 0, 0, 255);
						continue;
					}
					
					int subSPosX = x / 2;
					double[] YCbCr = this.COLOR_MANAGER.convert_RGB_to_YCbCr(img.getRGB(x + position.x, y + position.y));
					block.setYVal(x, y, YCbCr[0]);
					block.setChroma(subSPosX, subSPosY, YCbCr[1], YCbCr[2]);
				} else {
					block.setColor(x, y, 0, 0, 0, 255);
				}
			}
		}
		
		return block;
	}
}
