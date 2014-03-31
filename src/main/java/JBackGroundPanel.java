import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Mladjan on 31.3.2014.
 */
public class JBackGroundPanel extends JPanel {
    private BufferedImage img;

    public JBackGroundPanel() {
        // load the background image
        try {
            img = ImageIO.read(new File("C:\\Users\\Mladjan\\Desktop\\IDEAProject\\MaxBetLotto\\src\\main\\resources\\lotto.png"));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
    }
}
