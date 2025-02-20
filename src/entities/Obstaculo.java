package entities;

import java.awt.*;

public class Obstaculo {
    private int x, y, largura, altura;

    public Obstaculo(int x, int y, int largura, int altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    // Método para desenhar o obstáculo na tela
    public void desenhar(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(x, y, largura, altura);
    }

    // Retorna os limites do obstáculo como um retângulo
    public Rectangle getLimites() {
        return new Rectangle(x, y, largura, altura);
    }
}
