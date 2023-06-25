package com.company;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Snake extends JFrame {
    private JMenuBar saveMenu = null;
    public static int dots;
    private static String playerName;
    public boolean itOldGame;
    Board board;
    public Snake(String playerName, boolean itOldGame) throws FileNotFoundException {
        this.itOldGame = itOldGame;
        this.playerName = playerName;
        createSaveMenu();
        setJMenuBar(saveMenu);
        board = new Board(this, itOldGame);
        initUI();
    }
    
    private void initUI() throws FileNotFoundException {
        add(board);
        setResizable(false);
        pack();
        setTitle("Snake " + Snake.playerName);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void createSaveMenu() throws FileNotFoundException {

        saveMenu = new JMenuBar();
        JMenu fileMenu = new JMenu("Сохранение");

        JMenuItem item = new JMenuItem("Сохранить игру");
        JSeparator separator = new JSeparator();
        item.setActionCommand("Сохранить игру");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.saveGame();
            }
        });
        fileMenu.add(item);
        fileMenu.add(separator);

        saveMenu.add(fileMenu);
    }


    static class Board extends JPanel implements ActionListener {
        private Snake gameWindow;
        private final int B_WIDTH = 350;
        private final int B_HEIGHT = 400;
        private final int DOT_SIZE = 10;
        private final int ALL_DOTS = 900;
        private final int RAND_POS = 29;
        private final int DELAY = 140;

        private final int x[] = new int[ALL_DOTS];
        private final int y[] = new int[ALL_DOTS];

        private int apple_x;
        private int apple_y;
        private int counter;

        private boolean leftDirection = false;
        private boolean rightDirection = true;
        private boolean upDirection = false;
        private boolean downDirection = false;
        private boolean inGame = true;
        public boolean itOldGame;
        private boolean canSaveGame;

        private Timer timer;
        private Image ball;
        private Image apple;
        private Image head;



        public Board(Snake gameWindow, boolean itOldGame) throws FileNotFoundException {
            this.gameWindow = gameWindow;
            this.itOldGame = itOldGame;
            initBoard();
        }

        private void initBoard() throws FileNotFoundException {
            addKeyListener(new TAdapter());
            setBackground(Color.black);
            setFocusable(true);

            setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
            loadImages();
            initGame();

        }

        private void loadImages() {

            ImageIcon iid = new ImageIcon("src/resources/dot.png");
            ball = iid.getImage();

            ImageIcon iia = new ImageIcon("src/resources/apple.png");
            apple = iia.getImage();

            ImageIcon iih = new ImageIcon("src/resources/head.png");
            head = iih.getImage();
        }

        private void initGame() throws FileNotFoundException {
            if (itOldGame)
            {
                Scanner scanner = new Scanner(new FileInputStream("SaveGame.txt"));
                playerName = scanner.nextLine();
                dots = Integer.parseInt(scanner.nextLine());
                counter = Integer.parseInt(scanner.nextLine());
            }else {
                dots = 3;
                counter = 0;
            }


            for (int z = 0; z < dots; z++) {
                x[z] = 50 - z * 10;
                y[z] = 50;
            }

            locateApple();

            timer = new Timer(DELAY, this);
            timer.start();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                doDrawing(g);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void doDrawing(Graphics g) throws IOException {

            if (inGame) {

                g.drawImage(apple, apple_x, apple_y, this);

                for (int z = 0; z < dots; z++) {
                    if (z == 0) {
                        g.drawImage(head, x[z], y[z], this);
                    } else {
                        g.drawImage(ball, x[z], y[z], this);
                    }
                }

                Toolkit.getDefaultToolkit().sync();
                canSaveGame = true;
            } else {
                saveRecordInFile();
                gameOver(g);
                canSaveGame = false;
                counter = 0;
            }
        }

        private void saveRecordInFile() throws IOException {
            if(counter!=0) {
                String record = String.valueOf(counter);
                try (FileWriter writer = new FileWriter("statistics.txt", true)) {
                    writer.write("\n" + record + " яблок - " + playerName);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }

                ArrayList<String> lines = new ArrayList<>();
                try {
                    lines = new ArrayList<>(Files.readAllLines(Paths.get("statistics.txt")));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
                lines.removeAll(Arrays.asList("", null));
                lines.sort(Comparator.comparing(str -> Integer.parseInt(str.split("\\s+")[0])));
                lines.sort(Collections.reverseOrder());

                Files.write(Paths.get("statistics.txt"), lines, StandardOpenOption.CREATE);
            }
        }

        private void gameOver(Graphics g) {

            String msg = "Game Over";
            Font small = new Font("Helvetica", Font.BOLD, 14);
            FontMetrics metr = getFontMetrics(small);

            g.setColor(Color.white);
            g.setFont(small);
            g.drawString(msg, (B_WIDTH - metr.stringWidth(msg)) / 2, B_HEIGHT / 2);

        }

        private void checkApple() {

            if ((x[0] == apple_x) && (y[0] == apple_y)) {
                counter++;
                dots++;
                locateApple();
            }
        }

        private void move() {

            for (int z = dots; z > 0; z--) {
                x[z] = x[(z - 1)];
                y[z] = y[(z - 1)];
            }

            if (leftDirection) {
                x[0] -= DOT_SIZE;
            }

            if (rightDirection) {
                x[0] += DOT_SIZE;
            }

            if (upDirection) {
                y[0] -= DOT_SIZE;
            }

            if (downDirection) {
                y[0] += DOT_SIZE;
            }
        }

        private void checkCollision() {

            for (int z = dots; z > 0; z--) {

                if ((z > 4) && (x[0] == x[z]) && (y[0] == y[z])) {
                    inGame = false;
                }
            }

            if (y[0] >= B_HEIGHT) {
                inGame = false;
            }

            if (y[0] < 0) {
                inGame = false;
            }

            if (x[0] >= B_WIDTH) {
                inGame = false;
            }

            if (x[0] < 0) {
                inGame = false;
            }

            if (!inGame) {
                timer.stop();
            }
        }

        private void locateApple() {

            int r = (int) (Math.random() * RAND_POS);
            apple_x = ((r * DOT_SIZE));

            r = (int) (Math.random() * RAND_POS);
            apple_y = ((r * DOT_SIZE));
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (inGame) {

                checkApple();
                checkCollision();
                move();
            }

            repaint();
        }

        private class TAdapter extends KeyAdapter {

            @Override
            public void keyPressed(KeyEvent e) {

                int key = e.getKeyCode();

                if (key == KeyEvent.VK_ESCAPE) {
                    timer.stop();
                    saveGame();
                }
//                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
//                    timer.start();
//                    onPause = false;
//                }

                if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
                    leftDirection = true;
                    upDirection = false;
                    downDirection = false;
                }

                if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
                    rightDirection = true;
                    upDirection = false;
                    downDirection = false;
                }

                if ((key == KeyEvent.VK_UP) && (!downDirection)) {
                    upDirection = true;
                    rightDirection = false;
                    leftDirection = false;
                }

                if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
                    downDirection = true;
                    rightDirection = false;
                    leftDirection = false;
                }
            }
        }

        public void saveGame() {
            if (canSaveGame) {
                try (FileWriter writer = new FileWriter("saveGame.txt", false)) {
                    writer.write(playerName + "\n");
                    writer.write(dots + "\n");
                    writer.write(Integer.toString(counter));

                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
                JOptionPane.showMessageDialog(null, "Текущая игра сохранена!\nИгрок: " + playerName +"\n"+"Съедено яблок: " +counter,
                        "Сохранение", JOptionPane.PLAIN_MESSAGE);

                gameWindow.dispatchEvent(new WindowEvent(gameWindow,WindowEvent.WINDOW_CLOSING));

            }else {
                JOptionPane.showMessageDialog(null, "Сохранить игру нельзя\nВы уже проиграли!",
                        "Ошибка", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }
}
