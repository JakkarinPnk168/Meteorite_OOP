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
    public  Time(BgMeteor panel){
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

class  BgMeteor extends JPanel implements MouseMotionListener, MouseListener {
    Image ghost = Toolkit.getDefaultToolkit().createImage(System.getProperty("user.dir") +
            File.separator + "met.png");
    //ขยับ
    int showGhost = 10;
    int [] ghostX = new int[showGhost];
    int [] ghostY = new int[showGhost];

    //สมูท
    int [] SGhostY = new int[showGhost];
    int [] SGhostX = new int[showGhost];

    public BgMeteor() {
        setSize(990, 527);
        setLocation(0, 0);

        addMouseMotionListener(this);
        addMouseListener(this);

        Random random = new Random();
        for (int i = 0; i < showGhost; i++) {

            ghostX[i] = (int) (Math.random() * 950);
            ghostY[i] = (int) (Math.random() * 500);

            SGhostX[i] = random.nextInt(7) - 3;
            SGhostY[i] = random.nextInt(7) - 3;
        }

        Timer time = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        time.start();
    }

    private void update() {
        for (int i = 0; i < showGhost; i++) {
            ghostX[i] += SGhostX[i];
            ghostY[i] += SGhostY[i];

            int ghostSize = 50;

            //  ขอบซ้าย-ขวา
            if (ghostX[i] < 0) {
                ghostX[i] = 0;
                SGhostX[i] = -SGhostX[i];
            } else if (ghostX[i] > getWidth() - ghostSize) {
                ghostX[i] = getWidth() - ghostSize;
                SGhostX[i] = -SGhostX[i];
            }

            //  ขอบบน-ล่าง
            if (ghostY[i] < 0) {
                ghostY[i] = 0;
                SGhostY[i] = -SGhostY[i];
            } else if (ghostY[i] > getHeight() - ghostSize) {
                ghostY[i] = getHeight() - ghostSize;
                SGhostY[i] = -SGhostY[i];
            }
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < showGhost; i++) {
            g.drawImage(ghost, ghostX[i], ghostY[i], 50, 50, this);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int ChilckX = e.getX();
        int ChilckY = e.getY();
        System.out.println(ChilckX+ " "+ChilckY);
    }
    @Override
    public void mouseMoved(MouseEvent e) {
//        System.out.println(e.getX()+" "+e.getY());
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