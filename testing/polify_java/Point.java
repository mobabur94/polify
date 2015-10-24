public class Point {

    public int x;
    public int y;

    public boolean sentinel = false;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
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
            Point p = (Point) o;
            return (
                this.x == p.x &&
                this.y == p.y
            );
        }
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]", x, y);
    }

}
