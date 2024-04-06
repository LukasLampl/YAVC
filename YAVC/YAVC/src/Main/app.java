package Main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import Decoder.DataGrabber;
import Decoder.DataPipeEngine;
import Decoder.DataPipeValveEngine;
import Encoder.MakroBlock;
import Encoder.MakroBlockEngine;
import Encoder.MakroDifferenceEngine;
import Encoder.OutputWriter;
import Encoder.Vector;
import Encoder.VectorEngine;
import Encoder.YUVMakroBlock;
import UI.Frame;

public class app {
	//Create new Frame (contains compression preview)
	public static Frame f = new Frame(new Dimension(1500, 900));
	
	public static void main(String [] args) {
		JButton encodeBtn = new JButton("Encode");
		encodeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser chooser = new JFileChooser("Choose a destination");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.showOpenDialog(null);
					
					File input = chooser.getSelectedFile();
					
					chooser.showOpenDialog(null);
					File output = chooser.getSelectedFile();
					
					if (input != null && output != null) {
						Thread worker = new Thread(() -> {
							encode(input, output);
						});
						
						worker.start();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JButton decodeBtn = new JButton("Decode");
		decodeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser chooser = new JFileChooser("Choose a \"yavc\" file");
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					chooser.showOpenDialog(null);
					
					File file = chooser.getSelectedFile();
					
					if (file != null) {
						Thread worker = new Thread(() -> {
							decode(file);
						});
						
						worker.start();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		f.setEncodeBtn(encodeBtn);
		f.setDecodeBtn(decodeBtn);
		f.setSize(200, 100);
		f.setVisible(true);
	}
	
	/*
	 * Purpose: Function starting the decoding process of an YAVC file
	 * Return Type: void
	 * Params: File file => YAVC File to be decoded
	 */
	private static void decode(File file) {
		System.out.println("Start unzipping...");
		DataGrabber grabber = new DataGrabber();
		String content = grabber.slice(file);
		System.out.println("Finished unzipping...");
		DataPipeEngine dataPipeEngine = new DataPipeEngine(content);
		DataPipeValveEngine dataPipeValveEngine = new DataPipeValveEngine("C:\\Users\\Lukas Lampl\\Documents");
		
		int frameCounter = 0;
		BufferedImage prevFrame = null;
		BufferedImage currFrame = null;
		
		while (dataPipeEngine.hasNext(frameCounter)) {
			if (prevFrame == null) {
				prevFrame = dataPipeEngine.scrape_main_image();
				dataPipeValveEngine.release_image(prevFrame);
				continue;
			}
			
			dataPipeEngine.jump_to_frame_number(frameCounter);
			currFrame = dataPipeEngine.scrape_next_frame();
			
			ArrayList<Vector> vecs = dataPipeEngine.scrape_vectors(frameCounter++);
			BufferedImage result = dataPipeEngine.build_frame(vecs, prevFrame, currFrame);
			dataPipeValveEngine.release_image(result);
			prevFrame = result;
		}
		
		System.out.println("Generated base..");
	}
	
	/*
	 * Purpose: Function starting the encoding process of a folder filled with image files in the ".bmp" format
	 * Return Type: void
	 * Params: File file => Folder location
	 */
	private static void encode(File input, File output) {
		long timeStart = System.currentTimeMillis();
		f.setProgress(0);

		try {
			BufferedImage prevImage = null;
			BufferedImage currentImage = null;
			
			MakroBlockEngine makroBlockEngine = new MakroBlockEngine();
			MakroDifferenceEngine makroDifferenceEngine = new MakroDifferenceEngine();
			VectorEngine vectorEngine = new VectorEngine();
			OutputWriter outputWriter = new OutputWriter(output.getAbsolutePath());
			
			int filesCount = input.listFiles().length;
			
			for (int i = 0; i < filesCount; i++) {
				f.updateFrameCount(i, filesCount);
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
					outputWriter.bake_meta_data(prevImage, filesCount);
					outputWriter.bake_start_frame(prevImage);
					continue;
				}
				
				currentImage = ImageIO.read(frame);
				f.setPreviews(prevImage, currentImage);
				
				ArrayList<MakroBlock> prevImgBlocks = makroBlockEngine.get_makroblocks_from_image(prevImage);
				ArrayList<YUVMakroBlock> prevImgYUVBlocks = makroBlockEngine.convert_MakroBlocks_to_YUVMarkoBlocks(prevImgBlocks);
				
				ArrayList<MakroBlock> curImgBlocks = makroBlockEngine.get_makroblocks_from_image(currentImage);
				curImgBlocks = makroBlockEngine.damp_MakroBlock_colors(prevImgBlocks, curImgBlocks, currentImage, f.get_damping_tolerance(), f.get_edge_tolerance());
				ArrayList<MakroBlock> differences = makroDifferenceEngine.get_MakroBlock_difference(prevImgBlocks, curImgBlocks, currentImage);
				f.setDifferenceImage(differences, new Dimension(currentImage.getWidth(), currentImage.getHeight()));

				ArrayList<Vector> movementVectors = vectorEngine.calculate_movement_vectors(prevImage, differences);
				f.setVectorizedImage(differences, new Dimension(currentImage.getWidth(), currentImage.getHeight()));
				
				ArrayList<YUVMakroBlock> curImgYUVBlocks = makroBlockEngine.convert_MakroBlocks_to_YUVMarkoBlocks(curImgBlocks);
				
				outputWriter.bake_frame(differences);
				outputWriter.bake_vectors(movementVectors, i);
				outputWriter.build_Frame(prevImage, differences, movementVectors, output);
				prevImage = currentImage;
				double per = (((double)(i + 1) / (double)input.listFiles().length) * 100);
				f.setProgress((int)Math.round(per));
			}
			
			f.setProgress(90);
			outputWriter.compress_result();
			f.setProgress(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long timeEnd = System.currentTimeMillis();
		System.out.println("Time: " + (timeEnd - timeStart) + "ms");
	}
}
