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
import Utils.DCTObject;
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
	public ArrayList<YCbCrMakroBlock> get_makroblocks_from_image(PixelRaster img, int[][] edges, int startSize) {
		if (img == null) {
			System.err.println("Frame NULL, can't partition NULL!");
			return null;
		} else if (edges == null) {
			System.err.println("Can't find \"details\" > abort MB partition!");
			return null;
		} else if (startSize <= 0) {
			System.err.println("Partitioning in size 0 or lower not possible! > Set size to 32x32.");
			startSize = config.SUPER_BLOCK;
		}
		
		ArrayList<YCbCrMakroBlock> blocks = new ArrayList<YCbCrMakroBlock>();
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		for (int y = 0; y < height; y += startSize) {
			for (int x = 0; x < width; x += startSize) {
				YCbCrMakroBlock originBlock = get_single_makro_block(new Point(x, y), img, startSize, null);
				blocks.addAll(divide_down_MakroBlock(originBlock, edges, startSize));
			}
		}
		
		return blocks;
	}
	
	/*
	 * Purpose: Recursively divide down MB's according to their detail level
	 * Return Type: ArrayList<YCbCrMakroBlock> => List of down splitted blocks
	 * Params: YCbCrMakroBlock block => Block to divide down;
	 * 			int edges[][] => Edges in the image;
	 * 			int size => Size of the current down divided MB
	 */
	private ArrayList<YCbCrMakroBlock> divide_down_MakroBlock(YCbCrMakroBlock block, int[][] edges, int size) {
		ArrayList<YCbCrMakroBlock> blocks = new ArrayList<YCbCrMakroBlock>();
		int rawDetail = calculate_detail(edges, block.getPosition().x, block.getPosition().y, size);
		
		if (size <= 4) {
			block.setComplexity(rawDetail);
			blocks.add(block);
			return blocks;
		}
		
		double detail = (double)rawDetail / (double)(size * size);
		boolean passed = false;
		
		switch (size) {
		case 32:
			passed = detail > size * 0.46 ? true : false; //Small pre-filtering (Find nearly all edges, that are crucial)
			break;
		case 16:
			passed = detail > size * 1.29 ? true : false; //Moderate pre-filtering (Find edges of interest)
			break;
		case 8:
			passed = detail > size * 2.74 ? true : false; //High pre-filtering (Get edges with really high interest)
			break;
		default:
			break;
		}
		
		if (passed == true) {
			YCbCrMakroBlock[] parts = block.splitToSmaller(size / 2);
			
			for (int i = 0; i < parts.length; i++) {
				 blocks.addAll(divide_down_MakroBlock(parts[i], edges, size / 2));
			}
		} else {
			block.setComplexity(rawDetail);
			blocks.add(block);
		}
		
		return blocks;
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
			if (j + x + 2 >= edges.length) {
				continue;
			}
			
			for (int k = 0; k < size; k++) {
				if (k + y + 2 >= edges[j].length) {
					continue;
				}
				
				sum += edges[j + x][k + y];
			}
		}
		
		return sum;
	}
	
	/*
	 * Purpose: Run the DCT-II for a list of YCbCrMakroBlocks
	 * Return Type: ArrayList<DCTObject> => List with all DCT coefficients
	 * Params: ArrayList<YCbCrMakroBlock> blocks => Blocks to convert
	 */
	public ArrayList<DCTObject> apply_DCT_on_blocks(ArrayList<YCbCrMakroBlock> blocks) {
		if (blocks == null) {
			System.err.println("Can't apply DCT-II on NULL! > Skip");
			return null;
		}
		
		ArrayList<DCTObject> obj = new ArrayList<DCTObject>(blocks.size());
		
		for (YCbCrMakroBlock b : blocks) {
			if (b.getSize() == 0) {
				continue;
			}
			
			ArrayList<DCTObject> dct = apply_DCT(b);
			obj.addAll(dct);
		}
		
		return obj;
	}

	/*
	 * Purpose: Determines the factor for DCT-II
	 * Return Type: double => Pre factor
	 * Params: int x => k, n, u or v of DCT-II
	 */
	private double step(int x) {
		return x == 0 ? 1 / Math.sqrt(2) : 1;
	}
	
	private ArrayList<DCTObject> apply_DCT(YCbCrMakroBlock block) {
		ArrayList<DCTObject> objs = new ArrayList<DCTObject>(block.getSize() * block.getSize() / 16);
		YCbCrMakroBlock[] blocks = block.splitToSmaller(4);
		
		for (YCbCrMakroBlock b : blocks) {
			objs.add(apply_DCT_to_single_4x4_block(b));
		}
		
		return objs;
	}
	
	/*
	 * Purpose: Apply the DCT-II to a single YCbCrMakroBlock with subsampling of 2x2 chroma
	 * Return Type: DCTObject => Object with all coefficients
	 * Params: YCbCrMakroBlock block => Block to process
	 */
	private DCTObject apply_DCT_to_single_4x4_block(YCbCrMakroBlock block) {
		double[][] CbCol = block.getChromaCb();
		double[][] CrCol = block.getCromaCr();
		
		double[][] CbCo = new double[block.getSize() / 2][block.getSize() / 2];
		double[][] CrCo = new double[block.getSize() / 2][block.getSize() / 2];

		int m = block.getSize() / 2;
		
		for (int v = 0; v < m; v++) {
            for (int u = 0; u < m; u++) {
            	double CbSum = 0;
            	double CrSum = 0;
            	
                for (int x = 0; x < m; x++) {
                    for (int y = 0; y < m; y++) {
                        double cos1 = Math.cos(((double)(2 * x + 1) * (double)u * Math.PI) / (double)(2 * m));
                        double cos2 = Math.cos(((double)(2 * y + 1) * (double)v * Math.PI) / (double)(2 * m)); 
                        CbSum += CbCol[y][x] * cos1 * cos2;
                        CrSum += CrCol[y][x] * cos1 * cos2;
                    }
                }
                
                CbCo[v][u] = Math.round(step(v) * step(u) * CbSum);
                CrCo[v][u] = Math.round(step(v) * step(u) * CrSum);
            }
        }
		
		return new DCTObject(block.getYValues(), CbCo, CrCo, block.getPosition());
	}
	
	/*
	 * Purpose: Apply the IDCT-II to a single YCbCrMakroBlock with subsampling of 2x2 chroma
	 * Return Type: YCbCrMakroBlock => YCbCrMakroBlock with all DCTObject information
	 * Params: DCTObject obj => Object to apply IDCT-II to
	 */
	public YCbCrMakroBlock apply_IDCT(DCTObject obj) {
		YCbCrMakroBlock block = new YCbCrMakroBlock(obj.getPosition(), obj.getSize());
		block.setYValues(obj.getY());
		int m = obj.getSize() / 2;
		
		for (int x = 0; x < m; x++) {
			for (int y = 0; y < m; y++) {
				double CbSum = 0;
				double CrSum = 0;
				
				for (int u = 0; u < m; u++) {
					for (int v = 0; v < m; v++) {
						double cos1 = Math.cos(((double)(2 * x + 1) * (double)u * Math.PI) / (double)(2 * m));
                        double cos2 = Math.cos(((double)(2 * y + 1) * (double)v * Math.PI) / (double)(2 * m)); 
                        CbSum += obj.getCbDCT()[u][v] * step(u) * step(v) * cos1 * cos2;
                        CrSum += obj.getCrDCT()[u][v] * step(u) * step(v) * cos1 * cos2;
					}
				}
				
				block.setChroma(x, y, CbSum, CrSum);
			}
		}

		return block;
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
