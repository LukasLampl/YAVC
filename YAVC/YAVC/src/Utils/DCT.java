package Utils;

import java.util.ArrayList;

public class DCT {
	
	
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
			if (b.getSize() == 0) continue;
			
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
	private double step(int x, int m) {
		return x == 0 ? 1 / Math.sqrt(m) : Math.sqrt(2.0 / (double)m);
	}
	
	private ArrayList<DCTObject> apply_DCT(YCbCrMakroBlock block) {
		ArrayList<DCTObject> objs = new ArrayList<DCTObject>(block.getSize() * block.getSize() / 16);
		YCbCrMakroBlock[] blocks = null;
		
		blocks = block.splitToSmaller(4);
		
//		if (block.getSize() == 4 || block.getSize() == 8) {
//			blocks = new YCbCrMakroBlock[] {block};
//		} else {
//			blocks = block.splitToSmaller(8);
//		}
		
		for (YCbCrMakroBlock b : blocks) {
			objs.add(apply_DCT_to_single_block(b));
		}
		
		return objs;
	}
	
	/*
	 * Purpose: Apply the DCT-II to a single YCbCrMakroBlock with subsampling of 2x2 chroma
	 * Return Type: DCTObject => Object with all coefficients
	 * Params: YCbCrMakroBlock block => Block to process
	 */
	private DCTObject apply_DCT_to_single_block(YCbCrMakroBlock block) {
		double[][] CbCo = compute_DCT_coefficients(block.getChromaCb(), block.getSize() / 2);
		double[][] CrCo = compute_DCT_coefficients(block.getChromaCr(), block.getSize() / 2);
		
		return new DCTObject(block.getYValues(), CbCo, CrCo, block.getPosition());
	}
	
	private double[][] compute_DCT_coefficients(double[][] component, int m) {
		double res[][] = new double[m][m];
		
		for (int v = 0; v < m; v++) {
            for (int u = 0; u < m; u++) {
            	double sum = 0;
            	
                for (int x = 0; x < m; x++) {
                    for (int y = 0; y < m; y++) {
                        double cos1 = Math.cos(((double)(2 * x + 1) * (double)v * Math.PI) / (double)(2 * m));
                        double cos2 = Math.cos(((double)(2 * y + 1) * (double)u * Math.PI) / (double)(2 * m)); 
                        sum += (component[x][y] - 128) * cos1 * cos2;
                    }
                }
                
                res[v][u] = Math.round(step(v, m) * step(u, m) * sum);
            }
        }
		
		return res;
	}
	
	/*
	 * Purpose: Apply the IDCT-II to a single YCbCrMakroBlock with subsampling of 2x2 chroma
	 * Return Type: YCbCrMakroBlock => YCbCrMakroBlock with all DCTObject information
	 * Params: DCTObject obj => Object to apply IDCT-II to
	 */
	public YCbCrMakroBlock apply_IDCT(DCTObject obj) {
		double[][] CbVals = compute_IDCT_coefficients(obj.getCbDCT(), obj.getSize() / 2);
		double[][] CrVals = compute_IDCT_coefficients(obj.getCrDCT(), obj.getSize() / 2);
		
		return new YCbCrMakroBlock(obj.getYDCT(), CbVals, CrVals, null, obj.getPosition(), obj.getSize());
	}
	
	private double[][] compute_IDCT_coefficients(double[][] component, int m) {
		double[][] res = new double[m][m];
		
		for (int x = 0; x < m; x++) {
			for (int y = 0; y < m; y++) {
				double sum = 0;
				
				for (int u = 0; u < m; u++) {
					for (int v = 0; v < m; v++) {
						double cos1 = Math.cos(((double)(2 * x + 1) * (double)u * Math.PI) / (double)(2 * m));
                        double cos2 = Math.cos(((double)(2 * y + 1) * (double)v * Math.PI) / (double)(2 * m)); 
                        sum += component[u][v] * step(u, m) * step(v, m) * cos1 * cos2;
					}
				}
				
				res[x][y] = sum + 128;
			}
		}
		
		return res;
	}
}
