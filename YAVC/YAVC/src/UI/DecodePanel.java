package UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import Main.EntryPoint;

public class DecodePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JLabel frameCountLabel = new JLabel("0/0 Frames");
	private JProgressBar bar = new JProgressBar();
	
	private ToggleButton startBtn = new ToggleButton("Start");
	private EntryPoint entryPoint = null;
	
	public DecodePanel(Frame frame, EntryPoint entryPoint) {
		this.entryPoint = entryPoint;
		setBackground(ComponentColor.DEFAULT_COLOR);
		setLayout(new BorderLayout());
		
		add(get_control_panel(frame), BorderLayout.SOUTH);
	}
	
	private JPanel get_control_panel(Frame frame) {
		JPanel holder = new JPanel();
		holder.setLayout(new FlowLayout());
		holder.setBackground(ComponentColor.DEFAULT_COLOR);
		
		JPanel box = new JPanel();
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
		box.setBackground(ComponentColor.DEFAULT_COLOR);
		box.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
		
		this.frameCountLabel.setBackground(ComponentColor.DEFAULT_COLOR);
		this.frameCountLabel.setForeground(ComponentColor.TEXT_COLOR);
		this.frameCountLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		
		this.bar.setMinimum(0);
		this.bar.setValue(0);
		this.bar.setMaximum(100);
		this.bar.setStringPainted(true);
		this.bar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
		this.bar.setBackground(ComponentColor.SHADE_COLOR);
		this.bar.setForeground(ComponentColor.HIGHLIGHT_SHADE_COLOR);
		this.bar.setBorderPainted(false);
		
		box.add(this.frameCountLabel);
		box.add(this.bar);
		
		this.startBtn.setContentAreaFilled(false);
		this.startBtn.setFont(new Font("Arial", Font.PLAIN, 16));
		this.startBtn.setBorderPainted(false);
		this.startBtn.setBackground(ComponentColor.SUB_COLOR);
		
		this.startBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startBtn.setClicked(!startBtn.isClicked());
				
				if (startBtn.isClicked() == false) {
					startBtn.setText("Stop");
					startBtn.setBackground(ComponentColor.STO_COLOR);
					boolean succ = entryPoint.start_decoding_process(frame);
					
					if (succ == true) {
						return;
					}
				}
				
				reset_start_btn();
				entryPoint.stop_decoding_process();
			}
		});
		
		holder.add(box);
		holder.add(this.startBtn);
		
		return holder;
	}
	
	public void reset_start_btn() {
		this.startBtn.setBackground(ComponentColor.SUB_COLOR);
		this.startBtn.setText("Start");
	}
	
	public void set_frame_stats(int frame, int maxFrames, boolean percentOnly) {
		if (percentOnly == false) {
			this.frameCountLabel.setText(frame + "/" + maxFrames + " Frames");
		}
		
		double per = ((double)(frame + 1) / (double)maxFrames) * 100;
		this.bar.setValue((int)Math.round(per));
	}
}
