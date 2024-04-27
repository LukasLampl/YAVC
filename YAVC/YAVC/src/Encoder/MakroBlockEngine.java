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

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import Main.config;
import Utils.ColorManager;
import Utils.DCTObject;
import Utils.Filter;
import Utils.MakroBlock;
import Utils.PixelRaster;
import Utils.YCbCrColor;
import Utils.YCbCrMakroBlock;

public class MakroBlockEngine {
	private ColorManager COLOR_MANAGER = new ColorManager();
	
	/*
	 * Purpose: Get a list of MakroBlocks out of an image
	 * Return Type: ArrayList<MakroBlock> => MakroBlocks of the image
	 * Params: BufferedImage img => Image from which the MakroBlocks should be ripped off
	 */
	public ArrayList<YCbCrMakroBlock> get_makroblocks_from_image(PixelRaster img, int[][] edges) {
		ArrayList<YCbCrMakroBlock> blocks = new ArrayList<YCbCrMakroBlock>();
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		for (int y = 0; y < height; y += config.SUPER_BLOCK) {
			for (int x = 0; x < width; x += config.SUPER_BLOCK) {
				YCbCrMakroBlock originBlock = get_single_makro_block(new Point(x, y), img, config.SUPER_BLOCK);
				blocks.addAll(divide_down_MakroBlock(originBlock, edges, config.SUPER_BLOCK));
			}
		}
		
		return blocks;
	}
	
	private ArrayList<YCbCrMakroBlock> divide_down_MakroBlock(YCbCrMakroBlock block, int[][] edges, int size) {
		ArrayList<YCbCrMakroBlock> blocks = new ArrayList<YCbCrMakroBlock>();
		
		if (size <= 4) {
			blocks.add(block);
			return blocks;
		}
		
		double detail = calculate_detail(edges, block.getPosition().x, block.getPosition().y, size);
		boolean passed = false;
		
		switch (size) {
		case 32:
			passed = detail > size * 1.1 ? true : false; //Small pre-filtering (Find nearly all edges, that are crucial)
			break;
		case 16:
			passed = detail > size * 2.5 ? true : false; //Moderate pre-filtering (Find edges of interest)
			break;
		case 8:
			passed = detail > size * 5.5 ? true : false; //High pre-filtering (Get edges with really high interest)
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
	private double calculate_detail(int edges[][], int x, int y, int size) {
		double sum = 0;
		
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
		
		return sum / (double)(size * size);
	}
	
	/*
	 * Purpose: Converts a list of MakroBlocks to an list of YUVMakroBlocks
	 * Return Type: ArrayList<YUVMakroBlock> => Converted MakroBlocks list
	 * Params: ArrayList<MakroBlock> blocks => List of MakroBlocks to be converted
	 */
	public ArrayList<YCbCrMakroBlock> convert_MakroBlocks_to_YCbCrMarkoBlocks(ArrayList<MakroBlock> blocks) {
		ArrayList<YCbCrMakroBlock> convertedBlocks = new ArrayList<YCbCrMakroBlock>(blocks.size());
		
		for (MakroBlock b : blocks) {
			convertedBlocks.add(convert_single_MakroBlock_to_YCbCrMakroBlock(b));
		}

		return convertedBlocks;
	}
	
	/*
	 * Purpose: Converts a single MakroBlock to an YUVMakroBlock
	 * Return Type: YUVMakroBlock => Converted MakroBlock
	 * Params: MakroBlock block => MakroBlock to be converted
	 */
	public YCbCrMakroBlock convert_single_MakroBlock_to_YCbCrMakroBlock(MakroBlock block) {
		YCbCrColor[][] cols = new YCbCrColor[block.getSize()][block.getSize()];
		boolean[][] colorIgnores = block.getColorIgnore();
		int[][] colors = block.getColors();
		
		for (int y = 0; y < block.getSize(); y++) {
			for (int x = 0; x < block.getSize(); x++) {
				cols[y][x] = this.COLOR_MANAGER.convert_RGB_to_YCbCr(new Color(colors[y][x]));
				
				if (colorIgnores[y][x] == true) {
					cols[y][x].setA(255);
				}
			}
		}
		
		return new YCbCrMakroBlock(cols, block.getPosition(), block.getSize());
	}
	
	/*
	 * Purpose: Subsample a whole YCbCrMakroBlock array
	 * Return Type: void
	 * Params: ArrayList<YCbCrMakroBlock> blocks => Blocks to subsample
	 */
	public void sub_sample_YCbCrMakroBlocks(ArrayList<YCbCrMakroBlock> blocks) {
		for (YCbCrMakroBlock b : blocks) {
			sub_sample_YCbCrMakroBlock(b);
		}
	}
	
	/*
	 * Purpose: Subsample a YCbCrMakroBlock down to 4:2:0
	 * Return Type: void
	 * Params: YCbCrMakroBlock block => Block to subsample
	 */
	private void sub_sample_YCbCrMakroBlock(YCbCrMakroBlock block) {
		YCbCrColor[][] cols = block.getColors();
		
		for (int y = 0; y < cols.length; y += 2) {
			if (y < 0 || y + 1 >= cols.length) {
				continue;
			}
			
			for (int x = 0; x < cols[y].length; x += 2) {
				if (x < 0 || x >= cols[y].length) {
					continue;
				}
				
				double CbVal = (cols[x][y].getCb() + cols[x + 1][y].getCb() + cols[x][y + 1].getCb() + cols[x + 1][y + 1].getCb()) / 4;
				double CrVal = (cols[x][y].getCr() + cols[x + 1][y].getCr() + cols[x][y + 1].getCr() + cols[x + 1][y + 1].getCr()) / 4;
				cols[x + 1][y].setCb(CbVal);
				cols[x + 1][y + 1].setCb(CbVal);
				cols[x][y + 1].setCb(CbVal);
				cols[x + 1][y].setCr(CrVal);
				cols[x + 1][y + 1].setCr(CrVal);
				cols[x][y + 1].setCr(CrVal);
			}
		}
	}
	
	/*
	 * Purpose: Run the DCT-II for a list of YCbCrMakroBlocks
	 * Return Type: ArrayList<DCTObject> => List with all DCT coefficients
	 * Params: ArrayList<YCbCrMakroBlock> blocks => Blocks to convert
	 */
	public ArrayList<DCTObject> apply_DCT_on_blocks(ArrayList<YCbCrMakroBlock> blocks) {
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
	
	/*
	 * Purpose: Reverse the subsampling of colors from a 2x2 to 4x4 matrix
	 * Return Type: void
	 * Params: YCbCrColor[][] target => Target to write results into;
	 * 			double[][] coCb => Cb coefficients;
	 * 			double[][] coCr => Cr coefficients
	 */
	private void reverse_subsample(YCbCrColor[][] target, double[][] coCb, double[][] coCr) {
		for (int y = 0; y < target.length; y += 2) {
			for (int x = 0; x < target.length; x += 2) {
				double cb = coCb[x / 2][y / 2];
				double cr = coCr[x / 2][y / 2];
				
				target[x][y].setCb(cb);
				target[x + 1][y].setCb(cb);
				target[x][y + 1].setCb(cb);
				target[x + 1][y + 1].setCb(cb);
				target[x][y].setCr(cr);
				target[x + 1][y].setCr(cr);
				target[x][y + 1].setCr(cr);
				target[x + 1][y + 1].setCr(cr);
			}
		}
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
		double[][] CbCol = this.COLOR_MANAGER.get_YCbCr_comp_sub_sample(block.getColors(), YCbCrComp.CB, block.getSize());
		double[][] CrCol = this.COLOR_MANAGER.get_YCbCr_comp_sub_sample(block.getColors(), YCbCrComp.CR, block.getSize());
		
		double[][] YCol = new double[block.getSize()][block.getSize()];
		double[][] CbCo = new double[block.getSize() / 2][block.getSize() / 2];
		double[][] CrCo = new double[block.getSize() / 2][block.getSize() / 2];
		
		for (int x = 0; x < block.getSize(); x++) {
			for (int y = 0; y < block.getSize(); y++) {
				YCol[x][y] = block.getColors()[x][y].getY();
			}
		}
		
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
		
		return new DCTObject(YCol, CbCo, CrCo, block.getPosition());
	}
	
	/*
	 * Purpose: Apply the IDCT-II to a single YCbCrMakroBlock with subsampling of 2x2 chroma
	 * Return Type: YCbCrMakroBlock => YCbCrMakroBlock with all DCTObject information
	 * Params: DCTObject obj => Object to apply IDCT-II to
	 */
	public YCbCrMakroBlock apply_IDCT(DCTObject obj) {
		YCbCrColor[][] col = new YCbCrColor[obj.getSize()][obj.getSize()];
		
		for (int i = 0; i < col.length; i++) {
			for (int n = 0; n < col[i].length; n++) {
				col[i][n] = new YCbCrColor(obj.getY()[i][n], 0, 0);
			}
		}
		
		double[][] coCb = new double[obj.getSize() / 2][obj.getSize() / 2];
		double[][] coCr = new double[obj.getSize() / 2][obj.getSize() / 2];
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
				
				coCb[x][y] = CbSum;
				coCr[x][y] = CrSum;
			}
		}
		
		reverse_subsample(col, coCb, coCr);
		
		return new YCbCrMakroBlock(col, obj.getPosition(), obj.getSize());
	}
	
	/*
	 * Purpose: Get a single MakroBlock out of the whole image
	 * Return Type: MakroBlock => Analyzed MakroBlock
	 * Params: Point position => Position from where to grab the MakroBlock;
	 * 			PixelRaster img => Image from which the MakroBlock should be grabbed
	 */
	public YCbCrMakroBlock get_single_makro_block(Point position, PixelRaster img, int size) {
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
		YCbCrColor[][] colors = new YCbCrColor[size][size];
		
		int maxX = img.getWidth();
		int maxY = img.getHeight();
		boolean setRestToNil = false;
		
		for (int y = 0; y < size; y++) {
			if (y + position.y >= maxY || y + position.y < 0) {
				colors[y][0] = new YCbCrColor(0, 0, 0, 255);
				setRestToNil = true;
			}
			
			for (int x = 0; x < size; x++) {
				if (!setRestToNil) {
					if (x + position.x >= maxX || x + position.x < 0) {
						colors[y][x] = new YCbCrColor(0, 0, 0, 255);
						continue;
					}
					
					colors[y][x] = this.COLOR_MANAGER.convert_RGB_to_YCbCr(img.getRGB(x + position.x, y + position.y));
			
				} else {
					colors[y][x] = new YCbCrColor(0, 0, 0, 255);
				}
			}
		}
		
		return new YCbCrMakroBlock(colors, position, size);
	}
}
