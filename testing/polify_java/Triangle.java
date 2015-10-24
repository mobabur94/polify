import java.lang.Math;

import java.util.List;
import java.util.ArrayList;

public class Triangle {

    public Point a;
    public Point b;
    public Point c;

    public int[] xPoints = new int[3];
    public int[] yPoints = new int[3];

    public Edge aa; // opposite a
    public Edge bb; // opposite b
    public Edge cc; // opposite c

    public List<Edge> edges;

    public Circle circle;

    public Triangle(Point a, Point b, Point c) {
        this.a = a;
        this.b = b;
        this.c = c;

        this.xPoints[0] = a.x;
        this.xPoints[1] = b.x;
        this.xPoints[2] = c.x;

        this.yPoints[0] = a.y;
        this.yPoints[1] = b.y;
        this.yPoints[2] = c.y;

        this.aa = new Edge(b, c);
        this.bb = new Edge(c, a);
        this.cc = new Edge(a, b);

        this.edges = new ArrayList<Edge>();
        this.edges.add(aa);
        this.edges.add(bb);
        this.edges.add(cc);

        double aaa = aa.length;
        double bbb = bb.length;
        double ccc = cc.length;

        double radius = (aaa * bbb * ccc) / Math.sqrt(
            (aaa + bbb + ccc) *
            (bbb + ccc - aaa) *
            (aaa + ccc - bbb) *
            (aaa + bbb - ccc)
        );

        double aaaa = (0 - 1) / aa.slope; // inverse slope of edge opposite a
        double bbbb = (0 - 1) / bb.slope; // inverse slope of edge opposite b
        double cccc = (0 - 1) / cc.slope; // inverse slope of edge opposite c

        int x = 0; // x = (negative difference in y-intercepts) / (difference in slopes)
        int y = 0; // y = (slope * x) + y-intercept

        double aaaaa = aa.mid.y - (aaaa * aa.mid.x); // y-intercept of the perpendicular bisector of the edge opposite a
        double bbbbb = bb.mid.y - (bbbb * bb.mid.x); // y-intercept of the perpendicular bisector of the edge opposite b
        double ccccc = cc.mid.y - (cccc * cc.mid.x); // y-intercept of the perpendicular bisector of the edge opposite c

        if (Math.abs(aaaa) == Double.POSITIVE_INFINITY) {
            x = (int) Math.round((bbbbb - ccccc) / (cccc - bbbb));
            y = (int) Math.round((
                ((bbbb * x) + bbbbb) +
                ((cccc * x) + ccccc)
            ) / 2.0);
        } else if (Math.abs(bbbb) == Double.POSITIVE_INFINITY) {
            x = (int) Math.round((ccccc - aaaaa) / (aaaa - cccc));
            y = (int) Math.round((
                ((aaaa * x) + aaaaa) +
                ((cccc * x) + ccccc)
            ) / 2.0);
        } else if (Math.abs(cccc) == Double.POSITIVE_INFINITY) {
            x = (int) Math.round((aaaaa - bbbbb) / (bbbb - aaaa));
            y = (int) Math.round((
                ((aaaa * x) + aaaaa) +
                ((bbbb * x) + bbbbb)
            ) / 2.0);
        } else {
            x = (int) Math.round((
                ((bbbbb - ccccc) / (cccc - bbbb)) +
                ((ccccc - aaaaa) / (aaaa - cccc)) +
                ((aaaaa - bbbbb) / (bbbb - aaaa))
            ) / 3.0);
            y = (int) Math.round((
                ((aaaa * x) + aaaaa) +
                ((bbbb * x) + bbbbb) +
                ((cccc * x) + ccccc)
            ) / 3.0);
        }

        Point center = new Point(x, y);

        this.circle = new Circle(center, radius);
    }

    @Override
    public String toString() {
        return String.format("%s ++ %s ++ %s", aa, bb, cc);
    }

}
