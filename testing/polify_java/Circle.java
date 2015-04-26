import java.lang.Math;

public class Circle {

    public Point center;

    public double radius;

    public Circle(Point center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public boolean contains(Point p) {
        Edge e = new Edge(center, p);
        return e.length <= radius;
    }

}
