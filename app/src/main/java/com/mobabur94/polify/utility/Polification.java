package com.mobabur94.polify.utility;

import android.graphics.Bitmap;

import com.mobabur94.polify.geometry.Point;
import com.mobabur94.polify.geometry.Triangle;

import java.util.ArrayList;
import java.util.List;

public class Polification {

    public int complexity;
    public boolean stroke;

    public Bitmap original;
    public Bitmap altered;

    public int width;
    public int height;

    public int[] pixelsOriginal;
    public int[] pixelsAltered;

    public int radius;
    public int maxLimit;
    public int limit;

    public List<Point> points;
    public List<Triangle> triangles;

    public Polification(Bitmap photo, int complexity, boolean stroke) {
        // get the complexity, stroke, and the photo
        this.complexity = complexity;
        this.stroke = stroke;
        this.original = photo;

        // use an altered copy so we can retain color info for later
        this.altered = original.copy(original.getConfig(), true);

        // get width and height
        this.width = original.getWidth();
        this.height = original.getHeight();

        // initialize the pixel arrays
        pixelsOriginal = new int[width * height];
        pixelsAltered = new int[width * height];

        // fill in the pixel arrays
        original.getPixels(pixelsOriginal, 0, width, 0, 0, width, height);
        altered.getPixels(pixelsAltered, 0, width, 0, 0, width, height);

        // calculate values to use based on the complexity and the image size
        this.radius = (complexity < 0 || complexity > 9) ? 4 : 10 - complexity;
        this.maxLimit = Math.max(50, 2 * (int) Math.sqrt((altered.getWidth()) * (altered.getHeight())));
        this.limit = (int) ((complexity / 9.0) * (maxLimit - 50)) + 50;

        // initialize the list of points and the list of triangles
        this.points = new ArrayList<Point>();
        this.triangles = new ArrayList<Triangle>();
    }

    public Bitmap processPhoto() {
        // blur the image and make it grayscale
        gaussianGray();

        // detect the edges
        edgeDetect();

        // create points along the detected edges
        edgePoints();

        // add some random points to make it seem more natural
        morePoints();

        // triangulate the points into a delaunay triangulation
        triangulatePoints();

        // apply the altered pixels to the altered image
        altered.setPixels(pixelsAltered, 0, width, 0, 0, width, height);

        return altered;
    }

    private int luminance(int r, int g, int b) {
        return ((3 * r) + (4 * g) + b) >> 3;
    }

    private int luminance(int argb) {
        int[] parts = argb(argb);
        return luminance(parts[1], parts[2], parts[3]);
    }

    private int argb(int a, int r, int g, int b) {
        return ((a << 24) | (r << 16) | (g << 8) | b);
    }

    private int[] argb(int argb) {
        int[] result = new int[4];
        result[0] = (argb >> 24) & 0xff;
        result[1] = (argb >> 16) & 0xff;
        result[2] = (argb >>  8) & 0xff;
        result[3] = (argb      ) & 0xff;
        return result;
    }

    private void gaussianGray() {
        double[][] matrix = Matrix.blur[radius - 1];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (y * width) + x;

                double aSum = 0;
                double rSum = 0;
                double gSum = 0;
                double bSum = 0;

                for (int j = y - radius; j <= y + radius; j++) {
                    for (int i = x - radius; i <= x + radius; i++) {
                        if (
                            (j >= 0 && j < height) &&
                            (i >= 0 && i < width)
                        ) {
                            aSum += ((pixelsOriginal[index] >> 24) & 0xff) * matrix[j - y + radius][i - x + radius];
                            rSum += ((pixelsOriginal[index] >> 16) & 0xff) * matrix[j - y + radius][i - x + radius];
                            gSum += ((pixelsOriginal[index] >>  8) & 0xff) * matrix[j - y + radius][i - x + radius];
                            bSum += ((pixelsOriginal[index]      ) & 0xff) * matrix[j - y + radius][i - x + radius];
                        }
                    }
                }

                int a = (aSum > 0xff) ? 0xff : (aSum < 0x00) ? 0x00 : (int) aSum;
                int r = (rSum > 0xff) ? 0xff : (rSum < 0x00) ? 0x00 : (int) rSum;
                int g = (gSum > 0xff) ? 0xff : (gSum < 0x00) ? 0x00 : (int) gSum;
                int b = (bSum > 0xff) ? 0xff : (bSum < 0x00) ? 0x00 : (int) bSum;

//                int gray = luminance(r, g, b);

                pixelsAltered[index] = argb(a, r, g, b);
            }
        }
    }

    private void edgeDetect() {}

    private void edgePoints() {}

    private void morePoints() {}

    private void triangulatePoints() {}

}
