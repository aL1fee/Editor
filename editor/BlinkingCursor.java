package editor;

import javafx.scene.shape.Rectangle;

import java.util.Timer;
import java.util.TimerTask;

public class BlinkingCursor {
    private Timer timer;
    private Rectangle cursor;
    private int indicator;

    public BlinkingCursor(Rectangle cursor) {
        timer = new Timer();
        this.cursor = cursor;
        timer.schedule(new RepeatTask(), 0, 500);
    }

    class RepeatTask extends TimerTask {
        @Override
        public void run() {
            if (indicator == 0) {
                cursor.setVisible(false);
                indicator = 1;
                return;
            }
            indicator = 0;
            cursor.setVisible(true);
        }
    }
}