import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.sound.sampled.*;
import javax.swing.*;

public class Game extends JPanel implements ActionListener {
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final Timer timer;
    private int rounds;
    private int currentRound;
    private int player1Wins;
    private int player2Wins;
    private boolean isPaused = false; // Para controlar o estado do jogo
    private void startNewGame() {
        // Reseta o estado do jogo, como posições, pontuações, etc.
        System.out.println("Iniciando nova partida...");
    }
    private Tanque player1;
    private Tanque player2;
    private ArrayList<Bala> balas;
    private ArrayList<Obstaculo> obstaculos;

    // Gerenciamento de teclas para cada jogador
    private Set<Integer> player1Keys = new HashSet<>();
    private Set<Integer> player2Keys = new HashSet<>();

    public Game(int rounds) {
        // Garante número ímpar de rounds
        this.rounds = (rounds % 2 == 0) ? rounds + 1 : rounds;
        this.currentRound = 1;
        this.player1Wins = 0;
        this.player2Wins = 0;

        // Configurações da tela do jogo
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Inicializa tanques, balas e obstáculos
        player1 = new Tanque(100, 300, Color.RED, KeyEvent.VK_SPACE);
        player2 = new Tanque(700, 300, Color.BLUE, KeyEvent.VK_ENTER);
        balas = new ArrayList<>();
        obstaculos = new ArrayList<>();
        createObstacles();
        playBackgroundMusic();

        // Adiciona listeners de teclado para controle dos tanques
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (isPaused) {
                    // Trata opções do menu de pausa
                    if (keyCode == KeyEvent.VK_1) { // Retomar
                        isPaused = false;
                        repaint();
                    } else if (keyCode == KeyEvent.VK_2) { // Nova Partida
                        startNewGame(); // Método para reiniciar o jogo
                        isPaused = false;
                        repaint();
                    } else if (keyCode == KeyEvent.VK_3) { // Sair
                        System.exit(0); // Fecha o jogo
                    }
                } else if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_P) {
                    isPaused = !isPaused; // Alterna o estado de pausa
                    repaint();
                }
                synchronized (this) {
                    if (isPlayer1Key(e.getKeyCode())) {
                        player1Keys.add(e.getKeyCode());
                    } else if (isPlayer2Key(e.getKeyCode())) {
                        player2Keys.add(e.getKeyCode());
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                synchronized (this) {
                    if (player1Keys.contains(e.getKeyCode())) {
                        player1Keys.remove(e.getKeyCode());
                    } else if (player2Keys.contains(e.getKeyCode())) {
                        player2Keys.remove(e.getKeyCode());
                    }
                }
            }
        });

        // Inicializa o timer para atualização do jogo
        timer = new Timer(16, this); // Aproximadamente 60 FPS
        timer.start();
    }

    private void createObstacles() {
        // Cria obstáculos fixos no mapa
        obstaculos.add(new Obstaculo(300, 200, 50, 200));
        obstaculos.add(new Obstaculo(500, 200, 50, 200));
    }

        /**
         * Redesenha o componente com o estado atual do jogo.
         * Caso o jogo esteja pausado, exibe o menu de pausa com as opções:
         * 1. Retomar
         * 2. Nova Partida
         * 3. Sair
         * Caso contrário, desenha as informações do jogo, os tanques, as balas e os obstáculos.
         * @param g o objeto gráfico para desenhar no componente.
         */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isPaused) {
            // Exibe o menu de pausa
            g.setColor(Color.BLACK);
            g.fillRect(50, 50, 300, 200);

            g.setColor(Color.WHITE);
            g.drawString("PAUSADO", 170, 90);
            g.drawString("1. Retomar", 150, 120);
            g.drawString("2. Resetar Partida", 150, 150);
            g.drawString("3. Sair", 150, 180);
        } else {

            // Desenha informações do jogo
            g.setColor(Color.WHITE);
            g.drawString("Jogador 1: " + player1.getVidas() + " ♥", 10, 20);
            g.drawString("Jogador 2: " + player2.getVidas() + " ♥", WIDTH - 100, 20);
            g.drawString("Round : " + currentRound + "/" + rounds, WIDTH / 2 - 40, 20);

            // Desenha os tanques
            player1.draw(g);
            player2.draw(g);

            // Desenha as balas
            for (Bala bala : balas) {
                bala.draw(g);
                System.out.println("Desenhando bala: " + bala.getOwner()); // Adicione esta linha
            }

            // Desenha os obstáculos
            for (Obstaculo obstacle : obstaculos) {
                obstacle.desenhar(g);
            }
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        // Processa movimento baseado nas teclas pressionadas
        processPlayerMovement();

        // Atualiza as balas
        for (int i = 0; i < balas.size(); i++) {
            Bala bala = balas.get(i);
            bala.mover();
            System.out.println("Bala em movimento: " + bala.getOwner()); // Adicione esta linha

            // Verifica colisão da bala com os tanques
            if (bala.getAreaColisao(player1) && bala.getOwner() != player1) {
                balas.remove(i);
                player1.perdeVida();
                if (player1.getVidas() <= 0) {
                    player2Wins++;
                    playPlayer2WinSound();
                    resetRound("Player 2 venceu o round!");
                }
                continue;
            }

            if (bala.getAreaColisao(player2) && bala.getOwner() != player2) {
                balas.remove(i);
                player2.perdeVida();;
                if (player2.getVidas()<= 0) {
                    player1Wins++;
                    playPlayer1WinSound();
                    resetRound("Player 1 venceu o round!");
                }
                continue;
            }

            // Remove a bala se estiver fora da tela
            if (!bala.isOnScreen(WIDTH, HEIGHT)) {
                balas.remove(i);
            }

            // Verifica colisão da bala com os obstáculos
            for (Obstaculo obstaculo : obstaculos) {
                if (bala.getAreaColisao().intersects(obstaculo.getLimites())) {
                    balas.remove(i);
                    break;
                }
            }
        }

        // Verifica colisão dos tanques com os obstáculos
        for (Obstaculo obstaculo : obstaculos) {
            if (player1.getRectangle().intersects(obstaculo.getLimites())) {
                player1.restaurarPosicao();
            }
            if (player2.getRectangle().intersects(obstaculo.getLimites())) {
                player2.restaurarPosicao();
            }
        }

        // Atualiza a tela
        repaint();
    }

    private void processPlayerMovement() {
        synchronized (this) {
            // Movimenta o jogador 1
            if (player1Keys.contains(KeyEvent.VK_W)) player1.setDy(-player1.getVelocidade());
            else if (player1Keys.contains(KeyEvent.VK_S)) player1.setDy(player1.getVelocidade());
            else player1.setDy(0);

            if (player1Keys.contains(KeyEvent.VK_A)) player1.setDx(-player1.getVelocidade());
            else if (player1Keys.contains(KeyEvent.VK_D)) player1.setDx(player1.getVelocidade());
            else player1.setDx(0);

            // Disparo do jogador 1
            for (Integer key : player1Keys) {
                player1.handleKeyPress(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED), true, balas);
            }

            // Movimenta o jogador 2
            if (player2Keys.contains(KeyEvent.VK_UP)) player2.setDy(-player2.getVelocidade());
            else if (player2Keys.contains(KeyEvent.VK_DOWN)) player2.setDy(player2.getVelocidade());
            else player2.setDy(0);

            if (player2Keys.contains(KeyEvent.VK_LEFT)) player2.setDx(-player2.getVelocidade());
            else if (player2Keys.contains(KeyEvent.VK_RIGHT)) player2.setDx(player2.getVelocidade());
            else player2.setDx(0);

            // Disparo do jogador 2
            for (Integer key : player2Keys) {
                player2.handleKeyPress(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED), false, balas);
            }

            player1.mover();
            player2.mover();
        }
    }

    private void resetRound(String message) {
        // Mostra mensagem de encerramento do round
        JOptionPane.showMessageDialog(this, message, "Round Encerrado", JOptionPane.INFORMATION_MESSAGE);
        currentRound++;
        if (currentRound > rounds) {
            declareWinner();
        } else {
            // Reseta os tanques e limpa as balas
            player1.reset(100, 300);
            player2.reset(700, 300);
            balas.clear();
        }
    }

    private void declareWinner() {
        // Declara o vencedor do jogo
        String winner = player1Wins > player2Wins ? "Player 1 é o grande vencedor!" :
                player2Wins > player1Wins ? "Player 2 é o grande vencedor!" :
                        "O jogo terminou empatado!";

        JOptionPane.showMessageDialog(this, winner, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    public void playBackgroundMusic() {
        try {
            File file = new File("src/sounds/background.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playPlayer1WinSound() {
        try {
            File file = new File("src/sounds/player-1-wins.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playPlayer2WinSound() {
        try {
            File file = new File("src/sounds/player-2-wins.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPlayer1Key(int keyCode) {
        return keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_A ||
                keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_D ||
                keyCode == KeyEvent.VK_SPACE;
    }

    private boolean isPlayer2Key(int keyCode) {
        return keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                keyCode == KeyEvent.VK_ENTER;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Configura a janela do menu de jogo
            JFrame frame = new JFrame("Combat Game Menu");
            String input = JOptionPane.showInputDialog(frame, "Quantos rounds deseja jogar?", "Configurar Jogo", JOptionPane.QUESTION_MESSAGE);

            int rounds = 1; // Valor padrão
            try {
                rounds = Integer.parseInt(input);
                if (rounds <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Entrada inválida! Usando 1 round como padrão.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

            // Configura a janela principal do jogo
            JFrame gameFrame = new JFrame("Combat Game");
            Game game = new Game(rounds);

            gameFrame.add(game);
            gameFrame.pack();
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setVisible(true);
        });
    }
}