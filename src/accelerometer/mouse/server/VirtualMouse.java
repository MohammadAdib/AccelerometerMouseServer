package accelerometer.mouse.server;

import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class VirtualMouse {

    private float[] values;
    private final float g = 9.80665f;
    public static int cursorSensitivity = 20, scrollSensitivity = 50;
    public static boolean middleMouse = true;
    public float xOffset = 0, yOffset = 0, prevXOffset = 0, prevYOffset = 0;
    private boolean running = false, pauseFlag = false, leftClickFlag = false, rightClickFlag = false, middleFlag = false, scrollFlag = false, setLeftFlag = false, setRightFlag = false, setMiddleFlag = false, sudoPause = false;
    private Robot robot;

    public VirtualMouse() {
    }

    public void start() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                running = true;
                try {
                    robot = new Robot();
                } catch (Exception e) {
                }
                while (running) {
                    try {
                        if(sudoPause) pauseFlag = true;
                        if (!pauseFlag && !TCPServer.paused) {
                            xOffset = getMouseAcceleration(values[1]);
                            yOffset = -getMouseAcceleration(values[0] - (g / 2.0f));
                            if (Math.abs(prevXOffset) < 1) {
                                xOffset += prevXOffset;
                            }
                            if (Math.abs(prevYOffset) < 1) {
                                yOffset += prevYOffset;
                            }
                            //Left click
                            if (leftClickFlag != setLeftFlag) {
                                if (leftClickFlag) {
                                    robot.mousePress(InputEvent.BUTTON1_MASK);
                                } else {
                                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                }
                                setLeftFlag = leftClickFlag;
                            }
                            //Right click
                            if (rightClickFlag != setRightFlag) {
                                if (rightClickFlag) {
                                    robot.mousePress(InputEvent.BUTTON3_MASK);
                                } else {
                                    robot.mouseRelease(InputEvent.BUTTON3_MASK);
                                }
                                setRightFlag = rightClickFlag;
                            }
                            //Middle click
                            if (middleFlag != setMiddleFlag && middleMouse) {
                                if (middleFlag) {
                                    robot.mousePress(InputEvent.BUTTON2_MASK);
                                } else {
                                    robot.mouseRelease(InputEvent.BUTTON2_MASK);
                                }
                                setMiddleFlag = middleFlag;
                            }
                            //Scroll
                            if (scrollFlag) {
                                if (values[0] > (g / 2 + g / 10)) {
                                    robot.mouseWheel(-1 * (1 + (scrollSensitivity / 100)));
                                }
                                if (values[0] < (g / 2 - g / 10)) {
                                    robot.mouseWheel(1 * (1 + (scrollSensitivity / 100)));
                                }
                                Thread.sleep(50);
                            } else {
                                robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x + (int) xOffset, MouseInfo.getPointerInfo().getLocation().y + (int) yOffset);
                            }
                            prevXOffset = xOffset;
                            prevYOffset = yOffset;
                        }
                        if(TCPServer.paused){
                            if(middleFlag) {
                                robot.mouseRelease(InputEvent.BUTTON2_MASK);
                                middleFlag = false;
                                setMiddleFlag = false;
                            }
                        }
                        Thread.sleep(10);
                    } catch (Exception e) {
                    }
                }
            }
        };
        new Thread(r).start();
    }

    public void pause(boolean flag) {
        this.pauseFlag = flag;
    }
    
    public void sudoPause(boolean flag) {
        this.sudoPause = flag;
    }

    public void feedAccelerometerValues(float[] values) {
        this.values = values;
    }

    public void feedTouchFlags(boolean leftClickFlag, boolean rightClickFlag, boolean middleFlag, boolean scrollFlag) {
        this.leftClickFlag = leftClickFlag;
        this.rightClickFlag = rightClickFlag;
        this.middleFlag = middleFlag;
        this.scrollFlag = scrollFlag;
    }

    private float getMouseAcceleration(float f) {
        return (((cursorSensitivity / g) * f));
    }

    public void stop() {
        running = false;
    }
}
