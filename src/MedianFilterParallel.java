/**
 * This class is a parallel program to change all of the pixels of an image to the median value of the pixels in the grid around it.
 * It uses the ForkJoin method of parallelisation.
 *
 * @author Tayla Rogers
 * @since 04-08-2022
 */

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.Arrays;

public class MedianFilterParallel extends RecursiveAction
{
    public int width;
    public int height;
    public int start;
    public int[][] windowArr;
    public static int window;
    public static BufferedImage outputImage;
    public static int[][] pixels;
    public static final int THRESHOLD = 300;

    /**
     * This is a constructor to set the values 
     */
    public MedianFilterParallel(int w, int h, int x)
    {
        width = w;
        height = h;
        start = x;
        windowArr = new int[window][window];
    }

    /**
     * This is the main method that will read the input file in and output the new file.
     */
    public static void main(String[] args)
    {
        // Get file names of origin file and what to write it to
        String inputName = args[0];
        String outputName = args[1];

        // Get window size
        window = Integer.parseInt(args[2]);

        if ((window < 1) || (window % 2 == 0))
        {
            System.out.println("Invalid window value - your window size needs to be a positive, odd integer.");
            System.exit(0);
        }

        try
        {
            // Load input image
            File inputFile = new File(inputName);
            BufferedImage inputImage = ImageIO.read(inputFile);
            System.out.println("Image has been read into program.");

            // Manipulate image
            mean(inputImage);
        }
        catch (Exception e)
        {
            System.out.println("There was an error during the processing of the pixels.");
            System.exit(0);
        }

        try 
        {
            // Load output image
            File outputFile = new File(outputName);
            ImageIO.write(outputImage, "jpg", outputFile);
            System.out.println("Image has been written to a new file.");
        }
        catch (Exception e)
        {
            System.out.println("There was an error during writing to a new file.");
            System.exit(0);
        }
    }

    /**
     * This method changes a pixel's RGB values to the average of its surrounding values.
     */
    public static void mean(BufferedImage inputImage)
    {
        // Set necessary values
        int w = inputImage.getWidth();
        int h = inputImage.getHeight();

        pixels = new int[w][h];

        // Load RGB values
        loadInput(inputImage);

        // Create objects
        MedianFilterParallel filter = new MedianFilterParallel(w, h, 0);
        ForkJoinPool pool = new ForkJoinPool();

        // Start process and time
        long startTime = System.currentTimeMillis();
        pool.invoke(filter);
        long endTime = System.currentTimeMillis();

        System.out.println("Pixels edited.");

        // Create output image and load pixel values
        outputImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);

        loadOutput(inputImage);

        System.out.println("MedianFilterParallel took " + (endTime - startTime) + 
                " milliseconds.");
    }

    /**
     * This method calculates the average RGB value of the pixel
     */
    public int calculate(int middle)
    {
        int[] reds = new int[window*window];
        int[] greens = new int[window*window];
        int[] blues = new int[window*window];

        int num = 0;

        // Add total RGB values in array
        for (int i = 0; i < window; i++)
        {
            for (int j = 0; j < window; j++)
            {
                reds[num] = ((windowArr[i][j]>>16) & 0xff);
                greens[num] = ((windowArr[i][j]>>8) & 0xff);
                blues[num] = (windowArr[i][j] & 0xff);

                num++;
            }
        }

        // Sort arrays
        Arrays.sort(reds);
        Arrays.sort(greens);
        Arrays.sort(blues);

        // Set the median RGB values
        int middleNum = (int)(Math.floor((window*window) / 2));

        int alphaValue =  (windowArr[middle][middle]>>24) & 0xff;
        int redValue = reds[middleNum];
        int greenValue = greens[middleNum];
        int blueValue = blues[middleNum];

        // Set into pixel value
        int pixelValue = (alphaValue<<24) | (redValue<<16) | (greenValue<<8) | blueValue;

        return pixelValue;
    }

    /**
     * This method runs on each thread that is created to decide whether to run it or split it
     */
    protected void compute()
    {
        if (width < THRESHOLD)
        {
            // Loop through pixel values
            for (int i = start; i < (start + width - window); i++)
            {
                for (int j = 0; j < height - window; j++)
                {  
                    // Load window into array
                    for (int k = 0; k < window; k++)
                    {
                        for (int m = 0; m < window; m++)
                        {
                            windowArr[k][m] = pixels[i+k][j+m];
                        }
                    }

                    // Change pixel value
                    int middle = (int)Math.floor(window/2);
                    pixels[i+middle][j+middle] = calculate(middle);
                }
            }
        }
        else
        {
            // Find split value
            int split = (int)(width / 2);

            // Split the work
            int leftWidth = split + 3;
            int rightWidth = width - (split+1) + (window-1) + 3;

            MedianFilterParallel left = new MedianFilterParallel(leftWidth, height, start + 0);
            MedianFilterParallel right = new MedianFilterParallel(rightWidth, height, start + (split - window)-1);

            left.fork();
            right.compute();
            left.join();
        }
    }

    /**
     * This method loads the input images pixels into an array.
     */
    public static void loadInput(BufferedImage inputImage)
    {
        int w = inputImage.getWidth();
        int h = inputImage.getHeight();

        for (int i = 0; i < w; i++)
        {
            for (int j = 0; j < h; j++)
            {
                pixels[i][j] = inputImage.getRGB(i,j);
            }
        }

        System.out.println("Input loaded.");
    }

    /**
     * This method loads the output images pixels 
     */
    public static void loadOutput(BufferedImage inputImage)
    {
        int w = inputImage.getWidth();
        int h = inputImage.getHeight();

        for (int i = 0; i < w; i++)
        {
            for (int j = 0; j < h; j++)
            {
                outputImage.setRGB(i,j,pixels[i][j]);
            }
        }

        System.out.println("Output loaded.");
    }
}