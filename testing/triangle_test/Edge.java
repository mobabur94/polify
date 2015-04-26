import java.lang.Math;

public class Edge{
    public Point a;
    public Point b;
    public Point mid;

    public double m;
    public double length;

    public Edge(Point a, Point b){
        this.a = a;
        this.b = b;

        this.mid = new Point((int) Math.round((a.x + b.x) / 2.0), (int) Math.round((a.y + b.y) / 2.0));

        this.m = ((double) (b.y - a.y)) / ((double) (b.x - a.x));

        this.length = Math.sqrt(Math.pow((b.x - a.x), 2) + Math.pow((b.y - a.y), 2));
    }

    @Override
    public String toString(){
        return String.format("midpoint @ %s with slope of %f and length of %f", mid, m, length);
    }
}
