import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame{
    public MainFrame() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GamePanel panel = new GamePanel();
        this.setJMenuBar(new MinesweeperMenuBar(panel));
        panel.setPreferredSize(new Dimension(600, 600));
        setContentPane(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }
}