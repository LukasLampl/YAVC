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

package Decoder;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class DataPipeValveEngine {
	private File output = null;
	
	public DataPipeValveEngine(String path) {
		this.output = new File(path + "/result");
		this.output.mkdir();
	}
	
	private int frameCounter = 0;
	
	public void release_image(BufferedImage img) {
		try {
			ImageIO.write(img, "png", new File(output.getAbsolutePath() + "/" + (this.frameCounter++) + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
