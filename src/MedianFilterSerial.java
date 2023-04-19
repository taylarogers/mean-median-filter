/**
 * This class is a sequential program to change all of the pixels of an image to the median  value of the pixels in the grid around it.
 *
 * @author Tayla Rogers
 * @since 04-08-2022
 */

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Arrays;

public class MedianFilterSerial
{
    private static int width;
    private static int height;
    private static int window;
    private static BufferedImage outputImage;
    private static int[][] windowArr;
    private static int[][] pixels;

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

            // Set necessary values
            width = inputImage.getWidth();
            height = inputImage.getHeight();

            pixels = new int[width][height];

            // Manipulate image
            median(inputImage);

            // Load output image
            File outputFile = new File(outputName);
            ImageIO.write(outputImage, "jpg", outputFile);
            System.out.println("Image has been written to a new file.");
        }
        catch (Exception e)
        {
            System.out.println("There was an error during processing.");
            System.exit(0);
        }
    }

    /**
     * This method changes a pixel's RGB values to the average of its surrounding values.
     */
    public static void median(BufferedImage inputImage)
    {
        // Load RGB values
        loadInput(inputImage);

        windowArr = new int[window][window];

        long startTime = System.currentTimeMillis();

        // Loop through pixel values
        for (int i = 0; i < width - window; i++)
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

        long endTime = System.currentTimeMillis();

        System.out.println("Pixels edited.");

        System.out.println("MeanFilterSerial took " + (endTime - startTime) + 
                " milliseconds.");

        // Create output image and load pixel values
        outputImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        loadOutput();
    }

    /**
     * This method calculates the median RGB value of the pixel
     */
    public static int calculate(int middle)
    {
        int[] reds = new int[window*window];
        int[] greens = new int[window*window];
        int[] blues = new int[window*window];

        int num = 0;

        // Put values into arrays
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
     * This method loads the input images pixels into an array.
     */
    public static void loadInput(BufferedImage inputImage)
    {
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                pixels[i][j] = inputImage.getRGB(i,j);
            }
        }

        System.out.println("Input loaded.");
    }

    /**
     * This method loads the output images pixels 
     */
    public static void loadOutput()
    {
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                outputImage.setRGB(i,j,pixels[i][j]);
            }
        }

        System.out.println("Output loaded.");
    }
}