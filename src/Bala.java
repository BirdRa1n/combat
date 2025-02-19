import java.awt.*;

public class Bala {
    private int x, y;
    private final int tamanho = 10;
    private final int velocidade = 12;
    private Color color;
    private int dx;
    private Tanque owner;

    public Bala(int x, int y, Color color, Tanque owner, int dx) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.owner = owner;
        this.dx = dx;
    }

    // Método para desenhar o projétil
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x, y, tamanho, tamanho);
    }

    // Método para mover o projétil
    public void mover() {
        x += dx * velocidade;
    }

    // Retorna os limites do projétil para verificação de colisão
    public Rectangle getAreaColisao() {
        return new Rectangle(x, y, tamanho, tamanho);
    }

    // Verifica se o projétil colidiu com um tanque
    public boolean getAreaColisao(Tanque tanque) {
        return getAreaColisao().intersects(tanque.getRectangle());
    }

    // Verifica se o projétil ainda está dentro da tela
    public boolean isOnScreen(int width, int height) {
        return x >= 0 && x <= width && y >= 0 && y <= height;
    }

    // Retorna o tanque que disparou o projétil
    public Tanque getOwner() {
        return owner;
    }
}
