package editor;

import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;

public class ScrollingBar extends ScrollBar {

    private double value;
    private double min;
    private double max;
    private ScrollBar scroll;
    private int windowHeight;
    private int windowWidth;

    public ScrollingBar(int windowHeight, int windowWidth) {
        scroll = new ScrollBar();
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;
        min = 0;
        max = 0;
        value = 0;
        run();
    }

    private void run() {
        scroll.setOrientation(Orientation.VERTICAL);
        scroll.setPrefHeight(windowHeight);
        scroll.setMin(min);
        scroll.setMax(max);

        double usableScreenWidth = windowWidth - scroll.getLayoutBounds().getWidth() + 3;
        scroll.setLayoutX(usableScreenWidth);
    }

    public double getMinVal() {
        return min;
    }

    public double getMaxVal() {
        return max;
    }

    public ScrollBar scroll() {
        return scroll;
    }
}