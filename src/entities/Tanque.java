package entities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Tanque {
    private int x, y;
    private int previousX, previousY;
    private final int tamanho = 70;
    private final int velocidade = 7;
    private Color cor;
    private int dx = 0, dy = 0;
    private int shootKey;
    private int vidas;
    private double angle = 0; // Ângulo de rotação do tanque
    private Image tanqueImage; // Imagem do tanque

    public int getVelocidade() {
        return velocidade;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }

    private long tempoUltimoDisparo;

    public long getTempoUltimoDisparo() {
        return tempoUltimoDisparo;
    }

    public void setTempoUltimoDisparo(long time) {
        this.tempoUltimoDisparo = time;
    }

    public Tanque(int x, int y, Color color, int shootKey) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.cor = color;
        this.shootKey = shootKey;
        this.vidas = 3;
        this.tempoUltimoDisparo = 0;

        // Define o ângulo inicial com base na cor do tanque
        if (color == Color.BLUE) {
            this.angle = Math.PI; // 180 graus (apontado para a esquerda)
        } else {
            this.angle = 0; // 0 graus (apontado para a direita)
        }

        // Carrega a imagem do tanque
        try {
            tanqueImage = ImageIO.read(new File("src/resources/images/tanque.png"));
            // Aumenta o tamanho da imagem em 2x
            Image scaledImage = tanqueImage.getScaledInstance(tamanho * 2, tamanho, Image.SCALE_SMOOTH);
            tanqueImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = ((BufferedImage) tanqueImage).createGraphics();
            g2d.drawImage(scaledImage, 25, 0, null);
            g2d.dispose();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar a imagem do tanque.");
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x + tamanho / 2, y + tamanho / 2); // Move o ponto de rotação para o centro do tanque
        g2d.rotate(angle); // Aplica a rotação

        // Desenha a imagem do tanque
        if (tanqueImage != null) {
            g2d.drawImage(tanqueImage, -tamanho / 2, -tamanho / 2, tamanho, tamanho, null);
        } else {
            // Caso a imagem não seja carregada, desenha um retângulo como fallback
            g2d.setColor(cor);
            g2d.fillRect(-tamanho / 2, -tamanho / 2, tamanho, tamanho);
        }

        g2d.dispose();
    }

    public void handleKeyPress(KeyEvent e, boolean isPlayer1, ArrayList<Bala> balas) {
        if (isPlayer1) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W -> dy = -velocidade;
                case KeyEvent.VK_S -> dy = velocidade;
                case KeyEvent.VK_A -> dx = -velocidade;
                case KeyEvent.VK_D -> dx = velocidade;
                case KeyEvent.VK_Q -> angle -= Math.toRadians(5); // Rotação para a esquerda
                case KeyEvent.VK_E -> angle += Math.toRadians(5); // Rotação para a direita
            }
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP -> dy = -velocidade;
                case KeyEvent.VK_DOWN -> dy = velocidade;
                case KeyEvent.VK_LEFT -> dx = -velocidade;
                case KeyEvent.VK_RIGHT -> dx = velocidade;
                case KeyEvent.VK_N -> angle -= Math.toRadians(5); // Rotação para a esquerda
                case KeyEvent.VK_M -> angle += Math.toRadians(5); // Rotação para a direita
            }
        }

        // Verifica se a tecla de disparo foi pressionada
        if (e.getKeyCode() == shootKey) {
            long tempoAtual = System.currentTimeMillis();
            if (tempoAtual - tempoUltimoDisparo >= 200) { // Disparo a cada 200ms
                // Cria uma bala e adiciona ao jogo
                int bulletDx = (int) (Math.cos(angle) * 10); // Direção baseada no ângulo
                int bulletDy = (int) (Math.sin(angle) * 10);
                balas.add(new Bala(x + tamanho / 2, y + tamanho / 2, cor, this, bulletDx, bulletDy));

                // Atualiza o tempo do último disparo
                playSomDisparo();
                tempoUltimoDisparo = tempoAtual;
            }
        }
    }

    public void mover() {
        // Armazena a posição atual antes de mover
        previousX = x;
        previousY = y;

        x += dx;
        y += dy;

        // Limita os movimentos dentro da tela
        x = Math.max(0, Math.min(x, 800 - tamanho));
        y = Math.max(0, Math.min(y, 600 - tamanho));
    }

    public void restaurarPosicao() {
        // Restaura a posição anterior
        x = previousX;
        y = previousY;
    }

    public void resetMovimento(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_S -> dy = 0;
            case KeyEvent.VK_A, KeyEvent.VK_D -> dx = 0;
            case KeyEvent.VK_UP, KeyEvent.VK_DOWN -> dy = 0;
            case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT -> dx = 0;
        }
    }

    public void playSomDisparo() {
        try {
            File soundFile = new File("src/resources/sounds/shot.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = audioStream.getFormat();
            int frames = (int) (audioStream.getFrameLength() * 0.5);
            byte[] audioData = new byte[frames * format.getFrameSize()];
            audioStream.read(audioData);
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream2 = new AudioInputStream(bais, format, frames);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream2);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Rectangle getRectangle() {
        int hitboxSize = 35; // Tamanho do hitbox reduzido
        int offset = (tamanho - hitboxSize) / 2; // Centraliza o hitbox
        int width = cor == Color.BLUE ? hitboxSize : hitboxSize + 20;
        return new Rectangle(x + offset, y + offset, width, hitboxSize);
    }

    public int getVidas() {
        return vidas;
    }

    public void perdeVida() {
        vidas--;
    }

    public void reset(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.previousX = startX;
        this.previousY = startY;
        this.vidas = 3;
        this.dx = 0;
        this.dy = 0;

        // Reseta o ângulo com base na cor do tanque
        if (this.cor == Color.BLUE) {
            this.angle = Math.PI; // 180 graus (apontado para a esquerda)
        } else {
            this.angle = 0; // 0 graus (apontado para a direita)
        }
    }
}
