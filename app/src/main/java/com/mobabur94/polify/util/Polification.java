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
import org.opencv.imgproc.Subdiv2D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
        this.size = 1080;

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
        // get rid of alpha channel
        Mat opaque = new Mat();
        Imgproc.cvtColor(altered, opaque, Imgproc.COLOR_RGBA2RGB);

        // detect the edges
        Mat edges = new Mat();
        Imgproc.Canny(opaque, edges, 150 - (complexity * 10), 250 - (complexity * 10));

        // get points along the detected edges
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_KCOS);
        List<Point> points = new ArrayList<>();
        for (MatOfPoint mat : contours) {
            for (Point p : mat.toList()) {
                if (randomizer.nextInt(10) < 3) {
                    points.add(p);
                }
            }
        }

        // add random points to make it seem more natural
        int remaining = (int) (Math.sqrt(opaque.width() * opaque.height()) / 2);
        for (int i = 0; i < remaining; i++) {
            int x = randomizer.nextInt(opaque.width());
            int y = randomizer.nextInt(opaque.height());
            points.add(new Point(x, y));
        }

        // triangulate the points into a delaunay triangulation
        MatOfPoint2f vertices = new MatOfPoint2f();
        vertices.fromList(points);
        Rect outline = new Rect(0, 0, opaque.width(), opaque.height());
        Subdiv2D triangulation = new Subdiv2D();
        triangulation.initDelaunay(outline);
        triangulation.insert(vertices);

        // paint the triangles
        Mat canvas = Mat.zeros(opaque.size(), CvType.CV_8UC3);
        MatOfFloat6 triangles = new MatOfFloat6();
        triangulation.getTriangleList(triangles);
        float[] t = new float[6];
        Point[] p = new Point[3];
        Scalar maskColor = new Scalar(255, 255, 255);
        for (int y = 0; y < triangles.rows(); y++) {
            for (int x = 0; x < triangles.cols(); x++) {
                triangles.get(y, x, t);
                p[0] = new Point(t[0], t[1]);
                p[1] = new Point(t[2], t[3]);
                p[2] = new Point(t[4], t[5]);
                MatOfPoint triangle = new MatOfPoint(p);
                Mat mask = Mat.zeros(opaque.size(), CvType.CV_8UC1);
                Imgproc.fillConvexPoly(mask, triangle, maskColor);
                Scalar averageColor = Core.mean(opaque, mask);
                Imgproc.fillConvexPoly(canvas, triangle, averageColor);
            }
        }

        // commit result
        altered = canvas;

        // convert the matrix into a photo
        Bitmap result = Bitmap.createBitmap(altered.width(), altered.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(altered, result);

        return result;
    }

}
