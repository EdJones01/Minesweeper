import javax.swing.*;
import java.awt.event.ActionListener;

public class MinesweeperMenuBar extends JMenuBar {
    public MinesweeperMenuBar(ActionListener actionListener) {
        this.add(new JLabel("Difficulty:"));
        this.addGap(10);
        this.addDifficultyButton("Easy", GamePanel.EASY, actionListener);
        this.addDifficultyButton("Medium", GamePanel.MEDIUM, actionListener);
        this.addDifficultyButton("Hard", GamePanel.HARD, actionListener);
        this.addDifficultyButton("Expert", GamePanel.EXPERT, actionListener);
    }

    private void addDifficultyButton(String name, String cmd, ActionListener actionListener) {
        JButton button = new JButton(name);
        button.addActionListener(actionListener);
        button.setActionCommand(cmd);
        button.setFocusable(false);
        this.add(button);
        this.addGap(5);
    }

    private void addGap(int size) {
        this.add(Box.createHorizontalStrut(size));
    }
}
