package FH5Sniper;

import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("BusyWait")
public class Driver {
    private static Robot robot;
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    public static void main(String[] args) throws AWTException, InterruptedException, IOException {
        robot = new Robot();
        ScreenManager.init(robot);
        
        while (true) {
            Color selectionColor = new Color(255, 0, 134);
            Color detailsBarColor = new Color(52, 23, 53);
            Color auctionHouseColor = new Color(255, 222, 57);
            
            waitForColor(selectionColor, 322, 400);
            System.out.println("Search Auctions has appeared. Clicking...");
            clickKey(KeyEvent.VK_ENTER); // Click search auctions
            waitForColor(selectionColor, 700, 725);
            System.out.println("Filters have appeared. Clicking...");
            clickKey(KeyEvent.VK_ENTER); // Confirm search filters
            
            waitForColor(auctionHouseColor, 350, 180);
            System.out.println("The auction has loaded.");

            BufferedImage ss = ScreenManager.capture(new Rectangle(screenSize));
            if (isColorAtCoords(ss, selectionColor, 800, 215)) {
                // Buy
                clickKey(KeyEvent.VK_Y); // Open auction options
                waitForColor(selectionColor, 1200, 475);
                System.out.println("Buy out button has appeared. Moving to it...");
                clickKey(KeyEvent.VK_DOWN); // Move to buy out button
                waitForColor(selectionColor, 1200, 525);
                System.out.println("Moved to buy out button. Clicking...");
                clickKey(KeyEvent.VK_ENTER); // Click buy out button
                waitForColor(detailsBarColor, 750, 480);
                System.out.println("Buy out window appeared. Clicking yes...");
                clickKey(KeyEvent.VK_ENTER); // Click yes on buy out confirmation dialog
                waitForColor(detailsBarColor, 750, 518);
                System.out.println("Successful buyout dialog appeared. Closing...");
                clickKey(KeyEvent.VK_ENTER); // Close buyout successful screen
                waitForColor(selectionColor, 750, 550);
                System.out.println("Collect car button appeared. Clicking...");
                clickKey(KeyEvent.VK_ENTER); // Click collect car
                waitForColor(detailsBarColor, 750, 518);
                System.out.println("Successful car claim dialog appeared. Closing...");
                clickKey(KeyEvent.VK_ENTER); // Close successful car claim dialog
                waitForColor(selectionColor, 700, 580);
                System.out.println("Auction options window appeared. Closing...");
                clickKey(KeyEvent.VK_ESCAPE); // Close auction options window
                waitForColor(auctionHouseColor, 350, 180);
                System.out.println("Auction house appeared. Closing...");
                clickKey(KeyEvent.VK_ESCAPE); // Close out of auction house
                
                return; // TODO: REMOVE
                
            } else {
                clickKey(KeyEvent.VK_ESCAPE);
            }
        }
    }
    
    public static boolean isColorAtCoords(BufferedImage img, Color color, int x, int y) {
        Color curColor = new Color(img.getRGB(x, y));
        return curColor.equals(color);
    }
    
    public static void waitForColor(Color color, int x, int y) throws InterruptedException {
        while (true) {
            BufferedImage ss = ScreenManager.capture(new Rectangle(screenSize));
            if (isColorAtCoords(ss, color, x, y)) {
                Thread.sleep(100);
                return;
            }
        }
    }

    public static void oldMain(String[] args) throws AWTException, TesseractException, InterruptedException, IOException {
        robot = new Robot();
        ScreenManager.init(robot);

        //noinspection InfiniteLoopStatement
        while (true) {
            // Main page auction click
            while (true) {
                Thread.sleep(200);

                if (checkOcr(new String[]{"auctioned", "find cars"}, ScreenManager.auctionRect, 3, 100, true)) {
                    System.out.println("Found Search auctions button, clicking..");
                    clickKey(KeyEvent.VK_ENTER);
                    break;
                }
            }

            // Confirm search button click
            while (true) {
                Thread.sleep(200);

                BufferedImage confirmButtonImg = ScreenManager.capture(ScreenManager.searchConfirmRect);
                Color confirmButtonColor = new Color(255, 0, 135);
                if (imgContainsColor(confirmButtonImg, confirmButtonColor)) {
                    System.out.println("Found confirm button, clicking...");
                    clickKey(KeyEvent.VK_ENTER);
                    break;
                }
            }

            // Check for no auctions to display message
            while (true) {
                Thread.sleep(200);

                boolean noAuctions = checkOcr(new String[]{"no", "auctions", "display", "please", "try", "again", "later"}, ScreenManager.noAuctionsRect, 15, 100, false);
                if (noAuctions) {
                    clickKey(KeyEvent.VK_ESCAPE);
                    System.out.println("No auctions found. Escaping...");
                    break;

                } else {
                    System.out.println("Found auction listings. Buying...");
                    clickKey(KeyEvent.VK_Y);
                    Thread.sleep(500);
                    clickKey(KeyEvent.VK_DOWN);
                    Thread.sleep(500);
                    clickKey(KeyEvent.VK_ENTER);
                    Thread.sleep(500);
                    clickKey(KeyEvent.VK_ENTER);

                    Thread.sleep(1000);
                    boolean success = checkOcr(new String[]{"success", "collect", "bids"}, ScreenManager.buyoutSuccessRect, 4, 1000, false);
                    if (success) {
                        System.out.println("Success!");
                        clickKey(KeyEvent.VK_ENTER); // Escape success notification
                        Thread.sleep(500);
                        clickKey(KeyEvent.VK_ENTER); // Click collect car

                        // Wait until successful claim, then exit
                        while (true) {
                            Thread.sleep(200);
                            boolean claimSuccess = checkOcr(new String[]{"success", "claim", "garage", "added"}, ScreenManager.buyoutSuccessRect, 4, 500, false);
                            if (claimSuccess) {
                                Thread.sleep(700);
                                clickKey(KeyEvent.VK_ENTER);
                                Thread.sleep(700);
                                clickKey(KeyEvent.VK_ESCAPE);
                                Thread.sleep(700);
                                clickKey(KeyEvent.VK_ESCAPE);
                                break;
                            }
                        }
                        break;
                    }
                    // TODO: Check for failure here instead of just breaking
                    break;
                }
            }
            
            Thread.sleep(1000);
        }
    }

    public static boolean checkOcr(String[] stringsToFind, Rectangle rectangle, int n, int delay, boolean requireAll) throws TesseractException, InterruptedException, IOException {
        // Repeat OCR check N times
        for (int i = 0; i < n; i++) {
            Rectangle curRect = ScreenManager.randShiftRect(rectangle);

            BufferedImage img = ScreenManager.capture(curRect);
            BufferedImage imgBW = ScreenManager.toBlackWhite(img);

            String ocrText = ScreenManager.getText(imgBW);
            ocrText = ocrText.toLowerCase();

            // TODO: Comment out
            // long curTimeMillis = System.currentTimeMillis();
            // ImageIO.write(imgBW, "jpg", new File("Test-" + curTimeMillis + ".jpg"));
            // System.out.println("Read: \"" + ocrText + "\"" + " on file " + curTimeMillis);

            // Assume that the keyword wasn't found.
            // Check if the text found by the OCR contains any keyword
            // If it is found, set foundKeyword to true
            // If a keyword is found and you don't need all N checks to find a keyword, return true, else continue checking
            boolean foundKeyword = false;
            for (String s : stringsToFind) {
                if (ocrText.contains(s)) {
                    foundKeyword = true;
                }

                // Could move everything inside this if to the if above, but for readability I added this if below
                if (foundKeyword) {
                    if (!requireAll) {
                        return true;
                    }
                    // If you don't need all N checks to find a keyword, return true since this check found a keyword
                    // Else, stop checking for keywords and move on to the next check
                    break;
                }
            }

            // If all checks need to find a keyword and this check didn't find a keyword, return false early
            if (requireAll && !foundKeyword) {
                return false;
            }

            Thread.sleep(delay);
        }

        // If not all checks need to agree, a keyword hasn't been found if it gets to this point, so return false
        if (!requireAll) {
            return false;
        }

        // If it hasn't returned false by this point, it should return true (any false statement would escape this method early)
        return true;
    }

    public static void clickKey(int keyEvent) {
        robot.keyPress(keyEvent);
        robot.keyRelease(keyEvent);
    }

    public static boolean imgContainsColor(BufferedImage img, Color idealColor) {
        int colorDiffThreshold = 5;
        ArrayList<Color> colors = new ArrayList<>();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getRGB(x, y);
                Color col = new Color(color, true);

                boolean alreadyCounted = colors.contains(col);

                if (!alreadyCounted) {
                    for (Color c : colors) {
                        int curRDiff = Math.abs(col.getRed() - c.getRed());
                        int curGDiff = Math.abs(col.getGreen() - c.getGreen());
                        int curBDiff = Math.abs(col.getBlue() - c.getBlue());

                        if (curRDiff < colorDiffThreshold && curGDiff < colorDiffThreshold && curBDiff < colorDiffThreshold) {
                            alreadyCounted = true;
                            break;
                        }
                    }
                }

                if (!alreadyCounted) {
                    colors.add(col);
                }
            }
        }

        for (Color color : colors) {
            int curRDiff = Math.abs(color.getRed() - idealColor.getRed());
            int curGDiff = Math.abs(color.getGreen() - idealColor.getGreen());
            int curBDiff = Math.abs(color.getBlue() - idealColor.getBlue());

            if (curRDiff < colorDiffThreshold && curGDiff < colorDiffThreshold && curBDiff < colorDiffThreshold) {
                return true;
            }
        }

        return false;
    }
}
