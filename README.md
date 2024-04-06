> [!CAUTION]
> This repo is in its early stages and the project is in development as a school project.
> It does not have the intention to replace other video codecs, its just made to dive into the world of video compression.
> THE COMPRESSOR ONLY ACCEPTS `.bmp` FILES WITH THE FOLLOWING FORMAT: "%4d.bmp"
> DUE TO THE TIME THERE IS NO BMEA IMPLEMENTED!!!

# YAVC - Yet Another Video Compressor #
This is the repo for a small schoolproject: A videocompression algorithm

# Table of contents #
1. Installing and running the project
2. UI navigation
3. How it works
4. Contact
</ol>

# 1. Installing and running the project #
The whole project is writte in plain `Java` and `Eclipse IDE`, so it is recommended to use the `Eclipse IDE` as the editor.

## Installing ##
To install the repo you have multiple options:

### Option 1: ###
**1.1** Download the repo as `ZIP` for instance.  
**1.2** Extract the files out of the ZIP-File.  
**1.3** Now open Eclipse IDE and Head over to `File -> Import -> General -> Filesystem` and select the extracted folder. You're also able to import it as a project.  

### Option 2: ###
**2.1** Open Eclipse IDE.  
**2.2** Configure `EGit` in Eclipse.  
**2.3** Now right click the `Package Explorer` and head to `Team -> Pull`.  

### Running YAVC ###
To run YAVC just press the provided `Run button` in Eclipse IDE or use the shortcut `F5`.  

> [!NOTE]
> Due to its early stage and implementation the compressor runs multithreaded and ending the UI does NOT terminate the compression thread.

# 3. UI navigation #
The UI is relatively simple and the only major issues may occur at the different sliders, which I'll explain in a bit.  
**Encode** -> This button is the entry to the encoding process.  
**Decode** -> This button starts the decoding process for a YAVC file.  
**Slider 1** -> This slider defines the edge recognition damping. (The lower the tolerance to precise the compression)  
**Slider 2** -> This slider manages the color damping tolerance. (If the slider is at its highest the damping occures if 100% of the colors match)  
**Slider 3** -> This slider is for edge detection in the vector creation process. (The higher the higher the precision)  
**Slider 4** -> This slider manages the gradient filtering. (The lower, the better the gradient matches in the vectorization)  

> [!TIP]
> Just play a little with the sliders until you get your desired results. Massive changes are followed by massive changes.

# How it works #
Since I want to keep everything simple, I do not go into theoretical and high detail.
First of all the program takes two images from the input folder and loads them into memory. Now both images are converted into 8x8 MakroBlocks, if a MakroBlock is bigger than the image, the unused memory is set to a specific number for later recognition. Followed by the MakroBlock conversion is the color-damping part, in which the
MakroBlocks of the same position are compared to each other and damped if necessary to create as much redundancy as possible. No the differences between those MakroBlocks are calculated, meaning if the frames have a static background, the difference is a moving object for instance. After the differences are computed, the
differences are tried to match up with MakroBlock from the previous frame, to save space (Since the block just has to be defined once) (NOTE: THIS IS NOT A BMEA!!!). Now the vectorized MakroBlocks are written to a file. The process repeats for all frames. If all frames have been processed the final file is deflated
using ZIP.

# 4. Contact: #  
**E-Mail:** lampl.lukas@outlook.com
