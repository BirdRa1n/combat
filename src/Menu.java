import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Menu extends JPanel {
    private int rounds = 0;
    private final int maxRounds = 10;
    private final int minRounds = 1;
    private  boolean  jogoIniciado = false;

    public Menu(){
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    // Decrease rounds with left arrow
                    if (rounds > minRounds) rounds--;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    // Increase rounds with right arrow
                    if (rounds < maxRounds) rounds++;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_Q) {
                    // Exit the game when pressing Q
                    System.exit(0);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Start the game when pressing Enter
                    iniciarJogo();
                }
            }
        });
    }
    private void iniciarJogo() {
        // Configura a janela principal do jogo
        JFrame gameFrame = new JFrame("Combat Game");
        Game game = new Game(rounds); // Passa a quantidade de rounds escolhida

        gameFrame.add(game);
        gameFrame.pack();
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);

        // Fecha a janela do menu
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose();    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Desenha o título
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "Combat";
        FontMetrics metrics = g.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(title)) / 2;
        int y = 150;
        g.drawString(title, x, y);

        // Desenha a quantidade de rounds
        g.setFont(new Font("Arial", Font.PLAIN, 30));
        String roundsText = "Rounds: " + rounds;
        metrics = g.getFontMetrics();
        x = (getWidth() - metrics.stringWidth(roundsText)) / 2;
        y = 250;
        g.drawString(roundsText, x, y);

        // Desenha a mensagem de controle
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String controlMessage = "Pressione Q para sair";
        g.drawString(controlMessage, getWidth() - 200, getHeight() - 30);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Configura a janela do menu principal
            JFrame frame = new JFrame("Menu");
            Menu menu = new Menu();

            frame.add(menu);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
    
}
