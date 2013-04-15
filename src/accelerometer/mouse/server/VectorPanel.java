package accelerometer.mouse.server;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

public class VectorPanel extends JPanel {

    private int height = -1, width;
    private float xOffset, yOffset;
    private int xmin = -10, xmax = 10, ymin = -10, ymax = 10;

    public VectorPanel() {
        this.setDoubleBuffered(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            if (height > 0) {
                int x = (int) ((1.0 * xOffset / (xmax - xmin)) * width) + (int) getOriginX();
                int y = (int) ((1.0 * yOffset / (ymax - ymin)) * height) + (int) getOriginY();
                g.setColor(Color.RED);
                //Draw Vector
                g.drawLine((int) getOriginX(), (int) getOriginY(), x, y);
                g.fillOval(x - 3, y - 3, 6, 6);
                //Draw Axis
                g.setColor(Color.BLACK);
                g.drawLine(0, (int) getOriginY(), width, (int) getOriginY());
                g.drawLine((int) getOriginX(), 0, (int) getOriginX(), height);
                //Draw labels
                g.setColor(Color.LIGHT_GRAY);
                g.drawString(xmin + "", 0, (int) getOriginY() - 1);
                g.drawString(xmax + "", width - 12, (int) getOriginY() + 11);
                g.drawString(ymin + "", (int) getOriginX() - 16, 11);
                g.drawString(ymax + "", (int) getOriginX() + 1, height);
                //Finish drawing
                g.dispose();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void clear() {
        this.getGraphics().clearRect(0, 0, width, height);
    }

    private double getOriginX() {
        return ((-1.0 * xmin / (xmax - xmin)) * width);
    }

    private double getOriginY() {
        return ((-1.0 * ymin / (ymax - ymin)) * height);
    }

    public void feedOffsetValues(float xOffset, float yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        if (xOffset > xmax) {
            xmax = (int) xOffset;
        }
        if (xOffset < xmin) {
            xmin = (int) xOffset;
        }
        if (yOffset > ymax) {
            ymax = (int) yOffset;
        }
        if (yOffset < ymin) {
            ymin = (int) yOffset;
        }
    }

    public void updateValues(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
