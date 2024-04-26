package Encoder;

import java.awt.Point;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Utils.PixelRaster;
import Utils.YCbCrColor;
import Utils.YCbCrMakroBlock;

public class MakroDifferenceEngine {
	private MakroBlockEngine MAKRO_BLOCK_ENGINE = new MakroBlockEngine();
	
	/*
	 * Purpose: Get the differences between the MakroBlocks of two lists
	 * Return Type: ArrayList<MakroBlocks> => List of differences
	 * Params: ArrayList<MakroBlock> list1 => List1 to be compared with list2;
	 * 			ArrayList<MakroBlock> list2 => List2 to be compared with list1;
	 * 			BufferedImage img => Image in which the current MakroBlocks are located in;
	 */
	public ArrayList<YCbCrMakroBlock> get_MakroBlock_difference(ArrayList<YCbCrMakroBlock> list, PixelRaster prevImg, PixelRaster curImg) {
		ArrayList<YCbCrMakroBlock> diffs = new ArrayList<YCbCrMakroBlock>();
		ArrayList<Future<YCbCrMakroBlock>> fmbs = new ArrayList<Future<YCbCrMakroBlock>>();

		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		
		try {
			for (int i = 0; i < list.size(); i++) {
				final int index = i;
				final int size = list.get(i).getSize();
				final Point pos = list.get(i).getPosition();
				
				Callable<YCbCrMakroBlock> task = () -> {
					YCbCrColor[][] colors1 = list.get(index).getColors();
					
					YCbCrMakroBlock refBlock = this.MAKRO_BLOCK_ENGINE.get_single_makro_block(pos, prevImg, size);
					YCbCrColor[][] colors2 = refBlock.getColors();
					
					for (int y = 0; y < size; y++) {
						for (int x = 0; x < size; x++) {
							YCbCrColor col1 = colors1[y][x];
							YCbCrColor col2 = colors2[y][x];
							
							double deltaY = Math.abs(col1.getY() - col2.getY());
							double deltaCb = Math.abs(col1.getCb() - col2.getCb());
							double deltaCr = Math.abs(col1.getCr() - col2.getCr());
							
							if (deltaY > 3 || deltaCb > 8 || deltaCr > 8) {
								return list.get(index);
							}
						}
					}
					
					return null;
				};
				
				fmbs.add(executor.submit(task));
			}
			
			for (Future<YCbCrMakroBlock> f : fmbs) {
				try {
					YCbCrMakroBlock mb = f.get();
					
					if (mb != null) {
						diffs.add(mb);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			executor.shutdown();
		} catch (Exception e) {
			executor.shutdown();
			e.printStackTrace();
		}
		
		return diffs;
	}
}
