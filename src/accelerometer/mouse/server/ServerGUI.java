package accelerometer.mouse.server;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.InetAddress;
import java.util.LinkedList;
import javax.swing.*;

public class ServerGUI extends JFrame {

    private TCPServer tcpServer;
    private UDPServer udpServer;
    private GraphPanel graphPanel;
    private VirtualMouse mouse;
    private VectorPanel vectorPanel;

    public ServerGUI() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                }
            }
        }
        catch (Exception e) {
        }
        initComponents();
        setCenterScreen();
        tabbedPane.removeTabAt(2);
        tabbedPane.removeTabAt(1);
        graphPanel = new GraphPanel();
        vectorPanel = new VectorPanel();
        accelTab.invalidate();
        mouseActivityTab.invalidate();
        //
        //Graph
        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
        chartPanel.setLayout(chartPanelLayout);
        chartPanelLayout.setHorizontalGroup(
                chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(graphPanel).addGap(0, 349, Short.MAX_VALUE));
        chartPanelLayout.setVerticalGroup(
                chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(graphPanel).addGap(0, 160, Short.MAX_VALUE));
        //
        //Vector
        javax.swing.GroupLayout cursorVectorPanelLayout = new javax.swing.GroupLayout(cursorVectorPanel);
        cursorVectorPanel.setLayout(cursorVectorPanelLayout);
        cursorVectorPanelLayout.setHorizontalGroup(
                cursorVectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(vectorPanel).addGap(0, 349, Short.MAX_VALUE));
        cursorVectorPanelLayout.setVerticalGroup(
                cursorVectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(vectorPanel).addGap(0, 160, Short.MAX_VALUE));
        //
        //
        this.setMinimumSize(this.getSize());
        tcpServer = new TCPServer("0.0.0.0", 18250);
        tcpServer.start();
        udpServer = new UDPServer();
        udpServer.start();
        mouse = new VirtualMouse();
        Runnable r = new Runnable() {

            public void run() {

                try {
                    InetAddress localhost = InetAddress.getLocalHost();
                    LinkedList<String> ips = new LinkedList<String>();
                    InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getHostName());
                    if (allMyIps != null && allMyIps.length > 1) {
                        for (int i = 0; i < allMyIps.length; i++) {
                            if (!allMyIps[i].toString().contains(":")) {
                                ips.add(allMyIps[i].toString().substring(allMyIps[i].toString().indexOf("/") + 1));
                                System.out.println(allMyIps[i].toString().substring(allMyIps[i].toString().indexOf("/") + 1));
                                ipCB.setModel(new javax.swing.DefaultComboBoxModel(ips.toArray()));
                            }
                        }
                    }
                    ips.add("Listen on all");
                    ipCB.setModel(new javax.swing.DefaultComboBoxModel(ips.toArray()));
                }
                catch (Exception e) {
                }

                try {
                    mouse.start();
                    while (tcpServer.isRunning()) {
                        if (tcpServer.isConnected()) {
                            if (tcpServer.paused) {
                                if (tabbedPane.getTabCount() > 2) {
                                    tabbedPane.removeTabAt(2);
                                    tabbedPane.removeTabAt(1);
                                }
                            }
                            else if (tabbedPane.getTabCount() < 2) {
                                tabbedPane.addTab("Accelerometer Values", accelTab);
                                tabbedPane.addTab("Mouse Activity", mouseActivityTab);
                            }
                            mouse.pause(false);
                            float[] values = new float[]{
                                Float.parseFloat(tcpServer.getLastMessage().split(",")[0]),
                                Float.parseFloat(tcpServer.getLastMessage().split(",")[1]),
                                Float.parseFloat(tcpServer.getLastMessage().split(",")[2])
                            };
                            boolean leftClickFlag = Boolean.parseBoolean(tcpServer.getLastMessage().split(",")[3]);
                            boolean rightClickFlag = Boolean.parseBoolean(tcpServer.getLastMessage().split(",")[4]);
                            boolean middleFlag = Boolean.parseBoolean(tcpServer.getLastMessage().split(",")[5]);
                            boolean scrollFlag = Boolean.parseBoolean(tcpServer.getLastMessage().split(",")[6]);
                            if (leftClickFlag) {
                                mousePanel.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/accelerometer/mouse/server/mouseLeft.png")));
                            }
                            if (rightClickFlag) {
                                mousePanel.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/accelerometer/mouse/server/mouseRight.png")));
                            }
                            if (rightClickFlag && leftClickFlag) {
                                mousePanel.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/accelerometer/mouse/server/mouseLeftRight.png")));
                            }
                            if (!rightClickFlag && !leftClickFlag) {
                                mousePanel.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/accelerometer/mouse/server/mouseNeurtal.png")));
                            }
                            mouse.feedAccelerometerValues(values);
                            mouse.feedTouchFlags(leftClickFlag, rightClickFlag, middleFlag, scrollFlag);
                            applyButton.setEnabled(false);
                            updateGraphs();
                        }
                        else {
                            mouse.pause(true);
                            applyButton.setEnabled(true);
                            xProgressBar.setValue(0);
                            yProgressBar.setValue(0);
                            zProgressBar.setValue(0);
                            mousePanel.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/accelerometer/mouse/server/mouseNeurtal.png")));
                        }
                        setServerStatus(tcpServer.serverStatus);
                        setClientStatus(tcpServer.clientStatus);
                        Thread.sleep(20);
                    }
                }
                catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        new Thread(r).start();
        Runnable netRefresh = new Runnable() {

            @Override
            public void run() {
                while (tcpServer.isRunning()) {
                    try {
                        InetAddress localhost = InetAddress.getLocalHost();
                        LinkedList<String> ips = new LinkedList<String>();
                        InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getHostName());
                        if (allMyIps != null && allMyIps.length > 1) {
                            for (int i = 0; i < allMyIps.length; i++) {
                                if (!allMyIps[i].toString().contains(":")) {
                                    ips.add(allMyIps[i].toString().substring(allMyIps[i].toString().indexOf("/") + 1));
                                    ipCB.setModel(new javax.swing.DefaultComboBoxModel(ips.toArray()));
                                }
                            }
                            ips.add("Listen on all");
                            ipCB.setModel(new javax.swing.DefaultComboBoxModel(ips.toArray()));
                        }
                        ips.add("Listen on all");
                        Thread.sleep(10000);
                    }
                    catch (Exception e) {
                    }
                }
            }
        };
        new Thread(netRefresh).start();
    }

    private void setServerStatus(String s) {
        serverStatusLabel.setText(s);
    }

    private void setClientStatus(String s) {
        clientStatusLabel.setText(s);
    }

    private void setCenterScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int locX = dim.width / 2 - this.getWidth() / 2;
        int locY = dim.height / 2 - this.getHeight() / 2;
        this.setLocation(locX, locY);
    }

    private int getProgress(float f) {
        return (int) ((f * 100.0f) + 0.5f);
    }

    protected void updateGraphs() {
        if (tabbedPane.getSelectedIndex() == 1) {
            graphPanel.setInfo(tcpServer, graphPanel.getWidth(), graphPanel.getHeight(), new boolean[]{xCB.isSelected(), yCB.isSelected(), zCB.isSelected()}, tcpServer.packetsPerSec);
            graphPanel.repaint();
            xProgressBar.setValue(getProgress(-Float.parseFloat(tcpServer.getLastMessage().split(",")[0])));
            yProgressBar.setValue(getProgress(-Float.parseFloat(tcpServer.getLastMessage().split(",")[1])));
            zProgressBar.setValue(getProgress(-Float.parseFloat(tcpServer.getLastMessage().split(",")[2])));
        }
        else if (tabbedPane.getSelectedIndex() == 2) {
            vectorPanel.feedOffsetValues(mouse.xOffset, mouse.yOffset);
            vectorPanel.updateValues(vectorPanel.getWidth(), vectorPanel.getHeight());
            vectorPanel.repaint();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        serverConfigTab = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        ipCB = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        portNUD = new javax.swing.JSpinner();
        applyButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        sensitivitySlider1 = new javax.swing.JSlider();
        sensitivitySlider2 = new javax.swing.JSlider();
        jLabel5 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        serverStatusLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        clientStatusLabel = new javax.swing.JLabel();
        discoverToggle = new javax.swing.JToggleButton();
        playButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        accelTab = new javax.swing.JPanel();
        xProgressBar = new javax.swing.JProgressBar();
        yProgressBar = new javax.swing.JProgressBar();
        zProgressBar = new javax.swing.JProgressBar();
        xCB = new javax.swing.JCheckBox();
        yCB = new javax.swing.JCheckBox();
        zCB = new javax.swing.JCheckBox();
        chartPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        mouseActivityTab = new javax.swing.JPanel();
        mousePanel = new javax.swing.JButton();
        cursorVectorPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Accelerometer Mouse Server");
        setName("gui"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                Closing(evt);
            }
        });

        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Server IP:");

        ipCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "255.255.255.255" }));

        jLabel12.setText("Port:");

        portNUD.setModel(new javax.swing.SpinnerNumberModel(18250, 1, 65535, 1));

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ipCB, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(portNUD, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(applyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(ipCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portNUD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(applyButton)
                    .addComponent(jLabel12))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Sensitivity"));

        sensitivitySlider1.setMajorTickSpacing(2);
        sensitivitySlider1.setPaintTicks(true);
        sensitivitySlider1.setValue(20);
        sensitivitySlider1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cursorSensitivity(evt);
            }
        });

        sensitivitySlider2.setMajorTickSpacing(2);
        sensitivitySlider2.setPaintTicks(true);
        sensitivitySlider2.setSnapToTicks(true);
        sensitivitySlider2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                scrollSensitivity(evt);
            }
        });

        jLabel5.setText("Cursor");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Scroll");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sensitivitySlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(sensitivitySlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(sensitivitySlider1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(sensitivitySlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setText("Server Status: ");

        serverStatusLabel.setForeground(new java.awt.Color(51, 204, 0));
        serverStatusLabel.setText("Listening");

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Client Status: ");

        clientStatusLabel.setForeground(new java.awt.Color(255, 0, 1));
        clientStatusLabel.setText("None Connected");

        discoverToggle.setSelected(true);
        discoverToggle.setText("Discoverable");
        discoverToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discoverToggleActionPerformed(evt);
            }
        });

        playButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/accelerometer/mouse/server/play.png"))); // NOI18N
        playButton.setEnabled(false);
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playButtonActionPerformed(evt);
            }
        });

        pauseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/accelerometer/mouse/server/pause.png"))); // NOI18N
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(10, 10, 10)
                        .addComponent(clientStatusLabel))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverStatusLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                .addComponent(discoverToggle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(playButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pauseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(serverStatusLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(clientStatusLabel)))
                    .addComponent(discoverToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(playButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(pauseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout serverConfigTabLayout = new javax.swing.GroupLayout(serverConfigTab);
        serverConfigTab.setLayout(serverConfigTabLayout);
        serverConfigTabLayout.setHorizontalGroup(
            serverConfigTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serverConfigTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(serverConfigTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        serverConfigTabLayout.setVerticalGroup(
            serverConfigTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serverConfigTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Server Configuration", serverConfigTab);

        xProgressBar.setMaximum(2000);
        xProgressBar.setMinimum(-2000);

        yProgressBar.setMaximum(2000);
        yProgressBar.setMinimum(-2000);

        zProgressBar.setMaximum(2000);
        zProgressBar.setMinimum(-2000);

        xCB.setForeground(new java.awt.Color(255, 0, 0));
        xCB.setSelected(true);
        xCB.setText("X-Axis");

        yCB.setForeground(new java.awt.Color(0, 255, 51));
        yCB.setSelected(true);
        yCB.setText("Y-Axis");

        zCB.setForeground(new java.awt.Color(0, 0, 255));
        zCB.setSelected(true);
        zCB.setText("Z-Axis");

        chartPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Charts"));

        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
        chartPanel.setLayout(chartPanelLayout);
        chartPanelLayout.setHorizontalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 412, Short.MAX_VALUE)
        );
        chartPanelLayout.setVerticalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 128, Short.MAX_VALUE)
        );

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("0G");

        jLabel6.setText("+2G");

        jLabel7.setText("-2G");

        javax.swing.GroupLayout accelTabLayout = new javax.swing.GroupLayout(accelTab);
        accelTab.setLayout(accelTabLayout);
        accelTabLayout.setHorizontalGroup(
            accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(accelTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chartPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(accelTabLayout.createSequentialGroup()
                        .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(zCB)
                            .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(xCB)
                                .addComponent(yCB)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(zProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addComponent(yProgressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addComponent(xProgressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, accelTabLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)))))
                .addContainerGap())
        );
        accelTabLayout.setVerticalGroup(
            accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(accelTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2)
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(yProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2)
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(zProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5)
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Accelerometer Analysis", accelTab);

        mousePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/accelerometer/mouse/server/mouseNeurtal.png"))); // NOI18N
        mousePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mousePanel.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/accelerometer/mouse/server/mouseNeurtal.png"))); // NOI18N
        mousePanel.setDoubleBuffered(true);
        mousePanel.setEnabled(false);
        mousePanel.setRequestFocusEnabled(false);
        mousePanel.setRolloverEnabled(false);
        mousePanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mousePanelActionPerformed(evt);
            }
        });

        cursorVectorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Cursor Vector"));
        cursorVectorPanel.setPreferredSize(new java.awt.Dimension(235, 235));

        javax.swing.GroupLayout cursorVectorPanelLayout = new javax.swing.GroupLayout(cursorVectorPanel);
        cursorVectorPanel.setLayout(cursorVectorPanelLayout);
        cursorVectorPanelLayout.setHorizontalGroup(
            cursorVectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 231, Short.MAX_VALUE)
        );
        cursorVectorPanelLayout.setVerticalGroup(
            cursorVectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 226, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mouseActivityTabLayout = new javax.swing.GroupLayout(mouseActivityTab);
        mouseActivityTab.setLayout(mouseActivityTabLayout);
        mouseActivityTabLayout.setHorizontalGroup(
            mouseActivityTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mouseActivityTabLayout.createSequentialGroup()
                .addComponent(mousePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cursorVectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                .addContainerGap())
        );
        mouseActivityTabLayout.setVerticalGroup(
            mouseActivityTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mousePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
            .addGroup(mouseActivityTabLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(cursorVectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Mouse Activity", mouseActivityTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void Closing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_Closing
        tcpServer.stop();
    }//GEN-LAST:event_Closing

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        tcpServer.stop();
        String ip = ipCB.getSelectedItem().toString();
        if (ipCB.getSelectedItem().toString().contains("Listen on all")) {
            ip = "0.0.0.0";
        }
        tcpServer = new TCPServer(ip, Integer.valueOf(portNUD.getValue().toString()));
        tcpServer.start();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void cursorSensitivity(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cursorSensitivity
        VirtualMouse.cursorSensitivity = this.sensitivitySlider1.getValue();
    }//GEN-LAST:event_cursorSensitivity

    private void scrollSensitivity(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scrollSensitivity
        VirtualMouse.scrollSensitivity = this.sensitivitySlider2.getValue();
    }//GEN-LAST:event_scrollSensitivity

    private void mousePanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mousePanelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mousePanelActionPerformed

    private void discoverToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discoverToggleActionPerformed
        udpServer.setDiscoverable(discoverToggle.isSelected());
    }//GEN-LAST:event_discoverToggleActionPerformed

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        mouse.sudoPause(true);
        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
    }//GEN-LAST:event_pauseButtonActionPerformed

    private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
        mouse.sudoPause(false);
        playButton.setEnabled(false);
        pauseButton.setEnabled(true);
    }//GEN-LAST:event_playButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel accelTab;
    private javax.swing.JButton applyButton;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JLabel clientStatusLabel;
    private javax.swing.JPanel cursorVectorPanel;
    private javax.swing.JToggleButton discoverToggle;
    private javax.swing.JComboBox ipCB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel mouseActivityTab;
    private javax.swing.JButton mousePanel;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton playButton;
    private javax.swing.JSpinner portNUD;
    private javax.swing.JSlider sensitivitySlider1;
    private javax.swing.JSlider sensitivitySlider2;
    private javax.swing.JPanel serverConfigTab;
    private javax.swing.JLabel serverStatusLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JCheckBox xCB;
    private javax.swing.JProgressBar xProgressBar;
    private javax.swing.JCheckBox yCB;
    private javax.swing.JProgressBar yProgressBar;
    private javax.swing.JCheckBox zCB;
    private javax.swing.JProgressBar zProgressBar;
    // End of variables declaration//GEN-END:variables
}
