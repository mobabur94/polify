package com.mobabur94.polify.geometry;

public class Circle {

    public Point center;

    public double radius;

    public Circle(Point center, double radius) {
        // get the center and radius
        this.center = center;
        this.radius = radius;
    }

    public boolean contains(Point p) {
        // create a new edge between the center and the point, in order to check the distance
        Edge e = new Edge(center, p);
        return e.length <= radius;
    }

}
