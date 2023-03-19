import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    public static final int EASY = 4;
    public static final int MEDIUM = 7;
    public static final int HARD = 10;
    public static final int EXPERT = 16;

    private final Color[] textColors = {getBackground(), Color.blue, Color.green.darker(), Color.red,
            Color.blue.darker(), Color.red.darker()};

    private Point mouseLocation = new Point(0, 0);

    private BufferedImage bombImage;
    private BufferedImage flagImage;

    private final Random random = new Random();
    private int gridSize = 30;
    private double percentageAreBombs = EASY;
    private int numberOfBombs = (int) ((gridSize * gridSize) * (percentageAreBombs / 100));

    private Tile[][] grid = new Tile[gridSize][gridSize];
    private int tileSize;
    private boolean gameOver = false;

    public GamePanel() {
        addMouseListener(this);
        addMouseMotionListener(this);

        setupGame(EASY);
    }

    public void loadResources() {

        tileSize = getWidth() / gridSize;
        System.out.println(tileSize);
        try {
            bombImage = Tools.resizeBufferedImage(Tools.loadBufferedImage("bomb.png"), tileSize, tileSize);
            flagImage = Tools.resizeBufferedImage(Tools.loadBufferedImage("flag.png"), tileSize, tileSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupGame(int difficulty) {
        percentageAreBombs = difficulty;
        numberOfBombs = (int) ((gridSize * gridSize) * (percentageAreBombs / 100));
        generateGrid();
        gameOver = false;
        repaint();
    }

    private void generateGrid() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = new Tile(j, i, false);
            }
        }
        int bombCount = 0;
        while (bombCount < numberOfBombs) {
            Tile randomTile = grid[random.nextInt(gridSize)][random.nextInt(gridSize)];
            if (!randomTile.isBomb()) {
                randomTile.setBomb(true);
                bombCount++;
            }
        }

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j].setValue(getNumOfBombNextToTile(grid[i][j]));
            }
        }
    }

    private int getNumOfBombNextToTile(Tile tile) {
        int count = 0;
        int[][] xyoffsets = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

        for (int[] xyoffset : xyoffsets) {
            int x = tile.getX() + xyoffset[0];
            int y = tile.getY() + xyoffset[1];
            try {
                if (grid[y][x].isBomb())
                    count++;
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
        return count;
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        tileSize = getWidth() / gridSize;
        g2.setFont(getFont().deriveFont(Font.BOLD));

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Tile tile = grid[j][i];
                if (!tile.isShown()) {
                    g2.setColor(getBackground().brighter());
                    g2.fill3DRect(tile.getX() * tileSize, tile.getY() * tileSize,
                            tileSize, tileSize, true);
                    if(tile.isFlag())
                        g2.drawImage(flagImage, tile.getX() * tileSize, tile.getY() * tileSize, null);
                } else {
                    g2.setColor(getBackground().brighter());
                    g2.fill3DRect(tile.getX() * tileSize, tile.getY() * tileSize,
                            tileSize, tileSize, false);
                    if (tile.isBomb()) {
                        g2.drawImage(bombImage, tile.getX() * tileSize, tile.getY() * tileSize, null);

                    } else {
                        g2.setColor(textColors[tile.getValue()]);
                        Tools.centerString(g2, new Rectangle(tile.getX() * tileSize, tile.getY() * tileSize,
                                tileSize, tileSize), "" + tile.getValue());
                    }


                }


            }
        }
    }

    private void revealAdjacentTiles(Tile tile) {
        int[][] xyoffsets = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

        for (int[] xyoffset : xyoffsets) {
            int x = tile.getX() + xyoffset[0];
            int y = tile.getY() + xyoffset[1];
            try {
                Tile adjacentTile = grid[y][x];
                if (!adjacentTile.isShown()) {
                    if(!adjacentTile.isBomb()) {
                        adjacentTile.setShown(true);
                        if (adjacentTile.getValue() == 0){
                            revealAdjacentTiles(adjacentTile);
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
    }


    private Tile getTile(int x, int y) {
        return grid[y][x];
    }

    private Tile getTile(MouseEvent e) {
        int x = (int) Math.floor(e.getX() / tileSize);
        int y = (int) Math.floor(e.getY() / tileSize);
        return getTile(x, y);
    }

    private void revealTile(Tile tile) {
        if (tile.isShown() || tile.isFlag()) {
            return;
        }
        tile.setShown(true);
        if (tile.getValue() == 0) {
            int[][] xyoffsets = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
            for (int[] xyoffset : xyoffsets) {
                int x = tile.getX() + xyoffset[0];
                int y = tile.getY() + xyoffset[1];
                try {
                    revealTile(grid[y][x]);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
    }

    private void revealMines() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Tile tile = grid[j][i];
                if (tile.isBomb()) {
                    tile.setShown(true);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setupGame(Integer.parseInt(e.getActionCommand()));
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseLocation = new Point(e.getX(), e.getY());
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(gameOver)
            return;
        Tile pressedTile = getTile(e);
        if (pressedTile.isFlag()) {
            return;
        }
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (pressedTile.isBomb()) {
                gameOver = true;
                revealMines();
            } else {
                revealTile(pressedTile);
            }
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            pressedTile.setFlag(!pressedTile.isFlag());
        }
        if (SwingUtilities.isMiddleMouseButton(e) && pressedTile.isShown()) {
            revealAdjacentTiles(pressedTile);
            repaint();
        }
        repaint();
        if(gameOver)
            JOptionPane.showMessageDialog(this, "Game over");
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}