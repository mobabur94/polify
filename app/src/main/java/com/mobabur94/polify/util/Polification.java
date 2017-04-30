package com.mobabur94.polify.util;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat6;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.imgproc.Subdiv2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Polification {

    int size;
    int complexity;
    boolean stroke;

    Random randomizer;

    Mat original;
    Mat altered;

    Bitmap result;

    public Polification (Bitmap photo, int complexity, boolean stroke) {
        // set the size of the image [maximum dimension]
        this.size = 1000;

        // get the complexity and stroke
        this.complexity = complexity;
        this.stroke = stroke;

        // get a randomizer
        this.randomizer = new Random();

        // prepare the matrices
        this.original = new Mat();
        this.altered = new Mat();

        // get the photo and resize it to a max dimension of 1080px
        this.original = resize(photo);

        // use an altered copy so we can retain color info for later
        this.altered = this.original.clone();

        // begin the polification
        this.result = process();
    }

    public Bitmap getResult () {
        return result;
    }

    Mat resize (Bitmap photo) {
        // prepare the matrices
        Mat result = new Mat();
        Mat unscaled = new Mat();

        // convert the bitmap
        Utils.bitmapToMat(photo, unscaled);

        // scale down to d with INTER_AREA or up to d with INTER_LINEAR
        int w = photo.getWidth();
        int h = photo.getHeight();
        int interpolation = (w > size || h > size) ? Imgproc.INTER_AREA : Imgproc.INTER_LINEAR;
        if (w > h) {
            Imgproc.resize(unscaled, result, new Size(size, size * (h / (double) w)), 0, 0, interpolation);
        } else {
            Imgproc.resize(unscaled, result, new Size(size * (w / (double) h), size), 0, 0, interpolation);
        }

        return result;
    }

    Bitmap process () {
        // get colored and grayscale versions
        Mat colored = new Mat();
        Mat uncolored = new Mat();
        Imgproc.cvtColor(altered, colored, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(colored, uncolored, Imgproc.COLOR_RGB2GRAY);

        // blur a bit
        Imgproc.GaussianBlur(uncolored, uncolored, new Size(5, 5), 0);
        Imgproc.GaussianBlur(colored, colored, new Size(5, 5), 0);

        // detect the edges
        Mat edges = new Mat();
        Imgproc.Canny(uncolored, edges, 150 - (complexity * 10), 450 - (complexity * 30));

        // get points along the detected edges
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edges.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_KCOS);
        List<Point> points = new ArrayList<>();
        for (MatOfPoint mat : contours) {
            for (Point p : mat.toList()) {
                if (randomizer.nextInt(10) < 3) {
                    points.add(p);
                }
            }
        }

        // add random points to make it seem more natural
        int remaining = (int) (Math.sqrt(original.width() * original.height()) / 2);
        for (int i = 0; i < remaining; i++) {
            int x = randomizer.nextInt(original.width());
            int y = randomizer.nextInt(original.height());
            points.add(new Point(x, y));
        }

        Log.d(getClass().getName(), "initializing delaunay...");
        // triangulate the points into a delaunay triangulation
        MatOfPoint2f vertices = new MatOfPoint2f();
        vertices.fromList(points);
        Rect outline = new Rect(0, 0, original.width(), original.height());
        Subdiv2D triangulation = new Subdiv2D();
        triangulation.initDelaunay(outline);
        Log.d(getClass().getName(), "done initializing delaunay");
        Log.d(getClass().getName(), "inserting vertices...");
        triangulation.insert(vertices);
        Log.d(getClass().getName(), "done inserting vertices");

        // paint the triangles
        Mat canvas = Mat.zeros(original.size(), CvType.CV_8UC3);
        MatOfFloat6 triangles = new MatOfFloat6();
        triangulation.getTriangleList(triangles);
        float[] t = new float[6];
        Point[] p = new Point[3];
        Scalar maskColor = new Scalar(255, 255, 255);
        Log.d(getClass().getName(), "painting triangles...");
        for (int y = 0; y < triangles.rows(); y++) {
            for (int x = 0; x < triangles.cols(); x++) {
                triangles.get(y, x, t);
                p[0] = new Point(t[0], t[1]);
                p[1] = new Point(t[2], t[3]);
                p[2] = new Point(t[4], t[5]);
//                Log.d(getClass().getName(), String.format("triangle: (%d , %d) => points: (%.2f , %.2f) (%.2f , %.2f) (%.2f , %.2f)", y, x, t[0], t[1], t[2], t[3], t[4], t[5]));
                MatOfPoint triangle = new MatOfPoint(p);
                Moments moments = Imgproc.moments(triangle);
                int cx = (int) Math.round(moments.get_m10() / moments.get_m00());
                int cy = (int) Math.round(moments.get_m01() / moments.get_m00());
                Scalar color = new Scalar(colored.get(cy, cx));
                Imgproc.fillConvexPoly(canvas, triangle, color);
//                Mat mask = Mat.zeros(original.size(), CvType.CV_8UC1);
//                Imgproc.fillConvexPoly(mask, triangle, maskColor);
//                Scalar averageColor = Core.mean(colored, mask);
//                Imgproc.fillConvexPoly(canvas, triangle, averageColor);
            }
        }
        Log.d(getClass().getName(), "done painting triangles");

        // commit result
        altered = canvas;

        // convert the matrix into a photo
        Bitmap result = Bitmap.createBitmap(altered.width(), altered.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(altered, result);

        return result;
    }

}
