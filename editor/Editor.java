package editor;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;


public class Editor extends Application {

    private Group root;
    private Group textRoot;
    private Scene scene;
    private File inputFile;

    private editor.ScrollingBar scroll;
    private editor.LinkedListDeque<Text> bufferNew;
    private editor.LinkedListDeque<Text> buffer;
    private editor.LinkedListStack<Text> undoStack;
    private editor.LinkedListStack<Text> redoStack;

    private int fontSize = 12;
    private int numberOfLines;
    private int lastCharHeight = fontSize;
    private String fontName = "Verdana";

    private Rectangle cursor = new Rectangle(1, 1, Color.BLACK);

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {

        double mousePressedX;
        double mousePressedY;

        MouseClickEventHandler(Group root) {}

        @Override
        public void handle(MouseEvent mouseEvent) {

            int sceneHeight = (int) Math.round(scene.getHeight());
            double totalPixs = numberOfLines * fontSize;
            double max = scroll.scroll().getMax();
            double value = scroll.scroll().getValue();
            double screenTop = value / max * (totalPixs - sceneHeight);

            mousePressedX = mouseEvent.getX();
            mousePressedY = mouseEvent.getY() + screenTop;
            Text newCursorText = null;
            double minDeltaX = (int) Math.round(scene.getWidth());
            double cursorPosX = mousePressedX;
            double cursorPosY;
            int lines = ((int) Math.round(mousePressedY)) / fontSize;
            if (lines % fontSize >= fontSize / 2) {
                cursorPosY = lines * fontSize;
            } else {
                cursorPosY = lines * fontSize;
            }
            int indicator = 0;
            for (Text text: buffer) {
                if (text.getY() == cursorPosY) {
                    if (text.getX() > mousePressedX) {
                        indicator = 1;
                    }
                    if (Math.abs(text.getX() - mousePressedX) <  minDeltaX) {
                        minDeltaX = Math.min(minDeltaX, Math.abs(text.getX() - mousePressedX));
                        newCursorText = text;
                        cursorPosX = text.getX();
                    }
                }
            }
            if (indicator == 0) {
                for (Text text: buffer) {
                    if (text.getY() == cursorPosY) {
                        newCursorText = text;
                        cursorPosX = text.getX();
                    }
                }
                buffer.setCurrentNodeAfter(newCursorText);
                buffer.moveCurrentRight();
                cursor.setHeight(lastCharHeight);
                cursor.setX(cursorPosX + (int) Math.round(newCursorText.getLayoutBounds().getWidth()));
                cursor.setY(cursorPosY);
                return;
            }
            if (newCursorText.getX() == 5) {
                buffer.setCurrentNodeAfter(newCursorText);
                buffer.moveCurrentRight();
                cursor.setHeight(lastCharHeight);
                cursor.setX(5);
                cursor.setY(cursorPosY);
                return;
            }
            buffer.setCurrentNodeAfter(newCursorText);
            cursor.setHeight(lastCharHeight);
            cursor.setX(cursorPosX);
            cursor.setY(cursorPosY);
        }
    }


    public double oldTotalPixs = 0;

    private class KeyEventHandler implements EventHandler<KeyEvent> {
        int textCenterX;
        int textCenterY;

        int maxHeight = 0;

        Group root;

        public KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            textCenterX = 5;
            textCenterY = 0;
            this.root = root;
        }

        @Override
        public void handle(KeyEvent keyEvent) {


            int sceneHeight = (int) Math.round(scene.getHeight());
            double cursorPosY = cursor.getY();

            double totalPixs = numberOfLines * fontSize;
            double max = scroll.scroll().getMax();
            double value = scroll.scroll().getValue();

            double screenTop = value / max * (totalPixs - sceneHeight);
            double screenBottom = screenTop + sceneHeight;

            if (cursorPosY >= screenBottom - (value / max) * fontSize) {
                scroll.scroll().setMax(numberOfLines * fontSize);
                scroll.scroll().setValue(scroll.scroll().getMax());
            }

            if (totalPixs > oldTotalPixs) {
                scroll.scroll().setMax(numberOfLines * fontSize);
            }

            if (cursorPosY <= screenTop) {
                scroll.scroll().setValue(cursorPosY / totalPixs * value);
            }

            oldTotalPixs = totalPixs;

            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {

                String characterTyped = keyEvent.getCharacter();
                int charWidth;
                int charHeight;

                int sceneWidth = (int) Math.round(scene.getWidth());
                if (textCenterX < 5) {
                    textCenterX = 5;
                }
                if (characterTyped.length() > 0 && characterTyped.charAt(0) == '\r') {
                    Text newLine = new Text("\n");
                    buffer.addLast(newLine);
                    undoStack.push(newLine);
                    redoStack = new editor.LinkedListStack();
                    root.getChildren().add(newLine);
                    // if (numberOfLines * fontSize > (int) Math.round(scene.getHeight()) && scroll.scroll().getMax() == 0) {
                    //     scroll.scroll().setMax(100);
                    // }
                    render();
                    if (maxHeight == 0) {
                        textCenterX = 5;
                        textCenterY += lastCharHeight;
                        cursor.setHeight(fontSize);
                        cursor.setX(textCenterX);
                        cursor.setY(textCenterY);
                    } else {
                        textCenterX = 5;
                        textCenterY += maxHeight;
                        cursor.setHeight(maxHeight);
                        cursor.setX(textCenterX);
                        cursor.setY(newLine.getY());
                    }
                }

                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8 && 
                        characterTyped.charAt(0) != '\r' && !keyEvent.isShortcutDown()) {

                    // if (numberOfLines * fontSize > (int) Math.round(scene.getHeight()) && scroll.scroll().getMax() == 0) {
                    //     scroll.scroll().setMax(100);
                    // }

                    Text textTyped = new Text(textCenterX, textCenterY, characterTyped);
                    buffer.addLast(textTyped);
                    undoStack.push(textTyped);
                    redoStack = new editor.LinkedListStack();
                    Text textToAdd = buffer.getCurrentItem();
                    textToAdd.setTextOrigin(VPos.TOP);
                    textToAdd.setFont(Font.font (fontName, fontSize));
                    root.getChildren().add(textToAdd);

                    render();

                    charWidth = (int) Math.round(textToAdd.getLayoutBounds().getWidth());
                    charHeight = (int) Math.round(textToAdd.getLayoutBounds().getHeight());

                    double x = textToAdd.getX();
                    double y = textToAdd.getY();

                    cursor.setHeight(charHeight);
                    cursor.setX(x + charWidth);
                    cursor.setY(y);

                    root.getChildren().remove(cursor);
                    root.getChildren().add(cursor);

                    textCenterX += charWidth;

                    maxHeight = Math.max(maxHeight, charHeight);

                    lastCharHeight = charHeight;

                    keyEvent.consume();
                }

            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {

                KeyCode code = keyEvent.getCode();

                if (keyEvent.isShortcutDown()) {
                    if (keyEvent.getCode() == KeyCode.P) {
                        int x = (int) Math.round(cursor.getX());
                        int y = (int) Math.round(cursor.getY());
                        System.out.println(x + ", " + y);
                    }
                    if (keyEvent.getCode() == KeyCode.S) {
                        try {
                            FileWriter writer = new FileWriter(inputFile);
                            for (Text text : buffer) {
                                String textString = text.getText();
                                writer.write(textString, 0, textString.length());
                            }
                            writer.close();
                        } catch (FileNotFoundException fileNotFoundException) {
                            System.out.println("File not found! Exception was: " + fileNotFoundException);
                        } catch (IOException ioException) {
                            System.out.println("Error when copying; exception was: " + ioException);
                        }
                    }
                    if (keyEvent.getCode() == KeyCode.PLUS || keyEvent.getCode() == KeyCode.EQUALS) {
                        fontSize += 4;
                        for (Text text : buffer) {
                            text.setFont(new Font(fontName, fontSize));
                        }
                        render();
                    }
                    if (keyEvent.getCode() == KeyCode.MINUS) {
                        if (fontSize == 4) {
                            return;
                        }
                        fontSize -= 4;
                        for (Text text : buffer) {
                            text.setFont(new Font(fontName, fontSize));
                        }
                        render();
                    }
                    if (keyEvent.getCode() == KeyCode.Z) {
                        if (!undoStack.isEmpty()) {
                            Text undoItem = undoStack.pop();
                            int charWidth = (int) Math.round(undoItem.getLayoutBounds().getWidth());
                            int charHeight = (int) Math.round(undoItem.getLayoutBounds().getHeight());
                            double x = undoItem.getX();
                            double y = undoItem.getY();
                            if (undoItem.getText().equals("\n")) {
                                redoStack.push(undoItem);
                                textRoot.getChildren().remove(undoItem);
                                buffer.remove(undoItem);
                                Text prevItem = buffer.getCurrentItem();
                                if (prevItem == null) {
                                    cursor.setHeight(lastCharHeight);
                                    cursor.setX(x);
                                    cursor.setY(y - lastCharHeight);
                                    render();
                                    return;  
                                }
                                int xCen = (int) Math.round(prevItem.getX());
                                int yCen = (int) Math.round(prevItem.getY());
                                textCenterX = xCen;
                                textCenterY += yCen;
                                int prevCharWidth = (int) Math.round(prevItem.getLayoutBounds().getWidth());
                                double prevX = prevItem.getX();
                                double prevY = prevItem.getY();
                                cursor.setHeight(lastCharHeight);
                                cursor.setX(prevX + prevCharWidth);
                                cursor.setY(prevY);
                                render();
                                return;
                            }
                            if (textRoot.getChildren().contains(undoItem)) {
                                buffer.remove(undoItem);
                                redoStack.push(undoItem);
                                textRoot.getChildren().remove(undoItem);
                                cursor.setHeight(charHeight);
                                cursor.setX(x);
                                cursor.setY(y);
                            } else {
                                redoStack.push(undoItem);
                                buffer.addLast(undoItem);
                                textRoot.getChildren().add(undoItem);
                                cursor.setHeight(charHeight);
                                cursor.setX(x + charWidth);
                                cursor.setY(y);
                            }
                            render();
                        }

                    }
                    if (keyEvent.getCode() == KeyCode.Y) {
                        if (!redoStack.isEmpty()) {
                            Text redoItem = redoStack.pop();
                            int charWidth = (int) Math.round(redoItem.getLayoutBounds().getWidth());
                            int charHeight = (int) Math.round(redoItem.getLayoutBounds().getHeight());
                            double x = redoItem.getX();
                            double y = redoItem.getY();
                            if (redoItem.getText().equals("\n")) {
                                buffer.addLast(redoItem);
                                undoStack.push(redoItem);
                                textRoot.getChildren().add(redoItem);
                                Text prevItem = redoStack.peek();
                                if (prevItem == null) {
                                    cursor.setHeight(lastCharHeight);
                                    cursor.setX(x);
                                    cursor.setY(y);
                                    render();
                                    return;  
                                }
                                int xCen = (int) Math.round(prevItem.getX());
                                int yCen = (int) Math.round(prevItem.getY());
                                textCenterX = xCen;
                                textCenterY += yCen;
                                int prevCharWidth = (int) Math.round(prevItem.getLayoutBounds().getWidth());
                                double prevX = prevItem.getX();
                                double prevY = prevItem.getY();
                                cursor.setHeight(lastCharHeight);
                                cursor.setX(x + charWidth);
                                cursor.setY(y);
                                render();
                                return;
                            }
                            if (textRoot.getChildren().contains(redoItem)) {
                                buffer.remove(redoItem);
                                undoStack.push(redoItem);
                                textRoot.getChildren().remove(redoItem);
                                cursor.setHeight(charHeight);
                                cursor.setX(x);
                                cursor.setY(y);
                            } else {
                                buffer.addLast(redoItem);
                                undoStack.push(redoItem);
                                textRoot.getChildren().add(redoItem);
                                cursor.setHeight(charHeight);
                                cursor.setX(x + charWidth);
                                cursor.setY(y);
                            }
                            render();
                        }
                    }
                }


                if (code == KeyCode.LEFT) {
                    if (buffer.isTheFirstElement()) {
                        return;
                    }
                    Text textToMoveThrough = buffer.getPreviousItem();
                    if (textToMoveThrough == null) {
                        buffer.moveCurrentLeft();
                        cursor.setHeight(lastCharHeight);
                        cursor.setX(5);
                        cursor.setY(0);
                        return;
                    }
                    int charHeight = (int) Math.round(textToMoveThrough.getLayoutBounds().getHeight());
                    int charWidth = (int) Math.round(textToMoveThrough.getLayoutBounds().getWidth());
                    double x = textToMoveThrough.getX();
                    double y = textToMoveThrough.getY();
                    buffer.moveCurrentLeft();
                    cursor.setHeight(lastCharHeight);
                    cursor.setX(x + charWidth);
                    cursor.setY(y);
                } 
                if (code == KeyCode.RIGHT) {
                    if (buffer.isTheLastElement()) {
                        return;
                    }
                    buffer.moveCurrentRight();
                    Text textToMoveThrough = buffer.getCurrentItem();
                    int charWidth = (int) Math.round(textToMoveThrough.getLayoutBounds().getWidth());
                    int charHeight = (int) Math.round(textToMoveThrough.getLayoutBounds().getHeight());
                    double x = textToMoveThrough.getX();
                    double y = textToMoveThrough.getY();
                    cursor.setHeight(lastCharHeight);
                    cursor.setX(x + charWidth);
                    cursor.setY(y);
                }
                if (code == KeyCode.UP) {
                    double cursPosX = cursor.getX();
                    double curPosY = cursor.getY();
                    if (curPosY == 0) { 
                        return;
                    }
                    double newCursorPosX = cursPosX;
                    double newCursorPosY = curPosY - fontSize;
                    double minDeltaX = (int) Math.round(scene.getWidth());
                    Text newCursorText = null;
                    int indicator = 0;
                    for (Text text: buffer) {
                        if (text.getY() == newCursorPosY) {
                            if (text.getX() > cursPosX) {
                                indicator = 1;
                            }
                            if (Math.abs(text.getX() - cursPosX) <  minDeltaX) {
                                minDeltaX = Math.min(minDeltaX, Math.abs(text.getX() - cursPosX));
                                newCursorText = text;
                                newCursorPosX = text.getX();
                            }
                        }
                    }
                    if (indicator == 0) {
                        for (Text text: buffer) {
                            if (text.getY() == newCursorPosY) {
                                newCursorText = text;
                                newCursorPosX = text.getX();
                            }
                        }
                        buffer.setCurrentNodeAfter(newCursorText);
                        buffer.moveCurrentRight();
                        cursor.setHeight(lastCharHeight);
                        cursor.setX(newCursorPosX + (int) Math.round(newCursorText.getLayoutBounds().getWidth()));
                        cursor.setY(cursorPosY - fontSize);
                        return;
                    }
                    if (newCursorText.getX() == 5) {
                        buffer.setCurrentNodeAfter(newCursorText);
                        buffer.moveCurrentRight();
                        cursor.setHeight(lastCharHeight);
                        cursor.setX(5);
                        cursor.setY(newCursorPosY);
                        return;
                    }
                    buffer.setCurrentNodeAfter(newCursorText);
                    cursor.setHeight(lastCharHeight);
                    cursor.setX(newCursorPosX);
                    cursor.setY(newCursorPosY);
                }
                if (code == KeyCode.DOWN) {
                    double cursPosX = cursor.getX();
                    double curPosY = cursor.getY();
                    if (curPosY == numberOfLines * fontSize - fontSize) { 
                        return;
                    }
                    double newCursorPosX = cursPosX;
                    double newCursorPosY = curPosY + fontSize;
                    double minDeltaX = (int) Math.round(scene.getWidth());
                    int indicator = 0;
                    Text newCursorText = null;
                    for (Text text: buffer) {
                        if (text.getY() == newCursorPosY) {
                            if (text.getX() < cursPosX) {
                                indicator = 1;
                            }
                            if (Math.abs(text.getX() - cursPosX) <  minDeltaX) {
                                minDeltaX = Math.min(minDeltaX, Math.abs(text.getX() - cursPosX));
                                newCursorText = text;
                                newCursorPosX = text.getX();
                            }
                        }
                    }
                    if (indicator == 0) {
                        for (Text text: buffer) {
                            if (text.getY() == newCursorPosY) {
                                newCursorText = text;
                                newCursorPosX = text.getX();
                            }
                        }
                        buffer.setCurrentNodeAfter(newCursorText);
                        buffer.moveCurrentRight();
                        cursor.setHeight(lastCharHeight);
                        cursor.setX(newCursorPosX + (int) Math.round(newCursorText.getLayoutBounds().getWidth()));
                        cursor.setY(cursorPosY + fontSize);
                        return;
                    }
                    if (newCursorText.getX() == 5) {
                        buffer.setCurrentNodeAfter(newCursorText);
                        buffer.moveCurrentRight();
                        cursor.setHeight(lastCharHeight);
                        cursor.setX(5);
                        cursor.setY(newCursorPosY);
                        return;
                    }
                    buffer.setCurrentNodeAfter(newCursorText);
                    cursor.setHeight(lastCharHeight);
                    cursor.setX(newCursorPosX);
                    cursor.setY(newCursorPosY);
                }
                if (code == KeyCode.BACK_SPACE) {
                    Text textToRemove = buffer.removeLast();
                    if (textToRemove != null) {
                        undoStack.push(textToRemove);
                        redoStack = new editor.LinkedListStack();
                        String textToRemoveString = textToRemove.getText();
                        if (textToRemoveString == "\n") {
                            Text previousText = buffer.getCurrentItem();
                            if (previousText == null) {
                                cursor.setHeight(lastCharHeight);
                                cursor.setX(5);
                                cursor.setY(0);
                                render();
                                return;  
                            }
                            int x = (int) Math.round(previousText.getX());
                            int y = (int) Math.round(previousText.getY());
                            textCenterX = x;
                            textCenterY += y;
                            int charWidth = (int) Math.round(previousText.getLayoutBounds().getWidth());
                            double curX = previousText.getX();
                            double curY = previousText.getY();
                            cursor.setHeight(lastCharHeight);
                            cursor.setX(curX + charWidth);
                            cursor.setY(curY);
                            render();
                        } else {
                            root.getChildren().remove(textToRemove);
                            textCenterX -= (int) Math.round(textToRemove.getLayoutBounds().getWidth());
                            double x = textToRemove.getX();
                            double y = textToRemove.getY();
                            int charHeight = (int) Math.round(textToRemove.getLayoutBounds().getHeight());
                            cursor.setHeight(charHeight);
                            cursor.setX(x);
                            cursor.setY(y);
                            render();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        editor.LinkedListDeque<Text> bufferNew = new editor.LinkedListDeque<Text>();
        ArrayList<String> args = new ArrayList<String>();
        try { 
            for (int i = 0; i <= 5; i++) {
                args.add(getParameters().getRaw().get(i));
            }
        } catch (Exception e) {};
        if (args.size() == 0) {
            System.out.println("No source filename was provided");
            System.exit(1);
        } else if (args.size() > 2) {
            System.out.println("Expected usage: CopyFile <source filename> (<debug>)");
            System.exit(1);
        } else if (args.size() == 1 || (args.size() == 2 && args.get(1).equals("debug"))) {
            String inputFilename = args.get(0);
            try {
                inputFile = new File(inputFilename);
                if (inputFile.exists()) {
                    if (inputFile.isDirectory()) {
                        System.out.println("Unable to open file nameThatIsADirectory");
                        System.exit(1);
                    }
                    FileReader reader = new FileReader(inputFile);
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    int intRead = -1;
                    while ((intRead = bufferedReader.read()) != -1) {
                        char charRead = (char) intRead;
                        String string = String.valueOf(charRead);
                        Text text = new Text(string);
                        Font font = new Font(fontName, fontSize);
                        text.setFont(font);
                        text.setTextOrigin(VPos.TOP);
                        bufferNew.addLast(text);
                    }
                    bufferedReader.close();
                }
            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("File not found! Exception was: " + fileNotFoundException);
            } catch (IOException ioException) {
                System.out.println("Error when copying; exception was: " + ioException);
            }
            if (args.size() == 2) {
                System.out.println();
                System.out.println("===========================================================================");
                System.out.println("\"Sometimes it is the people no one can imagine anything of who do the things no one can imagine.\" ― Alan Turing");
                System.out.println("Best luck debugging!");
                System.out.println("===========================================================================");
            }
        }

        root = new Group();
        textRoot = new Group();
        editor.BlinkingCursor blinkTimer = new editor.BlinkingCursor(cursor);
        buffer = bufferNew;
        undoStack = new editor.LinkedListStack<Text>();
        redoStack = new editor.LinkedListStack<Text>();
        int windowWidth = 500;
        int windowHeight = 500;
        scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(textRoot, windowWidth, windowHeight);
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        primaryStage.setTitle("The Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
        cursor.setX(5);
        cursor.setY(0);
        cursor.setHeight(fontSize);
        if (!buffer.isEmpty()) {
            for (Text text : buffer) {
                textRoot.getChildren().add(text);
            }
            buffer.clearCursor();
        }
        textRoot.getChildren().add(cursor);
        root.getChildren().add(textRoot);
        render();
        scroll = new editor.ScrollingBar(windowHeight, windowWidth);
        root.getChildren().add(scroll.scroll());
        if (numberOfLines * fontSize > windowHeight) {
            double totalPixs = numberOfLines * fontSize;
            double unseenPixs = totalPixs - windowHeight;
            double unseenProportion = unseenPixs / totalPixs;
            scroll.scroll().setMax(totalPixs);
            double max = scroll.scroll().getMax();
        }


        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {

                double usableScreenWidth = (int) Math.round(scene.getWidth()) - scroll.getLayoutBounds().getWidth() + 3;
                scroll.scroll().setLayoutX(usableScreenWidth);
                render();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                double usableScreenHeight = (int) Math.round(scene.getHeight());
                scroll.scroll().setPrefHeight(usableScreenHeight);
                render();
            }
        });

        scroll.scroll().valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {


                int oldVal = oldValue.intValue();
                int newVal = newValue.intValue();   

                if (oldValue != newValue) {
                    if (numberOfLines * fontSize > windowHeight) {
                        double totalPixs = numberOfLines * fontSize;
                        double unseenPixs = totalPixs - windowHeight;
                        double unseenProportion = unseenPixs / totalPixs;
                        scroll.scroll().setValue(newVal);
                        textRoot.setLayoutY((-1) * ((unseenProportion) * newVal));
                    }
                }

            }
        });

        scene.setOnMouseClicked(new MouseClickEventHandler(root));
    }

    private void render() {
        int sceneWidth = (int) Math.round(scene.getWidth());
        double oldTextPositionX = 5.0;
        double oldTextPositionY = 0.0;
        int linesTotal = 0;
        ArrayList<Text> bufferList = new ArrayList<Text>();
        for (Text text: buffer) {
            bufferList.add(text);
            int textWidth = (int) Math.round(text.getLayoutBounds().getWidth());
            int textHeight = (int) Math.round(text.getLayoutBounds().getHeight());
            int index = bufferList.indexOf(text);
            if ((oldTextPositionX >= (sceneWidth - 30)) &&  !text.getText().equals("\n")) {

                int i = 0;
                while (index != 0) {
                    if (bufferList.get(index).getText().equals(" ")) {
                        break;
                    }
                    if (sceneWidth / textWidth < i) {
                        break;
                    }
                    i++;
                    index--;
                }
                oldTextPositionX = 5;
                oldTextPositionY += fontSize;
            } else if (text.getText().equals("\n")) {
                oldTextPositionX = 5;
                oldTextPositionY += fontSize;
            }
            while (index < bufferList.size() - 1) {
                int textWidthCur = (int) Math.round(bufferList.get(index).getLayoutBounds().getWidth());
                bufferList.get(index).setX(oldTextPositionX);
                bufferList.get(index).setY(oldTextPositionY);
                oldTextPositionX = bufferList.get(index).getX() + textWidthCur;
                oldTextPositionY = bufferList.get(index).getY();
                index++;
            }
            text.setX(oldTextPositionX);
            text.setY(oldTextPositionY);
            oldTextPositionX = text.getX() + textWidth;
            oldTextPositionY = text.getY();
            int line = (int) Math.round(text.getY());
            linesTotal = Math.max(linesTotal, line);
        }
        numberOfLines = linesTotal / fontSize + 1;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
