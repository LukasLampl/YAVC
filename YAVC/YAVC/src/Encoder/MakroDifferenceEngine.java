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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Utils.PixelRaster;
import Utils.YCbCrMakroBlock;

public class MakroDifferenceEngine {
	private MakroBlockEngine MAKRO_BLOCK_ENGINE = new MakroBlockEngine();
	
	/*
	 * Purpose: Get the differences between the MakroBlocks of two lists
	 * Return Type: ArrayList<MakroBlocks> => List of differences
	 * Params: ArrayList<YCbCrMakroBlock> list => List of MB's to compare with previous frame;
	 * 			PixelRaster prevImg => Previous image to compare to
	 */
	public ArrayList<YCbCrMakroBlock> get_MakroBlock_difference(ArrayList<YCbCrMakroBlock> list, PixelRaster prevImg) {
		if (list == null) {
			System.err.println("No Makroblocks to compare!");
			return null;
		} else if (prevImg == null) {
			System.err.println("No reference for differenciating!");
			return null;
		}
		
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
					YCbCrMakroBlock colors1 = list.get(index);
					YCbCrMakroBlock colors2 = this.MAKRO_BLOCK_ENGINE.get_single_makro_block(pos, prevImg, size, null);
					
					double sumY = 0;
					double sumCb = 0;
					double sumCr = 0;
					
					for (int y = 0; y < size; y++) {
						for (int x = 0; x < size; x++) {
							double[] col1 = colors1.getReversedSubSampleColor(x, y);
							double[] col2 = colors2.getReversedSubSampleColor(x, y);
							
							sumY += Math.abs(col1[0] - col2[0]);
							sumCb += Math.abs(col1[1] - col2[1]);
							sumCr += Math.abs(col1[2] - col2[2]);
						}
					}
					
					double normalY = sumY / (size * size);
					double normalCb = sumCb / (size * size);
					double normalCr = sumCr / (size * size);
					
					if (normalY > 1.55 || normalCb > 3.6 || normalCr > 3.6) return list.get(index);
					
					return null;
				};
				
				fmbs.add(executor.submit(task));
			}
			
			for (Future<YCbCrMakroBlock> f : fmbs) {
				try {
					YCbCrMakroBlock mb = f.get();
					
					if (mb != null) diffs.add(mb);
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
