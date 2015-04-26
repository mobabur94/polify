import java.lang.Math;

public class Edge {

    public Point a;
    public Point b;

    public Point mid;

    public double length;
    public double slope;

    public Edge(Point a, Point b){
        this.a = a;
        this.b = b;

        this.mid = new Point(
            (int) Math.round((b.x + a.x) / 2.0),
            (int) Math.round((b.y + a.y) / 2.0)
        );

        int dx = b.x - a.x;
        int dy = b.y - a.y;

        this.length = Math.sqrt((dx * dx) + (dy * dy));

        this.slope = ((double) dy) / ((double) dx);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (
            o == null ||
            o.getClass() != this.getClass()
        ) {
            return false;
        } else {
            Edge e = (Edge) o;
            return (
                (
                    this.a.equals(e.a) &&
                    this.b.equals(e.b)
                ) ||
                (
                    this.a.equals(e.b) &&
                    this.b.equals(e.a)
                )
            );
        }
    }

}
