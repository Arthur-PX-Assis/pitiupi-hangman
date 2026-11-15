
package pitiupi.hangman.ui;

import javax.swing.*;
import java.awt.*;

public class HangmanSpinner extends JComponent{

    private static final int FRAME_DELAY = 80;
    private static final int SPOKES = 12;

    private final Timer timer;
    private int currentFrame = 0;
    private boolean running = false;

    public HangmanSpinner(){
        setPreferredSize(new Dimension(48, 48));
        setMinimumSize(new Dimension(48, 48));
        setOpaque(false);

        timer = new Timer(FRAME_DELAY, event ->{
            currentFrame = (currentFrame + 1) % SPOKES;
            repaint();
        });
    }

    public void start(){
        if (running) return;

        running = true;
        timer.start();
        repaint();
    }

    public void stop(){
        running = false;
        timer.stop();
        repaint();
    }

    public boolean isRunning(){
        return running;
    }

    @Override
    protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);

        Graphics2D g2 = (Graphics2D) graphics.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        int outerRadius = Math.min(getWidth(), getHeight()) / 2 - 5;
        int innerRadius = outerRadius / 2;

        for (int i = 0; i < SPOKES; i++){
            int distance = (i - currentFrame + SPOKES) % SPOKES;

            int alpha = running
                ? 40 + (SPOKES - distance) * 215 / SPOKES
                : 120;

            alpha = Math.min(255, alpha);

            double angle = 2 * Math.PI * i / SPOKES;

            int x1 = centerX + (int) (Math.cos(angle) * innerRadius);
            int y1 = centerY + (int) (Math.sin(angle) * innerRadius);

            int x2 = centerX + (int) (Math.cos(angle) * outerRadius);
            int y2 = centerY + (int) (Math.sin(angle) * outerRadius);

            g2.setColor(new Color(70, 130, 200, alpha));
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.dispose();
    }

    @Override
    public void removeNotify(){
        stop();
        super.removeNotify();
    }
}
