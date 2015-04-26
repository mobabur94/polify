public class Circle{
    public Point center;
    public double radius;

    public Circle(Point center, double radius){
        this.center = center;
        this.radius = radius;
    }

    @Override
    public String toString(){
        return String.format("centered at %s with radius of %f", center, radius);
    }
}
