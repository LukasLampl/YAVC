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
	private static int MAX_REFERENCES = 5;
	private Status EN_STATUS = Status.STOPPED;
	private Status DE_STATUS = Status.STOPPED;
	
	public boolean start_encode(Frame f) {
		this.EN_STATUS = Status.RUNNING;
		
		try {
			JFileChooser chooser = new JFileChooser("Choose a destination");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.showOpenDialog(null);
			
			File input = chooser.getSelectedFile();
			
			chooser.showOpenDialog(null);
			File output = chooser.getSelectedFile();
			
			if (input == null || output == null) {
				return false;
			}
			
			Thread worker = new Thread(() -> {
				try {
					long timeStart = System.currentTimeMillis();
					ArrayList<PixelRaster> referenceImages = new ArrayList<PixelRaster>(MAX_REFERENCES);
					ArrayList<YCbCrMakroBlock> prevImgBlocks = null;
					
					PixelRaster prevImage = null;
					PixelRaster currentImage = null;
					
					Filter filter = new Filter();
					Scene scene = new Scene();
					MakroBlockEngine makroBlockEngine = new MakroBlockEngine();
					MakroDifferenceEngine makroDifferenceEngine = new MakroDifferenceEngine();
					VectorEngine vectorEngine = new VectorEngine();
					OutputWriter outputWriter = new OutputWriter(output.getAbsolutePath(), f);
					
					int filesCount = input.listFiles().length;
					int changeDetectDistance = 0;
					
					for (int i = 0; i < filesCount; i++, changeDetectDistance++) {
						if (this.EN_STATUS == Status.STOPPED) {
							output.delete();
						}
						
						f.update_encoder_frame_count(i, filesCount, false);
						String name = "";
						
						if (i < 10) {
							name = "000" + i;
						} else if (i < 100) {
							name = "00" + i;
						} else if (i < 1000) {
							name = "0" + i;
						} else {
							name = "" + i;
						}
						
						File frame = new File(input.getAbsolutePath() + "/" + name + ".bmp");
						
						if (!frame.exists()) {
							System.out.println("SKIP:" + frame.getAbsolutePath());
							continue;
						}
						
						if (prevImage == null) {
							prevImage = new PixelRaster(ImageIO.read(frame));
							int[][] edges = filter.get_sobel_values(prevImage);
							referenceImages.add(prevImage);
							prevImgBlocks = makroBlockEngine.get_makroblocks_from_image(prevImage, edges, config.SUPER_BLOCK);
							outputWriter.bake_meta_data(prevImage, filesCount);
							outputWriter.bake_start_frame(prevImage);
							continue;
						}
						
						currentImage = new PixelRaster(ImageIO.read(frame));
						int[][] edges = filter.get_sobel_values(currentImage);
						filter.damp_frame_colors(prevImage, currentImage); //CurrentImage gets updated automatically
						f.set_previews(prevImage, currentImage);
						f.set_sobel_image(filter.get_sobel_image());
						
						ArrayList<YCbCrMakroBlock> curImgBlocks = makroBlockEngine.get_makroblocks_from_image(currentImage, edges, config.SUPER_BLOCK);
						Dimension dim = new Dimension(currentImage.getWidth(), currentImage.getHeight());
						f.set_MBDiv_image(outputWriter.draw_MB_outlines(dim, curImgBlocks));
						
						//This only adds an I-Frame if 'i' is a 50th frame and a change
						//detection lied 20 frames ahead or a change detection has triggered.
						boolean sceneChanged = scene.scene_change_detected(prevImage, currentImage);
						
						if (sceneChanged == true) {
							System.out.println("Shot change dectedted at frame " + i);
						}
						
						if ((i % 80 == 0 && changeDetectDistance > 10) || sceneChanged) {
							prevImgBlocks = makroDifferenceEngine.get_MakroBlock_difference(curImgBlocks, prevImage, currentImage);
							ArrayList<DCTObject> dct = makroBlockEngine.apply_DCT_on_blocks(prevImgBlocks);
							
							outputWriter.add_obj_to_queue(dct, null);
							referenceImages.clear();
							referenceImages.add(currentImage);
							prevImage = currentImage;
							prevImgBlocks = curImgBlocks;
							changeDetectDistance = sceneChanged == true ? 0 : changeDetectDistance;
							continue;
						}
						
						ArrayList<YCbCrMakroBlock> differences = makroDifferenceEngine.get_MakroBlock_difference(curImgBlocks, prevImage, currentImage);
						f.setDifferenceImage(differences, new Dimension(currentImage.getWidth(), currentImage.getHeight()));
						
						ArrayList<Vector> movementVectors = vectorEngine.calculate_movement_vectors(referenceImages, differences, f.get_vec_sad_tolerance());

						///////////////////
						//// DEBUGGING ////
						///////////////////
						int areaVecs = 0, areaDiffs = 0;
						
						for (Vector v : movementVectors) {
							areaVecs += Math.pow(v.getAppendedBlock().getSize(), 2);
						}
						
						for (YCbCrMakroBlock b : differences) {
							areaDiffs += Math.pow(b.getSize(), 2);
						}
						
						System.out.println("Vecs: " + movementVectors.size() + " (" + areaVecs + ") : " + differences.size() + " (" + areaDiffs + ")");
						
						f.setVectorizedImage(vectorEngine.construct_vector_path(dim, movementVectors));

						BufferedImage result = outputWriter.build_Frame(prevImage, differences, movementVectors, 3);
						
//						try {
//							ImageIO.write(outputWriter.build_Frame(prevImage, differences, movementVectors, 2), "png", new File(output.getAbsolutePath() + "/V_" + i + ".png"));
//							ImageIO.write(outputWriter.build_Frame(prevImage, differences, null, 1), "png", new File(output.getAbsolutePath() + "/D_" + i + ".png"));
//							ImageIO.write(result, "png", new File(output.getAbsolutePath() + "/R_" + i + ".png"));
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
						
						PixelRaster res = new PixelRaster(result);
						edges = filter.get_sobel_values(res);
						prevImgBlocks = makroBlockEngine.get_makroblocks_from_image(res, edges, config.SUPER_BLOCK);
						
						ArrayList<DCTObject> diffDCT = makroBlockEngine.apply_DCT_on_blocks(differences);
						
//						try {
//							ImageIO.write(outputWriter.reconstruct_DCT_image(diffDCT, prevImage.getWidth(), prevImage.getHeight()), "png", new File(output.getAbsolutePath() + "/D_R_" + i + ".png"));
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
						
						//Just for validation
						ArrayList<DCTObject> dcts = makroBlockEngine.apply_DCT_on_blocks(prevImgBlocks);
						result = outputWriter.reconstruct_DCT_image(dcts, result.getWidth(), result.getHeight());
						
//						try {
//							ImageIO.write(result, "png", new File(output.getAbsolutePath() + "/DCT_" + i + ".png"));
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
						
						outputWriter.add_obj_to_queue(diffDCT, movementVectors);
						
						referenceImages.add(res);
						release_old_reference_images(referenceImages);
						prevImage = res;
					}
					
					f.disposeWriterPermission();
					
					outputWriter.compress_result();
					f.update_encoder_frame_count(filesCount + (filesCount / 10), filesCount, true);
					
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
				
				Filter filter = new Filter();
				DataPipeEngine dataPipeEngine = new DataPipeEngine(grabber);
				DataPipeValveEngine dataPipeValveEngine = new DataPipeValveEngine("C:\\Users\\Lukas Lampl\\Documents");
				
				ArrayList<BufferedImage> referenceImages = new ArrayList<BufferedImage>(MAX_REFERENCES);
				
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
					
					try {
						ImageIO.write(result, "png", new File("C:\\Users\\Lukas Lampl\\Documents\\result\\UP_" + frameCounter + ".png"));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (vecs != null) {
						filter.apply_deblocking_filter(vecs, new PixelRaster(result));
					}
//					BufferedImage outputImg = filter.apply_gaussian_blur(result, 1);
					
					dataPipeValveEngine.release_image(result);
					prevFrame = result;
					referenceImages.add(result);
					
					if (referenceImages.size() > EntryPoint.MAX_REFERENCES) {
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
		if (refList.size() < EntryPoint.MAX_REFERENCES) {
			return;
		}
		
		refList.remove(0);
	}
}
