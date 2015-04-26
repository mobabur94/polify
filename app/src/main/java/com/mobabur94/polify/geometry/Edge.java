package com.mobabur94.polify.geometry;

public class Edge {

    public Point a;
    public Point b;

    public Point mid;

    public double length;
    public double slope;

    public Edge(Point a, Point b) {
        // get the start and end
        this.a = a;
        this.b = b;

        // calculate the midpoint
        this.mid = new Point(
            (int) Math.round((a.x + b.x) / 2.0),
            (int) Math.round((a.y + b.y) / 2.0)
        );

        // calculate displacement
        int dx = b.x - a.x;
        int dy = b.y - a.y;

        // calculate the length
        this.length = Math.sqrt((dx * dx) + (dy * dy));

        // calculate the slope
        this.slope = ((double) dy) / ((double) dx);
    }

}
