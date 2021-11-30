package FH5Sniper;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;

public class ScreenManager {
    private static Robot robot;
    private static Tesseract tesseract;

    public static void init(Robot r) {
        robot = r;
        tesseract = new Tesseract();
        tesseract.setDatapath("D:/Coding/FH5Sniper/src/main/java/FH5Sniper/tessdata/");
    }

    public static String getText(BufferedImage img) throws TesseractException {
        return tesseract.doOCR(img);
    }

    public static final Rectangle auctionRect = new Rectangle(975, 820, 295, 65);
    public static final Rectangle searchConfirmRect = new Rectangle(550, 650, 900, 150);
    public static final Rectangle noAuctionsRect = new Rectangle(1100, 475, 600, 200);
    public static final Rectangle buyoutSuccessRect = new Rectangle(600, 410, 700, 250);

    public static Rectangle randShiftRect(Rectangle curRect) {
        Rectangle newRect = new Rectangle(curRect);

        newRect.x += (Math.random() * 10) - 5;
        newRect.y += (Math.random() * 10) - 5;

        newRect.width += (Math.random() * 10) - 5;
        newRect.height += (Math.random() * 10) - 5;

        return newRect;
    }

    public static BufferedImage capture(Rectangle curRect) {
        return robot.createScreenCapture(curRect);
    }

    public static BufferedImage toBlackWhite(BufferedImage img) {
        BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D g = gray.createGraphics();
        g.drawImage(img, 0, 0, null);

        return gray;
    }

    // https://www.geeksforgeeks.org/image-processing-in-java-colored-image-to-negative-image-conversion/
    public static BufferedImage invert(BufferedImage img) {
        BufferedImage inverted = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int p = img.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                // subtract RGB from 255
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;

                // set new RGB value
                p = (a << 24) | (r << 16) | (g << 8) | b;
                inverted.setRGB(x, y, p);
            }
        }
        
        return inverted;
    }
}
