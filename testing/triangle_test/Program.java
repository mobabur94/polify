import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.io.File;

public class Program{
    public static void main(String []args){

        Color black  = new Color(0x000000);

        for (int i = 175; i <= 225; i += 2) {
            BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();

            graphics.setClip(0, 0, 400, 400);
            graphics.setColor(black);
            graphics.fillRect(0, 0, 400, 400);

            Point a = new Point(225, 175);
            Point b = new Point(i, i);
            Point c = new Point(175, 225);
            Triangle triangle = new Triangle(a, b, c);

            triangle.draw(graphics);

            try {
                ImageIO.write(image, "png", new File(String.format("output-%d.png", i)));
            } catch (IOException e) {
                System.out.printf("IOException: %s\n", e.getMessage());
            }

            graphics.dispose();
        }
    }
}
