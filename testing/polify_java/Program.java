import java.util.List;

import java.lang.Math;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.awt.Color;

public class Program {

    public static BufferedImage polify(BufferedImage image, int complexity) {
        Color background = new Color(0, 0, 0, 0);
        Polification polification = new Polification(image, background, complexity);
        return polification.processImage();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.printf("usage: java Program [file]\n");
        } else {
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(args[0]));
            } catch (IOException e) {
                System.out.printf("Could not find file: %s\n", args[0]);
            }
            if (image != null) {
                if (image.getWidth() < 100 || image.getHeight() < 100) {
                    System.out.printf("Image is required to be at least 100px in each dimension.\n");
                } else {
                    for (int complexity = 3; complexity <= 3; complexity++) {
                        String resultName = "result" + complexity + ".png";
                        BufferedImage resultImage = polify(image, complexity);
                        try {
                            ImageIO.write(resultImage, "png", new File(resultName));
                        } catch (IOException e) {
                            System.out.printf("Could not write to file: %s\n", resultName);
                        }
                    }
                }
            }
        }
    }

}
