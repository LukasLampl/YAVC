package Encoder;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Main.config;

public class MakroBlockEngine {
	private ColorManager COLOR_MANAGER = new ColorManager();
	
	/*
	 * Purpose: Get a list of MakroBlocks out of an image
	 * Return Type: ArrayList<MakroBlock> => MakroBlocks of the image
	 * Params: BufferedImage img => Image from which the MakroBlocks should be ripped off
	 */
	public ArrayList<MakroBlock> get_makroblocks_from_image(BufferedImage img) {
		int estimatedSize = (int)Math.round((double)(img.getWidth() * img.getHeight()) / (double)config.MBS_SQ);
		ArrayList<MakroBlock> blocks = new ArrayList<MakroBlock>(estimatedSize);
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		for (int y = 0; y < height; y += config.MAKRO_BLOCK_SIZE) {
			for (int x = 0; x < width; x += config.MAKRO_BLOCK_SIZE) {
				blocks.add(get_single_makro_block(new Point(x, y), img));
			}
		}
		
		return blocks;
	}
	
	/*
	 * Purpose: Damp the RGB colors of two different frames, so a bunch of redundancy appears.
	 * 			The algorithm also takes the edges and heights of the current frame in count.
	 * Return Type: ArrayList<MakroBlock> => List of damped MakroBlocks
	 * Params: ArrayList<MakroBlock> list1 => Previous frame MakroBlocks to check;
	 * 			ArrayList<MakroBlock> list2 => Current frame in which the colors should be damped;
	 * 			BufferedImage curImg => Current frame image;
	 * 			float dampingTolerance => Tolerance at which color damping occurs;
	 * 			int edgeTolerance => Tolerance at which the edges get recognized
	 */
	public ArrayList<MakroBlock> damp_MakroBlock_colors(ArrayList<MakroBlock> list1, ArrayList<MakroBlock> list2, BufferedImage curImg, float dampingTolerance, int edgeTolerance) {
		//Error on dimension mismatching
		if (list1.size() != list2.size()) {
			System.err.println("Not the same size!");
			return null;
		}
		
		ArrayList<MakroBlock> damped = new ArrayList<MakroBlock>(list2.size());
		
		for (int i = 0; i < list2.size(); i++) {
			int slightEquality = 0;
			
			for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
				for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
					Color prevCol = new Color(list1.get(i).getColors()[y][x]);
					Color curCol = new Color(list2.get(i).getColors()[y][x]);
					
					int deltaRed = Math.abs(prevCol.getRed() - curCol.getRed());
					int deltaGreen = Math.abs(prevCol.getGreen() - curCol.getGreen());
					int deltaBlue = Math.abs(prevCol.getBlue() - curCol.getBlue());
					
					//This is for preventing damping the colors, if an outline or edge is detected
					int edge = detect_edge_and_outlines(list2.get(i).getPosition().x + x, list2.get(i).getPosition().y + y, curImg);
					double weightedDelta = (deltaRed + deltaGreen + deltaBlue) * (1.0 + (edge / 255.0));
					
					if (weightedDelta <= edgeTolerance) {
						slightEquality++;
					}
				}
			}
			
			//75% Equality recommended (else you'll have a lot of artifacts)
			if (slightEquality >= (int)((double)config.MBS_SQ * dampingTolerance)) {
				damped.add(list1.get(i));
			} else {
				damped.add(list2.get(i));
			}
		}
		
		return damped;
	}
	
	/*
	 * Purpose: Calculates the importance of the MakroBlock by edges and heights using the Sobel edge detection
	 * Return Type: int => Height of the current MarkoBlock, so an importance difference can be measured 
	 * Params: int x => X Coordinate in the image;
	 * 			int y => Y Coordinate in the image;
	 * 			BufferedImage img => Image that requires the edge detection
	 */
	private static int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    private static int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
	
	private int detect_edge_and_outlines(int x, int y, BufferedImage img) {
        int gx = 0, gy = 0;
        
        for (int j = -1; j <= 1; j++) {
            for (int i = -1; i <= 1; i++) {
                int pixelX = Math.min(Math.max(x + i, 0), img.getWidth() - 1);
                int pixelY = Math.min(Math.max(y + j, 0), img.getHeight() - 1);
                int gray = img.getRGB(pixelX, pixelY) & 0xFF;
                gx += sobelX[j + 1][i + 1] * gray;
                gy += sobelY[j + 1][i + 1] * gray;
            }
        }
        
        return Math.abs(gx) + Math.abs(gy);
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
		YCbCrColor[][] cols = new YCbCrColor[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
		boolean[][] colorIgnores = block.getColorIgnore();
		int[][] colors = block.getColors();
		
		for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
			for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
				cols[y][x] = this.COLOR_MANAGER.convert_RGB_to_YCbCr(new Color(colors[y][x]));
				
				if (colorIgnores[y][x] == true) {
					cols[y][x].setA(255);
				}
			}
		}
		
		return new YCbCrMakroBlock(cols, block.getPosition());
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
			DCTObject dct = apply_DCT(b);
			
			obj.add(dct);
			apply_IDCT(dct);
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
		for (int x = 0; x < target.length; x += 2) {
			for (int y = 0; y < target[x].length; y += 2) {
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
	
	/*
	 * Purpose: Apply the DCT-II to a single YCbCrMakroBlock with subsampling of 2x2 chroma
	 * Return Type: DCTObject => Object with all coefficients
	 * Params: YCbCrMakroBlock block => Block to process
	 */
	private DCTObject apply_DCT(YCbCrMakroBlock block) {
		double[][] CbCol = this.COLOR_MANAGER.get_YCbCr_comp_sub_sample(block.getColors(), YCbCrComp.CB);
		double[][] CrCol = this.COLOR_MANAGER.get_YCbCr_comp_sub_sample(block.getColors(), YCbCrComp.CR);
		DCTObject obj = new DCTObject();
		
		double[][] YCol = new double[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
		
		for (int x = 0; x < block.getColors().length; x++) {
			for (int y = 0; y < block.getColors()[x].length; y++) {
				YCol[x][y] = block.getColors()[x][y].getY();
			}
		}
		
		obj.setY(YCol);
		obj.setPosition(block.getPosition());
		
		int m = CbCol.length, n = CbCol[0].length;
		
		for (int v = 0; v < m; v++) {
            for (int u = 0; u < n; u++) {
            	double CbSum = 0;
            	double CrSum = 0;
            	
                for (int x = 0; x < m; x++) {
                    for (int y = 0; y < n; y++) {
                        double cos1 = Math.cos((double)(2 * x + 1) * (double)u * Math.PI / (double)(2 * n));
                        double cos2 = Math.cos((double)(2 * y + 1) * (double)v * Math.PI / (double)(2 * m)); 
                        CbSum += CbCol[y][x] * cos1 * cos2;
                        CrSum += CrCol[y][x] * cos1 * cos2;
                    }
                }
                
                obj.getCbDCT()[v][u] = Math.round(step(v) * step(u) * CbSum);
                obj.getCrDCT()[v][u] = Math.round(step(v) * step(u) * CrSum);
            }
        }

		return obj;
	}
	
	/*
	 * Purpose: Apply the IDCT-II to a single YCbCrMakroBlock with subsampling of 2x2 chroma
	 * Return Type: YCbCrMakroBlock => YCbCrMakroBlock with all DCTObject information
	 * Params: DCTObject obj => Object to apply IDCT-II to
	 */
	public YCbCrMakroBlock apply_IDCT(DCTObject obj) {
		YCbCrColor[][] col = new YCbCrColor[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
		
		for (int i = 0; i < col.length; i++) {
			for (int n = 0; n < col[i].length; n++) {
				col[i][n] = new YCbCrColor();
				col[i][n].setY(obj.getY()[i][n]);
			}
		}
		
		double[][] coCb = new double[config.MAKRO_BLOCK_SIZE / 2][config.MAKRO_BLOCK_SIZE / 2];
		double[][] coCr = new double[config.MAKRO_BLOCK_SIZE / 2][config.MAKRO_BLOCK_SIZE / 2];
		int m = obj.getCbDCT().length, n = obj.getCbDCT()[0].length;

		for (int x = 0; x < m; x++) {
			for (int y = 0; y < n; y++) {
				double CbSum = 0;
				double CrSum = 0;
				
				for (int u = 0; u < m; u++) {
					for (int v = 0; v < n; v++) {
						double cos1 = Math.cos((double)(2 * x + 1) * (double)u * Math.PI / (double)(2 * n));
                        double cos2 = Math.cos((double)(2 * y + 1) * (double)v * Math.PI / (double)(2 * m)); 
                        CbSum += obj.getCbDCT()[u][v] * step(u) * step(v) * cos1 * cos2;
                        CrSum += obj.getCrDCT()[u][v] * step(u) * step(v) * cos1 * cos2;
					}
				}
				
				coCb[x][y] = CbSum;
				coCr[x][y] = CrSum;
			}
		}
		
		reverse_subsample(col, coCb, coCr);
		
		return new YCbCrMakroBlock(col, obj.getPosition());
	}
	
	/*
	 * Purpose: Get a single MakroBlock out of the whole image
	 * Return Type: MakroBlock => Analyzed MakroBlock
	 * Params: Point position => Position from where to grab the MakroBlock;
	 * 			img => Image from which the MakroBlock should be grabbed
	 */
	public MakroBlock get_single_makro_block(Point position, BufferedImage img) {
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
		int[][] colors = new int[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
		boolean[][] colorIgnore = new boolean[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
		int currentColumn = 0;
		int currentRow = 0;
		
		int maxX = img.getWidth();
		int maxY = img.getHeight();
		int maxMBY = position.y + config.MAKRO_BLOCK_SIZE;
		int maxMBX = position.x + config.MAKRO_BLOCK_SIZE;
		
		for (int y = position.y; y < maxMBY; y++) {
			if (y >= maxY || y < 0) {
				break;
			}
			
			for (int x = position.x; x < maxMBX; x++) {
				if (currentRow == config.MAKRO_BLOCK_SIZE) {
					currentColumn++;
					currentRow = 0;
				}
				
				if (x >= maxX || x < 0) {
					colorIgnore[currentColumn][currentRow] = true;
					colors[currentColumn][currentRow++] = Integer.MAX_VALUE; //ASCII for YAVC
					continue;
				}
				
				colorIgnore[currentColumn][currentRow] = false;
				colors[currentColumn][currentRow++] = img.getRGB(x, y);
			}
		}
		
		return new MakroBlock(colors, position, colorIgnore);
	}
}
