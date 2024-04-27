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
	
	/*
	 * Purpose: Set up the sidebar of the frame
	 * Return Type: void
	 * Params: EncodePanel encodePanel => Used EncodePanel;
	 * 			DecodePanel decodePanel => Used DecodePanel;
	 * 			Frame frame => Frame in which the component is active
	 */
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
	
	/*
	 * Purpose: Create a button, that can be used multiple times
	 * Return Type: JButton => STD Button
	 * Params: String text => Text of the button
	 */
	private JButton create_std_button(String text) {
		JButton btn = new JButton(text);
		btn.setForeground(ComponentColor.TEXT_COLOR);
		btn.setBackground(ComponentColor.SHADE_COLOR);
		btn.setFont(new Font("Arial", Font.PLAIN, 20));
		btn.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
		
		return btn;
	}
}
