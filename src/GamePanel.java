import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener, MouseListener {
    public static final String EASY = "10_10";
    public static final String MEDIUM = "20_40";
    public static final String HARD = "30_100";
    public static final String EXPERT = "40_250";

    private final Color[] textColors = {getBackground(), Color.blue, Color.green.darker(), Color.red,
            Color.blue.darker(), Color.red.darker()};

    private int numberOfBombs;
    private int gridSize;

    private BufferedImage bombImage;
    private BufferedImage flagImage;

    private final Random random = new Random();

    private Tile[][] grid;
    private int tileSize;
    private boolean gameOver = false;

    public GamePanel() {
        addMouseListener(this);

        setupGame("10_1");
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

    private void setupGame(String difficulty) {
        gridSize = Integer.parseInt(difficulty.split("_")[0]);
        numberOfBombs = Integer.parseInt(difficulty.split("_")[1]);
        grid = new Tile[gridSize][gridSize];
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
        g2.setFont(getFont().deriveFont(Font.BOLD, (int) (tileSize*0.8)));

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Tile tile = grid[j][i];
                if (!tile.isShown()) {
                    g2.setColor(getBackground().brighter());
                    g2.fill3DRect(tile.getX() * tileSize, tile.getY() * tileSize,
                            tileSize, tileSize, true);
                    if (tile.isFlag())
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

        if (gameOver)
            drawGameOverScreen(g2);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(getBackground());
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 40f));
        String text = "You Lose.";
        if(checkWin())
            text = "You Win!";
        Tools.centerString(g2, getBounds(), text);
    }

    private void revealAdjacentTiles(Tile tile) {
        int[][] xyoffsets = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

        for (int[] xyoffset : xyoffsets) {
            int x = tile.getX() + xyoffset[0];
            int y = tile.getY() + xyoffset[1];
            try {
                Tile adjacentTile = grid[y][x];
                if (!adjacentTile.isShown()) {
                    if (!adjacentTile.isFlag()) {
                        adjacentTile.setShown(true);
                        if(adjacentTile.isBomb()) {
                            gameOver();
                        }

                        if (adjacentTile.getValue() == 0) {
                            revealAdjacentTiles(adjacentTile);
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
    }

    private void gameOver() {
        gameOver = true;
        revealMines();
    }

    private boolean checkWin() {
        int count = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if(grid[i][j].isBomb() && grid[i][j].isFlag())
                    count++;
            }
        }
        if(count == numberOfBombs)
            return true;

        count = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if(!grid[i][j].isBomb() && grid[i][j].isShown())
                    count++;
            }
        }
        return count == gridSize * gridSize - numberOfBombs;
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
        setupGame(e.getActionCommand());
        loadResources();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver)
            return;
        gameOver = checkWin();
        Tile pressedTile = getTile(e);
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (pressedTile.isFlag()) {
                return;
            }
            if (pressedTile.isBomb()) {
                gameOver();
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

    @Override
    public void mouseClicked(MouseEvent e) {

    }
}