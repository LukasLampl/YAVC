package UI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Sidebar extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private Frame FRAME = null;
	
	public Sidebar(EncodePanel encodePanel, DecodePanel decodePanel, Frame frame) {
		this.FRAME = frame;
		
		setBackground(ComponentColor.SHADE_COLOR);
		setLayout(new GridBagLayout());
		setSize(new Dimension(180, Integer.MAX_VALUE));
		setPreferredSize(getSize());
		setMinimumSize(getSize());
		setMaximumSize(getSize());
		setAlignmentX(JPanel.LEFT_ALIGNMENT);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.anchor = GridBagConstraints.NORTHWEST;
		cons.weightx = 1.0;
		cons.weighty = 0.0;
		cons.gridy = 0;
		cons.gridx = 0;
		
		JLabel title = new JLabel("YAVC");
		title.setFont(new Font("Arial", Font.BOLD, 36));
		title.setBackground(ComponentColor.SHADE_COLOR);
		title.setForeground(ComponentColor.TEXT_COLOR);
		title.setBorder(BorderFactory.createEmptyBorder(7, 15, 25, 0));
		
		JButton encodeTabBtn = create_std_button("Encode");
		JButton decodeTabBtn = create_std_button("Decode");
		
		encodeTabBtn.setFocusPainted(false);
		encodeTabBtn.setOpaque(true);
		
		encodeTabBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				encodeTabBtn.setBackground(ComponentColor.TONER_COLOR);
				encodeTabBtn.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(10, 0, 10, 0),
					BorderFactory.createMatteBorder(0, 5, 0, 0, ComponentColor.HIGHLIGHT_SHADE_COLOR))
				);
				
				decodeTabBtn.setBackground(ComponentColor.SHADE_COLOR);
				decodeTabBtn.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
				
				encodeTabBtn.repaint();
				encodeTabBtn.revalidate();
				
				FRAME.move_focused_panel(encodePanel);
			}
		});
		
		decodeTabBtn.setFocusPainted(false);
		decodeTabBtn.setOpaque(true);
		
		decodeTabBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				decodeTabBtn.setBackground(ComponentColor.TONER_COLOR);
				decodeTabBtn.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(10, 0, 10, 0),
					BorderFactory.createMatteBorder(0, 5, 0, 0, ComponentColor.HIGHLIGHT_SHADE_COLOR))
				);
				
				encodeTabBtn.setBackground(ComponentColor.SHADE_COLOR);
				encodeTabBtn.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
				
				decodeTabBtn.repaint();
				decodeTabBtn.revalidate();
				
				FRAME.move_focused_panel(decodePanel);
			}
		});
		
		add(title, cons);
		cons.gridy++;
		cons.weighty = 0;
		add(encodeTabBtn, cons);
		cons.gridy++;
		cons.weighty = 1.0;
		add(decodeTabBtn, cons);
	}
	
	private JButton create_std_button(String text) {
		JButton btn = new JButton(text);
		btn.setForeground(ComponentColor.TEXT_COLOR);
		btn.setBackground(ComponentColor.SHADE_COLOR);
		btn.setFont(new Font("Arial", Font.PLAIN, 20));
		btn.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
		
		return btn;
	}
}
