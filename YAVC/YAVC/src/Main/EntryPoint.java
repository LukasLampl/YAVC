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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import Decoder.DataGrabber;
import Decoder.DataPipeEngine;
import Decoder.DataPipeValveEngine;
import Encoder.MakroBlockEngine;
import Encoder.MakroDifferenceEngine;
import Encoder.OutputWriter;
import Encoder.Scene;
import Encoder.VectorEngine;
import UI.Frame;
import Utils.DCTObject;
import Utils.Filter;
import Utils.PixelRaster;
import Utils.Status;
import Utils.Vector;
import Utils.YCbCrMakroBlock;

public class EntryPoint {
	private Status EN_STATUS = Status.STOPPED;
	private Status DE_STATUS = Status.STOPPED;
	
	private Filter FILTER = new Filter();
	private Scene SCENE = new Scene();
	private MakroBlockEngine MAKROBLOCK_ENGINE = new MakroBlockEngine();
	private MakroDifferenceEngine MAKROBLOCK_DIFFERENCE_ENGINE = new MakroDifferenceEngine();
	private VectorEngine VECTOR_ENGINE = new VectorEngine();
	private OutputWriter OUTPUT_WRITER = null;
	
	public boolean start_encode(Frame frame) {
		this.EN_STATUS = Status.RUNNING;
		
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Select your frames");
			chooser.showOpenDialog(null);
			
			File input = chooser.getSelectedFile();
			
			chooser.setDialogTitle("Chooser a destination");
			chooser.showOpenDialog(null);
			File output = chooser.getSelectedFile();
			
			this.OUTPUT_WRITER = new OutputWriter(output.getAbsolutePath(), frame);
			
			if (input == null || output == null) {
				return false;
			}
			
			Thread worker = new Thread(() -> {
				try {
					long timeStart = System.currentTimeMillis();
					ArrayList<PixelRaster> referenceImages = new ArrayList<PixelRaster>(config.MAX_BACK_REF);
					PixelRaster prevImage = null;
					PixelRaster currentImage = null;
					
					Dimension dim = null;
					int[][] edges = null;
					int[][] prevHistogram = new int[3][256];
					int[][] curHistogram = new int[3][256];
					int filesCount = input.listFiles().length;
					int changeDetectDistance = 0;
					
					for (int i = 0; i < filesCount && this.EN_STATUS == Status.RUNNING; i++, changeDetectDistance++) {
						frame.update_encoder_frame_count(i, filesCount, false);
						String name = set_awaited_file_name(i, ".bmp");
						File frameFile = new File(input.getAbsolutePath() + "/" + name);
						
						if (!frameFile.exists()) {
							System.out.println("SKIP:" + frameFile.getAbsolutePath());
							continue;
						}
						
						if (prevImage == null) {
							prevImage = new PixelRaster(ImageIO.read(frameFile));
							
							ArrayList<YCbCrMakroBlock> prevBlocks = this.MAKROBLOCK_ENGINE.get_makroblocks_from_image(prevImage, null, config.SUPER_BLOCK);
							ArrayList<DCTObject> DCT = this.MAKROBLOCK_ENGINE.apply_DCT_on_blocks(prevBlocks);
							prevImage = this.OUTPUT_WRITER.reconstruct_DCT_image(DCT, prevImage);
							
							referenceImages.add(prevImage);
							this.OUTPUT_WRITER.bake_meta_data(prevImage, filesCount);
							this.OUTPUT_WRITER.bake_start_frame(prevImage);
							
							dim = new Dimension(prevImage.getWidth(), prevImage.getHeight());
							edges = new int[dim.width][dim.height];
							this.FILTER.get_sobel_values(prevImage, edges, prevHistogram);
							continue;
						}
						
						currentImage = new PixelRaster(ImageIO.read(frameFile));
						
						this.FILTER.get_sobel_values(currentImage, edges, curHistogram);
						this.FILTER.damp_frame_colors(prevImage, currentImage); //CurrentImage gets updated automatically
						frame.set_previews(prevImage, currentImage);
						frame.set_sobel_image(this.FILTER.get_sobel_image());
						
						ArrayList<YCbCrMakroBlock> curImgBlocks = this.MAKROBLOCK_ENGINE.get_makroblocks_from_image(currentImage, edges, config.SUPER_BLOCK);
						
						//This only adds an I-Frame if 'i' is a 80th frame and a change
						//detection lied 10 frames ahead or a change detection has triggered.
						boolean sceneChanged = this.SCENE.scene_change_detected(prevHistogram, curHistogram, dim);
						
						if (sceneChanged == true) {
							System.out.println("Shot change dectedted at frame " + i);
						}
						
						if (this.FILTER.get_color_count() <= 750) {
							curImgBlocks = this.MAKROBLOCK_ENGINE.get_makroblocks_from_image(currentImage, edges, config.SMALL_BLOCK);
						}
						
						if ((i % 80 == 0 && changeDetectDistance > 10) || sceneChanged) {
							ArrayList<DCTObject> dct = this.MAKROBLOCK_ENGINE.apply_DCT_on_blocks(curImgBlocks);
							this.OUTPUT_WRITER.add_obj_to_queue(dct, null);
							referenceImages.clear();
							referenceImages.add(currentImage);
							prevImage = currentImage;
							prevHistogram = curHistogram;
							changeDetectDistance = sceneChanged == true ? 0 : changeDetectDistance;
							continue;
						}
						
						ArrayList<YCbCrMakroBlock> differences = this.MAKROBLOCK_DIFFERENCE_ENGINE.get_MakroBlock_difference(curImgBlocks, prevImage);
						this.FILTER.flatten_down_color(differences);
						frame.set_MBDiv_image(this.OUTPUT_WRITER.draw_MB_outlines(dim, curImgBlocks));
						frame.setDifferenceImage(differences, new Dimension(currentImage.getWidth(), currentImage.getHeight()));
						
						ArrayList<Vector> movementVectors = this.VECTOR_ENGINE.calculate_movement_vectors(referenceImages, differences, frame.get_vec_sad_tolerance(), this.FILTER.get_color_count());
						print_statistics(movementVectors, differences, dim);
						
						frame.setVectorizedImage(this.VECTOR_ENGINE.construct_vector_path(dim, movementVectors));

						BufferedImage result = this.OUTPUT_WRITER.build_Frame(prevImage, referenceImages, differences, movementVectors, 3);
						PixelRaster res = new PixelRaster(result);
						ArrayList<DCTObject> diffDCT = this.MAKROBLOCK_ENGINE.apply_DCT_on_blocks(differences);
						
						//Just for validation
						res = this.OUTPUT_WRITER.reconstruct_DCT_image(diffDCT, res);
						this.OUTPUT_WRITER.add_obj_to_queue(diffDCT, movementVectors);
						this.FILTER.apply_deblocking(res, curImgBlocks);
						
						referenceImages.add(res);
						release_old_reference_images(referenceImages);
						prevImage = res;
						prevHistogram = curHistogram;
					}
					
					frame.disposeWriterPermission();
					
					this.OUTPUT_WRITER.compress_result();
					frame.update_encoder_frame_count(filesCount + (filesCount / 10), filesCount, true);
					
					referenceImages.clear();
					
					long timeEnd = System.currentTimeMillis();
					System.out.println("Time: " + (timeEnd - timeStart) + "ms");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
			worker.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private void print_statistics(ArrayList<Vector> movementVectors, ArrayList<YCbCrMakroBlock> differences, Dimension dim) {
		int areaVecs = 0, areaDiffs = 0;
		
		for (Vector v : movementVectors) {
			areaVecs += Math.pow(v.getAppendedBlock().getSize(), 2);
		}
		
		for (YCbCrMakroBlock b : differences) {
			areaDiffs += Math.pow(b.getSize(), 2);
		}
		
		System.out.println("Vecs: " + movementVectors.size() + " (" + areaVecs + ") : " + differences.size() + " (" + areaDiffs + ")");
	}
	
	private String set_awaited_file_name(int i, String type) {
		String prefix = "";
		
		if (i < 10) {
			prefix = "000";
		} else if (i < 100) {
			prefix = "00";
		} else if (i < 1000) {
			prefix = "0";
		}
		
		return prefix + i + type;
	}
	
	public boolean start_decoding_process(Frame frame) {
		this.DE_STATUS = Status.RUNNING;
		
		try {
			JFileChooser chooser = new JFileChooser("Choose a \"yavc\" file");
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.showOpenDialog(null);
			
			File file = chooser.getSelectedFile();
			
			if (file == null) {
				return false;
			}
			
			Thread worker = new Thread(() -> {
				DataGrabber grabber = new DataGrabber();
				grabber.slice(file);
				
				DataPipeEngine dataPipeEngine = new DataPipeEngine(grabber);
				DataPipeValveEngine dataPipeValveEngine = new DataPipeValveEngine(file.getParent());
				
				ArrayList<BufferedImage> referenceImages = new ArrayList<BufferedImage>(config.MAX_BACK_REF);
				
				int frameCounter = 0;
				int maxFrames = dataPipeEngine.get_max_frame_number();
				BufferedImage prevFrame = null;
				BufferedImage currFrame = null;
				
				while (dataPipeEngine.hasNext(frameCounter) && this.DE_STATUS == Status.RUNNING) {
					if (prevFrame == null) {
						prevFrame = dataPipeEngine.scrape_main_image(grabber.get_start_frame());
						referenceImages.add(prevFrame);
						dataPipeValveEngine.release_image(prevFrame);
						continue;
					}
					
					currFrame = dataPipeEngine.scrape_next_frame(frameCounter);
					
					ArrayList<Vector> vecs = dataPipeEngine.scrape_vectors(frameCounter++);
					BufferedImage result = dataPipeEngine.build_frame(vecs, referenceImages, prevFrame, currFrame);
					BufferedImage outputImg = this.FILTER.apply_gaussian_blur(result, 1);
					
					frame.set_decoder_preview(outputImg);
					dataPipeValveEngine.release_image(outputImg);
					prevFrame = result;
					referenceImages.add(result);
					
					if (referenceImages.size() > config.MAX_BACK_REF) {
						referenceImages.remove(0);
					}
					
					frame.update_decoder_frame_count(frameCounter, maxFrames, false);
				}
			});
			
			worker.start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return true;
	}
	
	public void stop_encoding_process() {
		this.EN_STATUS = Status.STOPPED;
	}
	
	public void stop_decoding_process() {
		this.DE_STATUS = Status.STOPPED;
	}
	
	private void release_old_reference_images(ArrayList<PixelRaster> refList) {
		if (refList.size() < config.MAX_BACK_REF) {
			return;
		}
		
		refList.remove(0);
	}
}
