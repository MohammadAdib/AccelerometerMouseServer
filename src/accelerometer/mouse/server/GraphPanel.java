package accelerometer.mouse.server;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import javax.swing.JPanel;

public class GraphPanel extends JPanel {

    private int range, height, packPerSec;
    private boolean[] flags;
    private TCPServer server;

    public GraphPanel() {
        this.setDoubleBuffered(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            String[] messages = server.getAllMessages();
            int x = 0, y = 0;
            double u = 2 * -(9.80665);
            g.setColor(Color.LIGHT_GRAY);                      
            g.drawLine(0, height / 4, range, height / 4);                        
            g.drawLine(0, 3 * height / 4, range, 3 * height / 4);
            g.drawString("+2G", range - 23, 10);
            g.drawString("0G", range - 16, height / 2 - 5);            
            g.drawString("-2G", range - 20, height);                  
            g.drawString(packPerSec + " packets/sec", 0, height);
            LinkedList<Integer> yValues = new LinkedList<Integer>();
            //
            if (flags[0]) {
                g.setColor(Color.RED);
                for (String s : messages) {
                    x++;
                    float f = Float.parseFloat(s.split(",")[0]);
                    y = (int) -(height * (((1 / (2 * u)) * f) - (u / (2 * u))) + 0.5);
                    yValues.add(y);
                }
                if (yValues.size() > range) {
                    for (int i = yValues.size() - 1; i > (yValues.size() - range); i += -1) {
                        g.drawLine(i - (yValues.size() - range), yValues.get(i), i - (yValues.size() - range) - 1, yValues.get(i - 1));
                    }
                } else {
                    for (int i = 1; i < yValues.size(); i++) {
                        g.drawLine(i - 1, yValues.get(i - 1), i, yValues.get(i));
                    }
                }
            }
            //
            if (flags[1]) {
                g.setColor(Color.GREEN);
                yValues.clear();
                for (String s : messages) {
                    x++;
                    float f = Float.parseFloat(s.split(",")[1]);
                    y = (int) -(height * (((1 / (2 * u)) * f) - (u / (2 * u))) + 0.5);
                    yValues.add(y);
                }
                if (yValues.size() > range) {
                    for (int i = yValues.size() - 1; i > (yValues.size() - range); i += -1) {
                        g.drawLine(i - (yValues.size() - range), yValues.get(i), i - (yValues.size() - range) - 1, yValues.get(i - 1));
                    }
                } else {
                    for (int i = 1; i < yValues.size(); i++) {
                        g.drawLine(i - 1, yValues.get(i - 1), i, yValues.get(i));
                    }
                }
            }
            //
            if (flags[2]) {
                g.setColor(Color.BLUE);
                yValues.clear();
                for (String s : messages) {
                    x++;
                    float f = Float.parseFloat(s.split(",")[2]);
                    y = (int) -(height * (((1 / (2 * u)) * f) - (u / (2 * u))) + 0.5);
                    yValues.add(y);
                }
                if (yValues.size() > range) {
                    for (int i = yValues.size() - 1; i > (yValues.size() - range); i += -1) {
                        g.drawLine(i - (yValues.size() - range), yValues.get(i), i - (yValues.size() - range) - 1, yValues.get(i - 1));
                    }
                } else {
                    for (int i = 1; i < yValues.size(); i++) {
                        g.drawLine(i - 1, yValues.get(i - 1), i, yValues.get(i));
                    }
                }
            }
            //Draw axis
            g.setColor(Color.BLACK);
            g.drawLine(0, height / 2, range, height / 2);
            g.drawLine(range - 1, 0, range - 1, height);
            g.drawLine(messages.length - 1, 1, messages.length - 1, height);  
            g.dispose();
        } catch (Exception e) {
        }
    }

    public void setInfo(TCPServer server, int range, int height, boolean[] flags, int packPerSec) {
        this.server = server;
        this.range = range;
        this.height = height;
        this.flags = flags;
        this.packPerSec = packPerSec;
    }
}
