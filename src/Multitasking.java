import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;

public class Multitasking {
    public static void main(String[] args) {
        FrameMT frameMT = new FrameMT();
        frameMT.setVisible(true);
    }
}

class FrameMT extends JFrame {
    BgMeteor bgmeteor = new BgMeteor(10); // กำหนดจำนวนอุกกาบาตได้

    public FrameMT() {
        setSize(1000, 563);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        add(bgmeteor);
    }
}

class Meteor implements Runnable {
    int x, y;
    int dx, dy;
    int size = 50;
    boolean alive = true;
    boolean exploding = false;
    long explodeStart;
    int explodeDuration = 500; // ms
    Image img;
    Image bombGif;
    BgMeteor panel;
    Random rand = new Random();

    public Meteor(BgMeteor panel, Image img, Image bombGif) {
        this.panel = panel;
        this.img = img;
        this.bombGif = bombGif;

        // เริ่มตำแหน่งสุ่ม
        this.x = rand.nextInt(Math.max(1, panel.getWidth() - size));
        this.y = rand.nextInt(Math.max(1, panel.getHeight() - size));

        // ความเร็วเริ่มต้น (สุ่มไม่เท่ากัน)
        this.dx = rand.nextInt(7) - 3;
        this.dy = rand.nextInt(7) - 3;
        if (dx == 0 && dy == 0) {
            dx = 2; // กันหยุดนิ่ง
            dy = 1;
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (exploding) {
                    if (System.currentTimeMillis() - explodeStart > explodeDuration) {
                        // หมดเวลาระเบิด → ไม่วาดอีก
                        exploding = false;
                        alive = false;
                    }
                } else if (alive) {
                    move();
                }

                panel.repaint();
                Thread.sleep(30); // ประมาณ 33 fps
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void move() {
        x += dx;
        y += dy;

        // ชนขอบ
        if (x < 0 || x > panel.getWidth() - size) {
            dx = -dx;
            dx *= 1.2; // เพิ่มความเร็ว
        }
        if (y < 0 || y > panel.getHeight() - size) {
            dy = -dy;
            dy *= 1.2;
        }

        // จำกัดความเร็วสูงสุด
        int maxSpeed = 15;
        dx = Math.max(-maxSpeed, Math.min(maxSpeed, dx));
        dy = Math.max(-maxSpeed, Math.min(maxSpeed, dy));
    }

    public void explode() {
        if (!alive || exploding) return; // ป้องกันซ้ำ
        exploding = true;
        explodeStart = System.currentTimeMillis();
        dx = 0;
        dy = 0;
    }

    public void bounce() {
        dx = -dx;
        dy = -dy;
        // กันหยุดนิ่ง
        if (dx == 0) dx = rand.nextInt(3) + 1;
        if (dy == 0) dy = rand.nextInt(3) + 1;

        // เพิ่มแรงกระแทก
        dx *= 1.2;
        dy *= 1.2;

        // จำกัดความเร็วสูงสุด
        int maxSpeed = 15;
        dx = Math.max(-maxSpeed, Math.min(maxSpeed, dx));
        dy = Math.max(-maxSpeed, Math.min(maxSpeed, dy));
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public void draw(Graphics g) {
        if (alive) {
            if (exploding) {
                g.drawImage(bombGif, x, y, size, size, panel);
            } else {
                g.drawImage(img, x, y, size, size, panel);
            }
        }
    }
}

class BgMeteor extends JPanel implements MouseListener {
    Meteor[] meteors;
    Image[] meteorite = new Image[10];
    Image bombGif;
    Random rand = new Random();

    public BgMeteor(int count) {
        setSize(990, 527);
        setLocation(0, 0);
        addMouseListener(this);

        bombGif = new ImageIcon(System.getProperty("user.dir") + File.separator + "bomb.gif").getImage();
        for (int i = 0; i < meteorite.length; i++) {
            meteorite[i] = Toolkit.getDefaultToolkit().createImage(
                    System.getProperty("user.dir") + File.separator + (i + 1) + ".png");
        }

        meteors = new Meteor[count];
        for (int i = 0; i < count; i++) {
            meteors[i] = new Meteor(this,
                    meteorite[rand.nextInt(meteorite.length)], bombGif);
            new Thread(meteors[i]).start();
        }

        // Thread ตรวจสอบการชน
        new Thread(() -> {
            while (true) {
                checkCollision();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void checkCollision() {
        for (int i = 0; i < meteors.length; i++) {
            if (!meteors[i].alive) continue;
            Rectangle r1 = meteors[i].getBounds();
            for (int j = i + 1; j < meteors.length; j++) {
                if (!meteors[j].alive) continue;
                Rectangle r2 = meteors[j].getBounds();

                if (r1.intersects(r2)) {
                    // ให้ระเบิดแค่ลูกเดียว อีกลูกไม่เปลี่ยนทิศทาง
                    if (!meteors[i].exploding && !meteors[j].exploding) {
                        if (rand.nextBoolean()) {
                            meteors[i].explode();
                            // meteors[j] เคลื่อนที่ต่อไปตามเดิม
                        } else {
                            meteors[j].explode();
                            // meteors[i] เคลื่อนที่ต่อไปตามเดิม
                        }
                    }
                }
            }
        }
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (Meteor m : meteors) {
            m.draw(g);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("Click at: " + e.getX() + "," + e.getY());
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
