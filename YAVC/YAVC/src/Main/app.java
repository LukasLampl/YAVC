package Main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import Decoder.DataGrabber;
import Decoder.DataPipeEngine;
import Decoder.DataPipeValveEngine;
import Encoder.Vector;
import UI.Frame;

public class app {
	//Create new Frame (contains compression preview)
	public static Frame f = new Frame();
	
	public static void main(String [] args) {
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

		f.setSize(700, 600);
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
		grabber.slice(file);
		
		System.out.println("Finished unzipping...");
		
		DataPipeEngine dataPipeEngine = new DataPipeEngine(grabber);
		DataPipeValveEngine dataPipeValveEngine = new DataPipeValveEngine("C:\\Users\\Lukas Lampl\\Documents");
		
		int frameCounter = 0;
		BufferedImage prevFrame = null;
		BufferedImage currFrame = null;
		
		while (dataPipeEngine.hasNext(frameCounter)) {
			if (prevFrame == null) {
				prevFrame = dataPipeEngine.scrape_main_image(grabber.get_start_frame());
				dataPipeValveEngine.release_image(prevFrame);
				continue;
			}
			
			currFrame = dataPipeEngine.scrape_next_frame(frameCounter);
			
			ArrayList<Vector> vecs = dataPipeEngine.scrape_vectors(frameCounter++);
			BufferedImage result = dataPipeEngine.build_frame(vecs, prevFrame, currFrame);
			dataPipeValveEngine.release_image(result);
			prevFrame = result;
		}
		
		System.out.println("Generated base..");
	}
}
