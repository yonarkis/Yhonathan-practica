package Vista;

import java.awt.BorderLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public final class UiStyles {

    public static final Color COLOR_PRIMARY = new Color(56, 106, 255);
    public static final Color COLOR_PRIMARY_DARK = new Color(30, 62, 176);
    public static final Color COLOR_ACCENT = new Color(0, 182, 161);
    public static final Color COLOR_BG = new Color(242, 246, 255);
    private static final int BASE_WIDTH = 1060;
    private static final int BASE_HEIGHT = 680;
    private static boolean globalFontApplied = false;
    private static final String[] LOGO_CANDIDATOS = {
        "assets/logo_institucion.png",
        "assets/logo.png",
        "logo_institucion.png",
        "logo.png"
    };

    private UiStyles() {
    }

    public static void applyWindowDefaults(JFrame frame) {
        applyGlobalFont();
        frame.setMinimumSize(new Dimension(980, 620));
        frame.setSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        Image icon = loadInstitutionLogoImage(48, 48);
        if (icon != null) {
            frame.setIconImage(icon);
        }
    }

    public static JLabel titleLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.BOLD, 24));
        l.setForeground(COLOR_PRIMARY_DARK);
        return l;
    }

    public static JLabel subtitleLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setForeground(new Color(90, 90, 90));
        return l;
    }

    public static JButton primaryButton(String txt) {
        JButton b = new JButton(txt);
        b.setBackground(COLOR_PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(180, 36));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(COLOR_PRIMARY_DARK);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(COLOR_PRIMARY);
            }
        });
        return b;
    }

    public static void card(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 221, 245)),
                        BorderFactory.createEmptyBorder(1, 1, 1, 1)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        c.setBackground(Color.WHITE);
    }

    public static JPanel topBar(String title) {
        JPanel top = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, COLOR_PRIMARY, getWidth(), getHeight(), COLOR_ACCENT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        ImageIcon logo = loadInstitutionLogoIcon(42, 42);
        JLabel left = new JLabel(logo == null ? "GZ" : "");
        left.setForeground(Color.WHITE);
        left.setFont(new Font("SansSerif", Font.BOLD, 14));
        left.setPreferredSize(new Dimension(42, 42));
        left.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        left.setOpaque(logo == null);
        left.setBackground(new Color(255, 255, 255, 32));

        JLabel lbl = new JLabel(title);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));

        top.add(left, BorderLayout.WEST);
        top.add(lbl, BorderLayout.CENTER);
        return top;
    }

    public static ImageIcon loadInstitutionLogoIcon(int width, int height) {
        Image image = loadInstitutionLogoImage(width, height);
        return image == null ? null : new ImageIcon(image);
    }

    private static Image loadInstitutionLogoImage(int width, int height) {
        URL resource = UiStyles.class.getClassLoader().getResource("assets/logo_institucion.png");
        if (resource != null) {
            return new ImageIcon(resource).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        for (String path : LOGO_CANDIDATOS) {
            File file = new File(path);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            }
        }
        return null;
    }

    private static void applyGlobalFont() {
        if (globalFontApplied) {
            return;
        }
        FontUIResource resource = new FontUIResource("SansSerif", Font.PLAIN, 14);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, resource);
            }
        }
        globalFontApplied = true;
    }
}
