package com.mobabur94.polify.geometry;

public class Triangle {

    double PINF = Double.POSITIVE_INFINITY; // alias
    double NINF = Double.NEGATIVE_INFINITY; // alias

    public Point a;
    public Point b;
    public Point c;

    public Edge oppositeA; // opposite a
    public Edge oppositeB; // opposite b
    public Edge oppositeC; // opposite c

    public Circle circle;

    public Triangle(Point a, Point b, Point c) {
        // get the points
        this.a = a;
        this.b = b;
        this.c = c;

        // create the opposing edges
        this.oppositeA = new Edge(b, c);
        this.oppositeB = new Edge(c, a);
        this.oppositeC = new Edge(a, b);

        // calculate the radius of the circumcircle
        double radius = (oppositeA.length * oppositeB.length * oppositeC.length) / Math.sqrt(
            (0 + oppositeA.length + oppositeB.length + oppositeC.length) *
            (0 + oppositeA.length + oppositeB.length - oppositeC.length) *
            (0 + oppositeA.length - oppositeB.length + oppositeC.length) *
            (0 - oppositeA.length + oppositeB.length + oppositeC.length)
        );

        // calculate the inverse slopes of the opposing edges
        double slopeA = (0 - 1) / oppositeA.length;
        double slopeB = (0 - 1) / oppositeB.length;
        double slopeC = (0 - 1) / oppositeC.length;

        // initialize circumcircle center x and y
        int x = 0;
        int y = 0;

        // calculate y-intercepts of the perpendicular bisectors of the opposing edges
        // y-intercept = y - (slope * x)
        double yInterceptA = oppositeA.mid.y - (slopeA * oppositeA.mid.x);
        double yInterceptB = oppositeB.mid.y - (slopeB * oppositeB.mid.x);
        double yInterceptC = oppositeC.mid.y - (slopeC * oppositeC.mid.x);

        // calculate the circumcircle center x and y points
        // x = (negative difference in y-intercepts) / (difference in slopes)
        // y = (slope * x) + y-intercept
        // take averages based on which y-intercepts don't exist
        if (yInterceptA == PINF || yInterceptA == NINF) {
            x = (int) Math.round((yInterceptB - yInterceptC) / (slopeC - slopeB));
            y = (int) Math.round((
                ((slopeB * x) + yInterceptB) +
                ((slopeC * x) + yInterceptC)
            ) / 2.0);
        } else if (yInterceptB == PINF || yInterceptB == NINF) {
            x = (int) Math.round((yInterceptC - yInterceptA) / (slopeA - slopeC));
            y = (int) Math.round((
                ((slopeA * x) + yInterceptA) +
                ((slopeC * x) + yInterceptC)
            ) / 2.0);
        } else if (yInterceptC == PINF || yInterceptC == NINF) {
            x = (int) Math.round((yInterceptA - yInterceptB) / (slopeB - slopeA));
            y = (int) Math.round((
                ((slopeA * x) + yInterceptA) +
                ((slopeB * x) + yInterceptB)
            ) / 2.0);
        } else {
            x = (int) Math.round((
                ((yInterceptB - yInterceptC) / (slopeC - slopeB)) +
                ((yInterceptC - yInterceptA) / (slopeA - slopeC)) +
                ((yInterceptA - yInterceptB) / (slopeB - slopeA))
            ) / 3.0);
            y = (int) Math.round((
                ((slopeA * x) + yInterceptA) +
                ((slopeB * x) + yInterceptB) +
                ((slopeC * x) + yInterceptC)
            ) / 3.0);
        }

        // create the center point
        Point center = new Point(x, y);

        // create the circumcircle
        this.circle = new Circle(center, radius);
    }

}
