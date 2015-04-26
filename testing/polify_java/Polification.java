import java.util.Comparator;
import java.util.Collections;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.lang.Math;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

public class Polification {

    private int watch;
    private int limit;
    private int radius;
    private List<Point> points;
    private Color background;
    private BufferedImage image;
    private BufferedImage original;
    private int[] pixels;
    private int[] pixelsOriginal;
    private int[][][] sobelMatrix = {
        {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        },
        {
            { 1,  2,  1},
            { 0,  0,  0},
            {-1, -2, -1}
        }
    };
    private double[][][] blurMatrix = {
        {
            {0.00987648, 0.0796275, 0.00987648},
            {0.0796275,  0.641984,  0.0796275},
            {0.00987648, 0.0796275, 0.00987648}
        },
        {
            {0.002589,  0.0107788, 0.0241466, 0.0107788, 0.002589},
            {0.0107788, 0.0448755, 0.10053,   0.0448755, 0.0107788},
            {0.0241466, 0.10053,   0.225206,  0.10053,   0.0241466},
            {0.0107788, 0.0448755, 0.10053,   0.0448755, 0.0107788},
            {0.002589,  0.0107788, 0.0241466, 0.0107788, 0.002589}
        },
        {
            {0.00121055, 0.00354909, 0.00752005, 0.0102336, 0.00752005, 0.00354909, 0.00121055},
            {0.00354909, 0.0104052,  0.0220473,  0.0300028, 0.0220473,  0.0104052,  0.00354909},
            {0.00752005, 0.0220473,  0.0467152,  0.0635719, 0.0467152,  0.0220473,  0.00752005},
            {0.0102336,  0.0300028,  0.0635719,  0.0865112, 0.0635719,  0.0300028,  0.0102336},
            {0.00752005, 0.0220473,  0.0467152,  0.0635719, 0.0467152,  0.0220473,  0.00752005},
            {0.00354909, 0.0104052,  0.0220473,  0.0300028, 0.0220473,  0.0104052,  0.00354909},
            {0.00121055, 0.00354909, 0.00752005, 0.0102336, 0.00752005, 0.00354909, 0.00121055}
        },
        {
            {0.000709454, 0.00167174, 0.00321706, 0.0048888, 0.00566146, 0.0048888, 0.00321706, 0.00167174, 0.000709454},
            {0.00167174,  0.00393924, 0.0075806,  0.0115198, 0.0133405,  0.0115198, 0.0075806,  0.00393924, 0.00167174},
            {0.00321706,  0.0075806,  0.014588,   0.0221686, 0.0256723,  0.0221686, 0.014588,   0.0075806,  0.00321706},
            {0.0048888,   0.0115198,  0.0221686,  0.0336884, 0.0390128,  0.0336884, 0.0221686,  0.0115198,  0.0048888},
            {0.00566146,  0.0133405,  0.0256723,  0.0390128, 0.0451786,  0.0390128, 0.0256723,  0.0133405,  0.00566146},
            {0.0048888,   0.0115198,  0.0221686,  0.0336884, 0.0390128,  0.0336884, 0.0221686,  0.0115198,  0.0048888},
            {0.00321706,  0.0075806,  0.014588,   0.0221686, 0.0256723,  0.0221686, 0.014588,   0.0075806,  0.00321706},
            {0.00167174,  0.00393924, 0.0075806,  0.0115198, 0.0133405,  0.0115198, 0.0075806,  0.00393924, 0.00167174},
            {0.000709454, 0.00167174, 0.00321706, 0.0048888, 0.00566146, 0.0048888, 0.00321706, 0.00167174, 0.000709454}
        },
        {
            {0.000467204, 0.000950092, 0.00168332, 0.00256608, 0.00332561, 0.00363028, 0.00332561, 0.00256608, 0.00168332, 0.000950092, 0.000467204},
            {0.000950092, 0.00193208,  0.00342315, 0.0052183,  0.00676287, 0.00738242, 0.00676287, 0.0052183,  0.00342315, 0.00193208,  0.000950092},
            {0.00168332,  0.00342315,  0.00606496, 0.00924551, 0.0119821,  0.0130798,  0.0119821,  0.00924551, 0.00606496, 0.00342315,  0.00168332},
            {0.00256608,  0.0052183,   0.00924551, 0.014094,   0.0182657,  0.019939,   0.0182657,  0.014094,   0.00924551, 0.0052183,   0.00256608},
            {0.00332561,  0.00676287,  0.0119821,  0.0182657,  0.0236721,  0.0258407,  0.0236721,  0.0182657,  0.0119821,  0.00676287,  0.00332561},
            {0.00363028,  0.00738242,  0.0130798,  0.019939,   0.0258407,  0.028208,   0.0258407,  0.019939,   0.0130798,  0.00738242,  0.00363028},
            {0.00332561,  0.00676287,  0.0119821,  0.0182657,  0.0236721,  0.0258407,  0.0236721,  0.0182657,  0.0119821,  0.00676287,  0.00332561},
            {0.00256608,  0.0052183,   0.00924551, 0.014094,   0.0182657,  0.019939,   0.0182657,  0.014094,   0.00924551, 0.0052183,   0.00256608},
            {0.00168332,  0.00342315,  0.00606496, 0.00924551, 0.0119821,  0.0130798,  0.0119821,  0.00924551, 0.00606496, 0.00342315,  0.00168332},
            {0.000950092, 0.00193208,  0.00342315, 0.0052183,  0.00676287, 0.00738242, 0.00676287, 0.0052183,  0.00342315, 0.00193208,  0.000950092},
            {0.000467204, 0.000950092, 0.00168332, 0.00256608, 0.00332561, 0.00363028, 0.00332561, 0.00256608, 0.00168332, 0.000950092, 0.000467204}
        }
    };

    public Polification(BufferedImage image, Color background, int complexity) {
        this.watch = (int) (2 * Math.sqrt(image.getWidth() * image.getHeight()));

        this.radius = scaleRange(6 - complexity, 1, 5, 1, 5);
        this.limit = scaleRange(complexity, 1, 5, 50, watch);

        this.image = image;
        if (this.image.getType() != BufferedImage.TYPE_INT_ARGB) {
            this.image = copy();
        }
        this.pixels = ((DataBufferInt) this.image.getRaster().getDataBuffer()).getData();

        this.original = copy();
        this.pixelsOriginal = ((DataBufferInt) this.original.getRaster().getDataBuffer()).getData();

        this.background = background;

        this.points = new ArrayList<Point>();
    }

    public int scaleRange (int v, int ilo, int ihi, int olo, int ohi) {
        double percent = ((double) (v - ilo)) / ((double) (ihi - ilo));
        int result = (int) (percent * (ohi - olo)) + olo;
        return result;
    }

    public BufferedImage processImage() {
        blur();
        grayScale();
        detectEdges();

        createEdgePoints();
        addRandomPoints();
        // points = new ArrayList<Point>();
        // points.add(new Point(60, 10));
        // points.add(new Point(200, 560));
        // points.add(new Point(840, 140));
        // points.add(new Point(950, 900));
        // points.add(new Point(80, 750));
        // points.add(new Point(480, 320));
        // points.add(new Point(720, 480));
        // points.add(new Point(563, 622));
        // points.add(new Point(920, 5));

        triangulate();

        return image;
    }

    public BufferedImage copy() {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    public void blur() {
        if (radius < 1 || radius > 5) {
            radius = 3;
        }
        double[][] matrix = blurMatrix[radius - 1];

        int[] size = new int[2];
        int[] tempSize = new int[2];
        size[0] = image.getWidth();
        size[1] = image.getHeight();
        tempSize[0] = size[0] + (2 * radius);
        tempSize[1] = size[1] + (2 * radius);

        BufferedImage temp = new BufferedImage(tempSize[0], tempSize[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = temp.createGraphics();

        graphics.setPaint(background);
        graphics.fillRect(0, 0, tempSize[0], tempSize[1]);
        graphics.drawImage(image, radius, radius, null);
        graphics.dispose();

        int[] tempPixels = ((DataBufferInt) temp.getRaster().getDataBuffer()).getData();
        for (int y = radius; y < size[1] + radius; y++) {
            for (int x = radius; x < size[0] + radius; x++) {
                int index = ((y - radius) * size[0]) + (x - radius);
                int tempIndex = (y * tempSize[0]) + x;
                double aSum = 0;
                double rSum = 0;
                double gSum = 0;
                double bSum = 0;
                for (int j = y - radius; j <= y + radius; j++) {
                    for (int i = x - radius; i <= x + radius; i++) {
                        int subIndex = (j * tempSize[0]) + i;
                        aSum += ((tempPixels[subIndex] >> 24) & 0xff) * matrix[j - y + radius][i - x + radius];
                        rSum += ((tempPixels[subIndex] >> 16) & 0xff) * matrix[j - y + radius][i - x + radius];
                        gSum += ((tempPixels[subIndex] >> 8 ) & 0xff) * matrix[j - y + radius][i - x + radius];
                        bSum += ((tempPixels[subIndex] >> 0 ) & 0xff) * matrix[j - y + radius][i - x + radius];
                    }
                }
                int a = (aSum > 0xff) ? 0xff : (aSum < 0) ? 0 : (int) aSum;
                int r = (rSum > 0xff) ? 0xff : (rSum < 0) ? 0 : (int) rSum;
                int g = (gSum > 0xff) ? 0xff : (gSum < 0) ? 0 : (int) gSum;
                int b = (bSum > 0xff) ? 0xff : (bSum < 0) ? 0 : (int) bSum;
                pixels[index] = (a << 24) + (r << 16) + (g << 8) + (b << 0);
            }
        }
    }

    public int luminance(int r, int g, int b) {
        return ((3 * r) + (4 * g) + (1 * b)) >> 3;
    }

    public void grayScale() {
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i] >> 24) & 0xff;
            int r = (pixels[i] >> 16) & 0xff;
            int g = (pixels[i] >> 8 ) & 0xff;
            int b = (pixels[i] >> 0 ) & 0xff;

            int gray = luminance(r, g, b);

            pixels[i] = (a << 24) + (gray << 16) + (gray << 8) + (gray << 0);
        }
    }

    public void detectEdges() {
        int[] size = new int[2];
        int[] tempSize = new int[2];
        size[0] = image.getWidth();
        size[1] = image.getHeight();
        tempSize[0] = size[0] + 2;
        tempSize[1] = size[1] + 2;

        BufferedImage temp = new BufferedImage(tempSize[0], tempSize[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = temp.createGraphics();

        graphics.setPaint(background);
        graphics.fillRect(0, 0, tempSize[0], tempSize[1]);
        graphics.drawImage(image, 1, 1, null);
        graphics.dispose();

        int[] tempPixels = ((DataBufferInt) temp.getRaster().getDataBuffer()).getData();
        for (int y = 1; y < size[1] + 1; y++) {
            for (int x = 1; x < size[0] + 1; x++) {
                int index = ((y - 1) * size[0]) + (x - 1);
                int tempIndex = (y * tempSize[0]) + x;
                int alpha = (tempPixels[tempIndex] >> 24) & 0xff;
                int gx = 0;
                int gy = 0;
                for (int j = y - 1; j <= y + 1; j++) {
                    for (int i = x - 1; i <= x + 1; i++) {
                        int subIndex = (j * tempSize[0]) + i;
                        int r = (tempPixels[subIndex] >> 16) & 0xff;
                        int g = (tempPixels[subIndex] >> 8 ) & 0xff;
                        int b = (tempPixels[subIndex] >> 0 ) & 0xff;
                        int gray = luminance(r, g, b);
                        gx += gray * sobelMatrix[0][j - y + 1][i - x + 1];
                        gy += gray * sobelMatrix[1][j - y + 1][i - x + 1];
                    }
                }
                int level = (Math.abs(gx) + Math.abs(gy)) / 2;
                if (level > 0xff) {
                    level = 0xff;
                }
                pixels[index] = (alpha << 24) + (level << 16) + (level << 8) + (level << 0);
            }
        }
    }

    public void createEdgePoints() {
        int[] size = new int[2];
        size[0] = image.getWidth();
        size[1] = image.getHeight();

        for (int y = radius; y < size[1] - radius; y += ((int) (Math.random() * radius)) + (25 /radius)) {
            for (int x = radius; x < size[0] - radius; x+= ((int) (Math.random() * radius)) + (25 /radius)) {
                int index = (y * size[0]) + x;
                int a = (pixels[index] >> 24) & 0xff;
                int r = (pixels[index] >> 16) & 0xff;
                int g = (pixels[index] >> 8 ) & 0xff;
                int b = (pixels[index] >> 0 ) & 0xff;

                int gray = luminance(r, g, b);

                boolean chance = (Math.random()) < 0.5;

                if (gray > 0x10 && chance) {
                    pixels[index] = (a << 24) + 0xff0000;
                    points.add(new Point(x, y));
                }
            }
        }
    }

    public void addRandomPoints() {
        int xSize = image.getWidth();
        int i = limit - points.size();
        int hits = 0;

        while (i > 0 || hits > watch) {
            int index = (int) (Math.random() * pixels.length);
            int a = (pixels[index] >> 24) & 0xff;
            int r = (pixels[index] >> 16) & 0xff;
            if (r != 0xff) {
                pixels[index] = (a << 24) + 0xff0000;
                points.add(new Point(index % xSize, index / xSize));
                i--;
            } else {
                hits++;
            }
        }
    }

    public void triangulate() {
        // size variables
        int[] size = new int[2];
        size[0] = image.getWidth();
        size[1] = image.getHeight();
        int v = points.size();

        // check if there are enough points for a triangle
        if (v < 3) {
            return;
        }

        // sort the points by their x value
        Collections.sort(points, new Comparator<Point>(){
            @Override
            public int compare(Point p, Point q){
                return p.x - q.x;
            }
        });

        // System.out.printf("%s\n", points);

        // assume min and max value
        int xMin = points.get(0).x;
        int xMax = points.get(v - 1).x;
        int yMin = points.get(0).y;
        int yMax = yMin;

        // adjust the min and max values to be accurate
        for (Point p : points) {
            if (p.x < xMin) {
                xMin = p.x;
            }
            if (p.x > xMax) {
                xMax = p.x;
            }
            if (p.y < yMin) {
                yMin = p.y;
            }
            if (p.y > yMax) {
                yMax = p.y;
            }
        }

        // triangulation
        List<Triangle> triangles = new ArrayList<Triangle>();

        // more size variables
        int dxMax = (xMax - xMin);
        int dyMax = (yMax - yMin);
        int dMax = (dxMax > dyMax) ? dxMax : dyMax;
        int xMid = (int) Math.round((xMax + xMin) / 2.0);
        int yMid = (int) Math.round((yMax + yMin) / 2.0);

        // super triangle points
        Point superA = new Point(xMid - (20 * dMax), yMid -       dMax);
        Point superB = new Point(xMid              , yMid + (20 * dMax));
        Point superC = new Point(xMid + (20 * dMax), yMid -       dMax);

        // make the points sentinel
        superA.sentinel = true;
        superB.sentinel = true;
        superC.sentinel = true;

        // super triangle sentinel bounding all points
        Triangle superTriangle = new Triangle(superA, superB, superC);

        // add the super triangle to the open list
        triangles.add(superTriangle);

        // add points to the mesh
        for (Point p : points) {
            List<Edge> edges = new ArrayList<Edge>();

            for (int j = triangles.size() - 1; j >= 0; j--) {
                Triangle t = triangles.get(j);

                if (t.circle.contains(p)) {
                    edges.addAll(t.edges);
                    triangles.remove(j);
                }
            }

            // add a new triangle for each unique edge
            for (Edge e : edges) {
                if (Collections.frequency(edges, e) < 2) {
                    triangles.add(new Triangle(e.a, e.b, p));
                }
            }
        }

        // remove those that touch the supertriangle
        for (int i = triangles.size() - 1; i >= 0; i--) {
            Triangle t = triangles.get(i);
            if (
                t.a.sentinel ||
                t.b.sentinel ||
                t.c.sentinel
            ) {
                triangles.remove(i);
            }
        }

        Color color = new Color(0xff0000);
        Graphics2D graphics = image.createGraphics();

        graphics.setPaint(background);
        graphics.setPaint(new Color(0xff000000));
        graphics.fillRect(0, 0, size[0], size[1]);
        // graphics.drawImage(original, 0, 0, null);
        graphics.setPaint(color);

        for (Triangle t : triangles) {
            int[] position = new int[2];
            position[0] = (t.a.x + t.b.x + t.c.x) / 3;
            position[1] = (t.a.y + t.b.y + t.c.y) / 3;

            int index = (position[1] * size[0]) + position[0];

            if (index >= 0 && index < pixelsOriginal.length) {
                int p = pixelsOriginal[index];
                int a = (p >> 24) & 0xff;

                // color = new Color(p, a < 0xff);
                // graphics.setPaint(color);
                // graphics.fillPolygon(t.xPoints, t.yPoints, 3);
                graphics.setPaint(new Color(0xff0000));
                graphics.drawPolygon(t.xPoints, t.yPoints, 3);
                graphics.setPaint(new Color(0xffff00));
                // int circumradius = ((int) Math.round(t.circle.radius));
                // graphics.drawOval(t.circle.center.x - 1, t.circle.center.y - 1, 2, 2);
                // graphics.drawOval(t.circle.center.x - circumradius, t.circle.center.y - circumradius, circumradius * 2, circumradius * 2);
            } else {
                System.out.printf("index was out of bounds: %d , %d\n", position[0], position[1]);
            }
        }

        graphics.dispose();
    }

}
