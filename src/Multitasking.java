import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;
import java.util.TimerTask;

public class Multitasking {
    public static void main(String[] args) {
        FrameMT frameMT = new FrameMT();
        frameMT.setVisible(true);
    }
}

class Time extends TimerTask {
    BgMeteor panel;
    public Time(BgMeteor panel){
        this.panel = panel;
    }
    @Override
    public void run() {
        for (int i = 0; i < panel.showGhost; i++) {
            int randox = new Random().nextInt(40) - 20;
            int randoy = new Random().nextInt(40) - 20;
            panel.ghostX[i] += randox;
            panel.ghostY[i] += randoy;
        }
        panel.repaint();
    }
}

class FrameMT extends JFrame{
    BgMeteor bgmeteor = new BgMeteor();
    public FrameMT(){
        setSize(1000,563);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        add(bgmeteor);
    }
}

class BgMeteor extends JPanel implements MouseMotionListener, MouseListener {

    // ขยับ
    int showGhost = 10;
    int [] ghostX = new int[showGhost];
    int [] ghostY = new int[showGhost];

    // สมูท
    int [] SGhostY = new int[showGhost];
    int [] SGhostX = new int[showGhost];

    // หลายภาพ
    Image[] meteorite = new Image[10];
    int[] meteoriteType = new int[showGhost];

    // อยู่หรือหาย
    boolean[] alive = new boolean[showGhost];
    boolean[] exploding = new boolean[showGhost]; // กำลังระเบิด

    Image bombGif; // GIF ระเบิด

    public BgMeteor() {
        setSize(990, 527);
        setLocation(0, 0);

        addMouseMotionListener(this);
        addMouseListener(this);

        bombGif = new ImageIcon(System.getProperty("user.dir") + File.separator + "bomb.gif").getImage();

        for (int i = 0; i < meteorite.length; i++) {
            meteorite[i] = Toolkit.getDefaultToolkit().createImage(
                    System.getProperty("user.dir") + File.separator + (i+1) + ".png");
        }

        Random random = new Random();
        for (int i = 0; i < showGhost; i++) {

            ghostX[i] = (int) (Math.random() * 950);
            ghostY[i] = (int) (Math.random() * 500);

            SGhostX[i] = random.nextInt(7) - 3;
            SGhostY[i] = random.nextInt(7) - 3;

            meteoriteType[i] = random.nextInt(meteorite.length); // สุ่มเลือกภาพ
            alive[i] = true;
        }

        Timer time = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        time.start();
    }

    long[] explodeStart = new long[showGhost];
    int explodeDuration = 500;
    private void update() {
        int ghostSize = 50;

        for (int i = 0; i < showGhost; i++) {
            // ถ้ากำลังระเบิด
            if (exploding[i]) {
                // ตรวจสอบเวลาที่เล่นระเบิด
                if (System.currentTimeMillis() - explodeStart[i] > explodeDuration) {
                    exploding[i] = false;
                    ghostX[i] = -1000;
                    ghostY[i] = -1000;
                }
                continue; // ไม่ต้องอัปเดตตำแหน่ง
            }

            if (!alive[i]) continue; // ข้ามลูกที่หายไป

            // --- อัปเดตตำแหน่งปกติ ---
            ghostX[i] += SGhostX[i];
            ghostY[i] += SGhostY[i];

            // ขอบซ้าย-ขวา
            if (ghostX[i] < 0) {
                ghostX[i] = 0;
                SGhostX[i] = -SGhostX[i];
                SGhostX[i] *= 1.2;
            } else if (ghostX[i] > getWidth() - ghostSize) {
                ghostX[i] = getWidth() - ghostSize;
                SGhostX[i] = -SGhostX[i];
                SGhostX[i] *= 1.2;
            }

            // ขอบบน-ล่าง
            if (ghostY[i] < 0) {
                ghostY[i] = 0;
                SGhostY[i] = -SGhostY[i];
                SGhostY[i] *= 1.2;
            } else if (ghostY[i] > getHeight() - ghostSize) {
                ghostY[i] = getHeight() - ghostSize;
                SGhostY[i] = -SGhostY[i];
                SGhostY[i] *= 1.2;
            }

            int maxSpeed = 15;
            SGhostX[i] = Math.max(-maxSpeed, Math.min(maxSpeed, SGhostX[i]));
            SGhostY[i] = Math.max(-maxSpeed, Math.min(maxSpeed, SGhostY[i]));

            // ตรวจสอบการชนกัน
            for (int m = 0; m < showGhost; m++) {
                if (!alive[m]) continue;
                Rectangle r1 = new Rectangle(ghostX[m], ghostY[m], ghostSize, ghostSize);

                for (int j = m + 1; j < showGhost; j++) {
                    if (!alive[j]) continue;
                    Rectangle r2 = new Rectangle(ghostX[j], ghostY[j], ghostSize, ghostSize);

                    if (r1.intersects(r2)) {
                        int toRemove;
                        if (new Random().nextBoolean()) {
                            toRemove = m;
                        } else {
                            toRemove = j;
                        }
                        alive[toRemove] = false;
                        SGhostX[toRemove] = 0;
                        SGhostY[toRemove] = 0;
                        exploding[toRemove] = true;
                        explodeStart[toRemove] = System.currentTimeMillis(); // บันทึกเวลาเริ่มระเบิด

                        int survivor;
                        if (toRemove == m) {
                            survivor = j;
                        } else {
                            survivor = m;
                        }
                        SGhostX[survivor] = -SGhostX[survivor];
                        SGhostY[survivor] = -SGhostY[survivor];
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

        for (int i = 0; i < showGhost; i++) {
            if (exploding[i]) {
                g.drawImage(bombGif, ghostX[i], ghostY[i], 50, 50, this); // วาด GIF ระเบิด
            } else if (alive[i]) {
                Image img = meteorite[meteoriteType[i]];
                if (img != null) {
                    g.drawImage(img, ghostX[i], ghostY[i], 50, 50, this);
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillOval(ghostX[i], ghostY[i], 50, 50);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int ChilckX = e.getX();
        int ChilckY = e.getY();
        System.out.println(ChilckX + " " + ChilckY);
    }
    @Override
    public void mouseMoved(MouseEvent e) {
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {}
}
