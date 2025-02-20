package entities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import javax.sound.sampled.*;

public class Tanque {
    private int x, y;
    private int previousX, previousY; // Armazena a posição anterior
    private final int tamanho = 40;
    private final int velocidade = 5;
    private Color cor;
    private int dx = 0, dy = 0;
    private int shootKey;
    private int vidas;

    public int getVelocidade() {
        return velocidade;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }

    private long tempoUltimoDisparo; // Armazena o momento do último disparo

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
    }

    public void draw(Graphics g) {
        // Desenho do corpo do tanque
        g.setColor(cor);
        g.fillRect(x, y, tamanho, tamanho); // Corpo principal do tanque

        // Desenho das rodas do tanque
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y - 5, tamanho, 10); // Roda superior
        g.fillRect(x, y + tamanho - 5, tamanho, 10); // Roda inferior

        // Desenho do canhão
        g.setColor(Color.GRAY);
        if (cor == Color.RED) {
            // Canhão do Player 1 (direcionado para a direita)
            g.fillRect(x + tamanho, y + tamanho / 2 - 5, 20, 10);
        } else {
            // Canhão do Player 2 (direcionado para a esquerda)
            g.fillRect(x - 20, y + tamanho / 2 - 5, 20, 10);
        }
    }

    public void handleKeyPress(KeyEvent e, boolean isPlayer1, ArrayList<Bala> balas) {
        if (isPlayer1) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W -> dy = -velocidade;
                case KeyEvent.VK_S -> dy = velocidade;
                case KeyEvent.VK_A -> dx = -velocidade;
                case KeyEvent.VK_D -> dx = velocidade;
            }
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP -> dy = -velocidade;
                case KeyEvent.VK_DOWN -> dy = velocidade;
                case KeyEvent.VK_LEFT -> dx = -velocidade;
                case KeyEvent.VK_RIGHT -> dx = velocidade;
            }
        }

        // Verifica se a tecla de disparo foi pressionada
        if (e.getKeyCode() == shootKey) {
            long tempoAtual = System.currentTimeMillis();
            if (tempoAtual - tempoUltimoDisparo >= 200) { // Disparo a cada 200ms
                // Cria uma bala e adiciona ao jogo
                int bulletDx = (cor == Color.RED) ? 1 : -1; // Direção baseada no jogador
                balas.add(new Bala(x + (bulletDx > 0 ? tamanho : -10), y + tamanho / 2 - 5, cor, this, bulletDx));

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
        return new Rectangle(x, y, tamanho, tamanho);
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
    }
}
