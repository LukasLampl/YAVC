> [!CAUTION]
> This repo is in its early stages and the project is in development as a school project.
> It does not have the intention to replace other video codecs, its just made to dive into the world of video compression.
> THE COMPRESSOR ONLY ACCEPTS `.bmp` FILES WITH THE FOLLOWING FORMAT: `"%4d.bmp"`

# YAVC - Yet Another Video Compressor #
This is the repo for a small schoolproject: A videocompression algorithm

# Table of contents #
1. Installing and running the project
2. UI navigation
3. How it works
4. Statistics
5. Contact
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

# 2. UI navigation #
The UI is now in a state, in which the user can use the application even without any instructions.  
It is relatively simple and the only major issues may occur at the different sliders, which I'll explain now.  
**Encode -> Start** -> This button is the entry to the encoding process.  
**Decode -> Start** -> This button starts the decoding process for a YAVC file.  
**Slider 1** -> This slider manages the color damping tolerance. (If the slider is at its highest the damping occures if 100% of the colors match)  
**Slider 2** -> This slider determines the maximum SAD a block can have before getting filtered. (The lower the higher the precision)

> [!TIP]
> Just play a little with the sliders until you get your desired results. Massive changes are followed by massive changes.
> The better the result, the worse the compression ration!

# 3. How it works #
I'll keep everything simple and try to explain it as good as possible.  
First of all all frames are converted from ```RGB / ARGB``` to ```YCbCr / YUV```. The conversion takes place, in order to exploit the human eye (Humans detect differences in luma better than chroma). Furthermore the colors are then subsampled from `4:4:4` to `4:2:0`, since you won't ever see all details in a ```4:4:4``` video.
  
<details>  
<summary>Reading the input</summary>  
  
To start compressing the compressor needs a source. For YAVC it's a folder filled with a bunch of raw frames to compress. The images have to be in ```%4d.bmp``` format to be processed. First the images are converted in "Pixel rasters" which are essentially just an array of integers holding the RGB values of the image.
</details>

<details>
<summary>Edges & Textures</summary>
  
Afterall this compressor works by exploiting redundancy, to avoid compressing smaller-fine details like the textures of a T-Shirt or leaves of a tree, YAVC consists of a texture and edge detection algorithm (Scharr-Operator / Sobel-Operator). The frame gets read in and the ```Sobel-Operator (Scharr-Operator)``` values are calculated. The higher the value the more "complexity" is in that area (by complexity I mean edges and textures or "area of interest").  

Finally the compressor has an 2D-array of integers, that match up with the input image. So if you'd pick the edge magnitude at position 54, 67 and compare to the original image, you'll see, that the edge magnitudes match up perfectly.
</details>

<details>
<summary>Color damping</summary>  
  
The intermediate step of color damping constists of scanning all pixels of the previous and current frame and compare them, if the delta values are in a specific threshold, the color of the current frame is damped to create as much redundancy as possible.  

```Threshold:``` _delta_Y > 3.0 && delta_Cb > 8.0 && delta_Cr > 8.0_  

To make it more clear, _the human eye is more sensitive to changes in contrast than in chroma_, so finding the "next best color" is not affecting the visuals.
</details>

<details>
<summary>Makroblock partitioning</summary>  
  
Followed by the texture and edge determination is the Makroblock partitioning. To achieve that the compressor uses the generated values of the ```"Edge & Texture"``` detection and puts ```"Areas of interest"```  at parts, that have a lot of textures and edges. This happens, since textures and edges should be as detailed as possible. The partitioning itself is by dividing a ```Superblock (32x32)``` to smaller Subblocks, if a certain details threshold is smaller than the actual detail in the block. Every Subblock has its own threshold, at which it divides again. The available blocksizes are: _32x32_, _16x16_, _8x8_ and _4x4_.  
  
> The threshold contains the variable ```size```, that stands for the block size. In addition to that the ```detail``` is normalized by the size.
    
| Blocksize | Threshold |
|-----------|-----------|
| 32x32 | size * 0.46 |
| 16x16 | size * 1.29 |
| 8x8 | size * 2.74 |
| 4x4 | N/A |
</details>

<details>
<summary>Scene change detection & I-Frames</summary>  
  
If the colors of a video are changing drastically, the motion estimation and block-matching might fail. In order to prevent that, the YAVC compressor constists of a scene change detector, that sets an ```I-Frame``` if a lot of color changing is happening. The I-Frames are placed all ```80 Frames```, when no scene detection has occured within ```10 frames``` prior.  
For the scene change detection the current frame is scanned for all colors it contains. Based on that ```3 histograms``` are created containing the color samples of the current frame. If the delta values of the histogram of the previous frame and the current frame are higher than ```1.0``` (adaptive; based on frame size) there might have been a scene change.  
If a scene change has occured a I-Frame is placed.  
The scene change detection has no guarantee to detect every shot change, it is especially good at finding ```hard cuts```.
</details>

<details>
<summary>Computing differences</summary>  
  
Since a video has a lot redundancy, YAVC filters the most obvious ones out by finding the differences. To do that the MakroBlocks from the current frame are compared to the MakroBlocks of the previus frame. If a change is detected that difference is marked as "difference", while the ones that are mostly the same are filtered out.  

> The MakroBlocks of the current frame are compared with the exact MakroBlock from the previous frame. Let's say the current MakroBlock has the follwing properties: Position = 35, 75; Size = 16,
> then the comparable MakroBlock would be MakroBlock at Position 35, 75 and Size 16 from the previous frame.
  
</details>

<details>
<summary>Computing motion</summary>

Motion estimation is a really crucial step in YAVC, because that's the main source of compression. For motion estimation the differences are read one by one and are tried to match with another block in the previous frame. For that YAVC uses ```Hexagonal-search```, which ensures a low time complexity and good matching. For the matching itself the indivisual SAD values of the predicted blocks are calculated and compared, the lower, the better. YAVC uses 7 reference frames, which means a motion vector can point up to 7 frames into the past.  
  
  ```SAD Formula```: _((delta_Y)³ + (delta_Cb)² + (delta_Cr)² + (delta_A)<sup>delta_A</sup>) / (colorSize)²_  

After getting the best match, a vector is calculated, that references to the reference frame and position.
</details>

<details>
<summary>DCT-II</summary>
  
The last step of the YAVC video compressor is the DCT-II (Discrete Cosine Transform). All remaining MakroBlocks, that were not encoded as a vector are split into ```4x4 MakroBlocks``` and their chroma is transformed using DCT, the luma remains untouched. After transforming the chroma is a double, which gets rounded (the actual compression in DCT). Now the YAVC compression is complete.
</details>

<details>
<summary>Codec</summary>
  
To store the file and read out of it again a file codec is necessary. For that the files are encoded in ```UTF-8```.  
For Seperation of information the YAVC compressor creates a file for every frame, the start frame and meta data.  
In order for the vectors and DCT-II coefficients to be stored there is a strict notation form.

| Reserved HEX code | Reserved Binary | Meaning |
|-------------------|-----------------|---------|
| 0x01 | 00000000 00000001 | Start of movement vectors |
| 0x02 | 00000000 00000010 | Start of DCT-II Coefficients |
| 0x03 | 00000000 00000011 | End of Y - Coefficients |
| 0x04 | 00000000 00000100 | End of Cb - Coefficients |
| 0x05 | 00000000 00000101 | End of Cr - Coefficients |
| 0x06 | 00000000 00000110 | Newline of Coefficient matrix |
| 0x07 | 00000000 00000111 | DCT-II matrix end |
  
There's no special order in which the different data has to appear, the only restriction is, that a datapack (DCT or vectors) have to be after the indicator and can't be mixed up.  

### DCT-II ###
After the DCT-II inidicator 0x02 the matrices in the differences that remained are placed. For that each coefficient has its own 2 bytes. To prevent a number from going into the reserved area, an offset is added. If the number is negative, the 14<sup>th</sup> is flipped to a 1 (1 << 14). The number is just written with the following syntax:  

```
If (number is negative) then
  b = ((1 << 14) | ((number + offset) & 0xFFF))
Else
  b = ((number + offset) & 0xFFF)
```
   
After each row in the coefficient matrix a _new line inidicator (0x06)_ is placed. The chroma values are processed in that scheme too. Furthermore the position of that particular matrix is written with 4 bytes behind the actual matrix. At the end of each matrix a _matrix end inidicator (0x07)_ is placed.  
To keep is short, this is the syntax:  

```
{Y-Matrix} -> 0x03 -> {Cb-Matrix} -> 0x04 -> {Cr-Matrix} -> 0x05 -> {Position} -> 0x07
```
  
And a matrix for instance:  

```
0x3a 0xbb 0x20 0xee -> 0x06 -> 0x45 0x0e 0x01 0x2c -> 0x34 -> ect.
```

### Vectors ###
The start of the vectors is marked with 0x01. The vectors only contain the following information: Start position, SpanX, SpanY, Reference, Size. Here's a table with the max values of the properties:  

| Start Position X | Start Position Y | Span X | Span Y | Reference | Size |
|------------------|------------------|--------|--------|-----------|------|
| 65491 | 65491 | 64 | 64 | 10 | 32 |

By that you can see what types need more memory and which can be summed up. The position X and Y get their own 2 bytes (+ offset). Also the span X and span Y get their own, since they can get into the negative values (calculated as equal as the negative DCT-II coefficients). Only the reference and size are small enough to store in 2 bytes. For that YAVC does the following bitshifting:  

```
b = ((((reference & 0xFF) << 8) | (reference & 0xFF)) + offset)
```

Now every vector has exactly 10 bytes storing all the necessarry information.  

### Layout ###
The normal layout is pretty forward, first all Coefficients of the DCT-II then followed by all movement vectors.
</details>

# 4. Statistics #
> [!Caution]
> Until now the whole program is still lacking the "Deblocking filter", so the statistics are based of Color damping, Color reduction, Edge & Difference detection, DCT-II and Blockmatching (5 reference Frames)  

All the frames are measured by their "original" size, which means in `.bmp` format. Moreover all settings are the standard values.  
  
|  Video name  | Original size | YAVC size (old) | YAVC size (new) | Decrease (old) | Decrease (new) | Resolution | Frames | FPS |
|--------------|---------------|-----------------|-----------------|----------------|----------------|------------|--------|-----|
| Big Buck Bunny | 348 MB | 34.4 MB | 21.8 MB | 90.1 % | 93.7 % | 1280x720 | 132 | 25 |
| Sunset | 3.89 GB | 0.99 GB | 291 MB | 74.5 % | 92.5 % | 1280x720 | 1512 | 25 |
| Road in city | 0,99 GB | 408 MB | 99.4 MB | 58.8 % | 90.0 % | 1920x1080 | 171 | 30 |
| Watering a flower | 495 MB | 53.8 MB | 20.4 MB | 89.1 % | 95.8 % | 640x360 | 752 | 25 |

# 4. Contact: #  
**E-Mail:** lampl.lukas@outlook.com
