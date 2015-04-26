import java.lang.Double;
import java.lang.Math;

import java.awt.Color;
import java.awt.Graphics2D;

public class Triangle{
    public Point a;
    public Point b;
    public Point c;

    public Edge aa; // opposite a
    public Edge bb; // opposite b
    public Edge cc; // opposite c

    public Circle circle;

    public Triangle(Point a, Point b, Point c){
        this.a = a;
        this.b = b;
        this.c = c;

        this.aa = new Edge(b, c);
        this.bb = new Edge(c, a);
        this.cc = new Edge(a, b);

        double aaa = aa.length;
        double bbb = bb.length;
        double ccc = cc.length;

        double radius = (aaa * bbb * ccc) / Math.sqrt(
            (aaa + bbb + ccc) *
            (bbb + ccc - aaa) *
            (aaa + ccc - bbb) *
            (aaa + bbb - ccc)
        );

        double aaaa = (0 - 1) / aa.m; // inverse slope of the edge opposite a
        double bbbb = (0 - 1) / bb.m; // inverse slope of the edge opposite b
        double cccc = (0 - 1) / cc.m; // inverse slope of the edge opposite c

        int x = 0;
        int y = 0;

        if (aaaa == Double.POSITIVE_INFINITY || aaaa == Double.NEGATIVE_INFINITY) {
            double bbbbb = bb.mid.y - (bbbb * bb.mid.x); // y-intercept of the perpendicular bisector of the edge opposite b
            double ccccc = cc.mid.y - (cccc * cc.mid.x); // y-intercept of the perpendicular bisector of the edge opposite c

            // x = (negative difference in y-intercepts) / (difference in slopes)
            x = (int) Math.round((bbbbb - ccccc) / (cccc - bbbb));

            // y = (slope * x) + y-intercept
            y = (int) Math.round((
                ((bbbb * x) + bbbbb) +
                ((cccc * x) + ccccc)
            ) / 2.0);
        } else if (bbbb == Double.POSITIVE_INFINITY || bbbb == Double.NEGATIVE_INFINITY) {
            double aaaaa = aa.mid.y - (aaaa * aa.mid.x); // y-intercept of the perpendicular bisector of the edge opposite a
            double ccccc = cc.mid.y - (cccc * cc.mid.x); // y-intercept of the perpendicular bisector of the edge opposite c

            // x = (negative difference in y-intercepts) / (difference in slopes)
            x = (int) Math.round((ccccc - aaaaa) / (aaaa - cccc));

            // y = (slope * x) + y-intercept
            y = (int) Math.round((
                ((aaaa * x) + aaaaa) +
                ((cccc * x) + ccccc)
            ) / 2.0);
        } else if (cccc == Double.POSITIVE_INFINITY || cccc == Double.NEGATIVE_INFINITY) {
            double aaaaa = aa.mid.y - (aaaa * aa.mid.x); // y-intercept of the perpendicular bisector of the edge opposite a
            double bbbbb = bb.mid.y - (bbbb * bb.mid.x); // y-intercept of the perpendicular bisector of the edge opposite b

            // x = (negative difference in y-intercepts) / (difference in slopes)
            x = (int) Math.round((aaaaa - bbbbb) / (bbbb - aaaa));

            // y = (slope * x) + y-intercept
            y = (int) Math.round((
                ((aaaa * x) + aaaaa) +
                ((bbbb * x) + bbbbb)
            ) / 2.0);
        } else {
            double aaaaa = aa.mid.y - (aaaa * aa.mid.x); // y-intercept of the perpendicular bisector of the edge opposite a
            double bbbbb = bb.mid.y - (bbbb * bb.mid.x); // y-intercept of the perpendicular bisector of the edge opposite b
            double ccccc = cc.mid.y - (cccc * cc.mid.x); // y-intercept of the perpendicular bisector of the edge opposite c

            // x = (negative difference in y-intercepts) / (difference in slopes)
            x = (int) Math.round((
                ((bbbbb - ccccc) / (cccc - bbbb)) +
                ((ccccc - aaaaa) / (aaaa - cccc)) +
                ((aaaaa - bbbbb) / (bbbb - aaaa))
            ) / 3.0);

            // y = (slope * x) + y-intercept
            y = (int) Math.round((
                ((aaaa * x) + aaaaa) +
                ((bbbb * x) + bbbbb) +
                ((cccc * x) + ccccc)
            ) / 3.0);
        }

        Point center = new Point(x, y);

        this.circle = new Circle(center, radius);
    }

    public int[] xPoints(){
        int[] points = new int[3];
        points[0] = this.a.x;
        points[1] = this.b.x;
        points[2] = this.c.x;
        return points;
    }

    public int[] yPoints(){
        int[] points = new int[3];
        points[0] = this.a.y;
        points[1] = this.b.y;
        points[2] = this.c.y;
        return points;
    }

    @Override
    public String toString(){
        return String.format("\n\n\ta: %s\n\tb: %s\n\tc: %s\n\n\tbc: %s\n\tca: %s\n\tab: %s\n\n\tcircle: %s", a, b, c, aa, bb, cc, circle);
    }

    public void draw(Graphics2D graphics){
        Color red    = new Color(0xff0000);
        Color green  = new Color(0x00ff00);
        Color blue   = new Color(0x0000ff);
        Color yellow = new Color(0xffff00);

        int diameter = (int) (2 * circle.radius);

        graphics.setColor(red);
        graphics.drawPolygon(xPoints(), yPoints(), 3);

        graphics.setColor(yellow);
        graphics.drawOval(a.x - 3, a.y - 3, 6, 6);
        graphics.drawOval(b.x - 3, b.y - 3, 6, 6);
        graphics.drawOval(c.x - 3, c.y - 3, 6, 6);
        graphics.drawOval(circle.center.x - 3, circle.center.y - 3, 6, 6);

        graphics.setColor(green);
        graphics.drawOval(circle.center.x - (diameter / 2), circle.center.y - (diameter / 2), diameter, diameter);
    }
}
