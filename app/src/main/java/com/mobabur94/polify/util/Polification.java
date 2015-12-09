package com.mobabur94.polify.util;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Polification {

    int complexity;
    boolean stroke;

    Mat original;
    Mat altered;

    Bitmap result;

    public Polification (Bitmap photo, int complexity, boolean stroke) {
        // get the complexity and stroke
        this.complexity = complexity;
        this.stroke = stroke;

        // prepare the matrices
        this.original = new Mat();
        this.altered = new Mat();

        // get the photo and resize it to a max dimension of 1080px
        this.original = resize(photo, 1080);

        // use an altered copy so we can retain color info for later
        this.altered = this.original.clone();

        // begin the polification
        this.result = process();
    }

    public Bitmap getResult () {
        return result;
    }

    Mat resize (Bitmap photo, int d) {
        // prepare the matrices
        Mat result = new Mat();
        Mat unscaled = new Mat();

        // convert the bitmap
        Utils.bitmapToMat(photo, unscaled);

        // scale down to d with INTER_AREA or up to d with INTER_LINEAR
        int w = photo.getWidth();
        int h = photo.getHeight();
        int interpolation = (w > d || h > d) ? Imgproc.INTER_AREA : Imgproc.INTER_LINEAR;
        if (w > h) {
            Imgproc.resize(unscaled, result, new Size(d, d * (h / (double) w)), 0, 0, interpolation);
        } else {
            Imgproc.resize(unscaled, result, new Size(d * (w / (double) h), d), 0, 0, interpolation);
        }

        return result;
    }

    Bitmap process () {
        // get rid of alpha channel
        Mat opaque = new Mat();
        Imgproc.cvtColor(altered, opaque, Imgproc.COLOR_RGBA2RGB);

        // apply a bilateral blur to keep edges intact
        Mat blur = new Mat();
        Imgproc.bilateralFilter(opaque, blur, complexity + 5, complexity * 15, complexity * 15);

        // grayscale to aid with edge detection
        Mat gray = new Mat();
        Imgproc.cvtColor(blur, gray, Imgproc.COLOR_RGBA2GRAY);

        // TODO: detect the edges

        // TODO: create points along the detected edges

        // TODO: add some random points to make it seem more natural

        // TODO: triangulate the points into a delaunay triangulation

        // TODO: fill each triangle with an average color

        // commit result
        altered = gray;

        // convert the matrix into a photo
        Bitmap result = Bitmap.createBitmap(altered.width(), altered.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(altered, result);

        return result;
    }

}
