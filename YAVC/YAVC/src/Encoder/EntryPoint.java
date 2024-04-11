package Encoder;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import UI.Frame;

public class EntryPoint {
	private static int MAX_REFERENCES = 5;
	private Status STATUS = Status.STOPPED;
	
	public boolean start_encode(Frame f) {
		this.STATUS = Status.RUNNING;
		
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
					
					ArrayList<BufferedImage> referenceImages = new ArrayList<BufferedImage>(MAX_REFERENCES);
					ArrayList<MakroBlock> prevImgBlocks = null;
					
					BufferedImage prevImage = null;
					BufferedImage currentImage = null;
					
					MakroBlockEngine makroBlockEngine = new MakroBlockEngine();
					MakroDifferenceEngine makroDifferenceEngine = new MakroDifferenceEngine();
					VectorEngine vectorEngine = new VectorEngine();
					OutputWriter outputWriter = new OutputWriter(output.getAbsolutePath(), f);
					
					int filesCount = input.listFiles().length;
					
					for (int i = 0; i < filesCount && this.STATUS == Status.RUNNING; i++) {
						f.updateFrameCount(i, filesCount, false);
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
							prevImage = ImageIO.read(frame);
							referenceImages.add(prevImage);
							outputWriter.bake_meta_data(prevImage, filesCount);
							outputWriter.bake_start_frame(prevImage);
							continue;
						}
						
						currentImage = ImageIO.read(frame);
						f.setPreviews(prevImage, currentImage);
						
						if (prevImgBlocks == null) {
							prevImgBlocks = makroBlockEngine.get_makroblocks_from_image(prevImage);
						}
						
						ArrayList<MakroBlock> curImgBlocks = makroBlockEngine.get_makroblocks_from_image(currentImage);
						curImgBlocks = makroBlockEngine.damp_MakroBlock_colors(prevImgBlocks, curImgBlocks, currentImage, f.get_damping_tolerance(), f.get_edge_tolerance());
						ArrayList<MakroBlock> rawDifferences = makroDifferenceEngine.get_MakroBlock_difference(prevImgBlocks, curImgBlocks, currentImage);
						ArrayList<YCbCrMakroBlock> differences = makroBlockEngine.convert_MakroBlocks_to_YCbCrMarkoBlocks(rawDifferences);
						f.setDifferenceImage(rawDifferences, new Dimension(currentImage.getWidth(), currentImage.getHeight()));
		
						ArrayList<Vector> movementVectors = vectorEngine.calculate_movement_vectors(referenceImages, differences, f.get_vec_sad_tolerance());
		
						Dimension dim = new Dimension(currentImage.getWidth(), currentImage.getHeight());
						f.setVectorizedImage(vectorEngine.construct_vector_path(dim, movementVectors));
						
						try {
							ImageIO.write(vectorEngine.construct_vector_path(dim, movementVectors), "png", new File(output.getAbsolutePath() + "/V_" + i + ".png"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						outputWriter.build_Frame(currentImage, differences, movementVectors, output, 3);
						outputWriter.add_obj_to_queue(differences, movementVectors);
						
						referenceImages.add(currentImage);
						release_old_reference_images(referenceImages);
						prevImage = currentImage;
						prevImgBlocks = curImgBlocks;
					}
					
					f.disposeWriterPermission();
					
					outputWriter.compress_result();
					f.updateFrameCount(filesCount + 10, filesCount, true);
					
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
	
	public void stop_process() {
		this.STATUS = Status.STOPPED;
	}
	
	private static void release_old_reference_images(ArrayList<BufferedImage> refList) {
		if (refList.size() < MAX_REFERENCES) {
			return;
		}
		
		refList.remove(0);
	}
}
