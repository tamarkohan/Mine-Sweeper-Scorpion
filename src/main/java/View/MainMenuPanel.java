package View;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {

    public interface MainMenuListener {
        void onStartGameClicked();

        void onHistoryClicked();

        void onHowToPlayClicked();

        void onManageQuestionsClicked();
    }

    private final MainMenuListener listener;
    private final BackgroundPanel bg;

    private final IconButton btnStart;
    private final IconButton btnHistory;
    private final IconButton btnHowTo;
    private final IconButton btnAdmin;

    public MainMenuPanel(MainMenuListener listener) {
        this.listener = listener;

        setLayout(new BorderLayout());

        bg = new BackgroundPanel("/ui/menu/bg.png");
        bg.setLayout(null);
        add(bg, BorderLayout.CENTER);

        // all buttons are PNG image buttons now
        btnStart = new IconButton("/ui/menu/start_new_game_btn.png", true);
        btnHistory = new IconButton("/ui/menu/view_game_history_btn.png", true);
        btnHowTo = new IconButton("/ui/menu/how_to_play_btn.png", true);
        btnAdmin = new IconButton("/ui/menu/question_manager_btn.png", true);

        btnStart.setOnClick(() -> {
            if (listener != null) listener.onStartGameClicked();
        });
        btnHistory.setOnClick(() -> {
            if (listener != null) listener.onHistoryClicked();
        });
        btnHowTo.setOnClick(() -> {
            if (listener != null) listener.onHowToPlayClicked();
        });
        btnAdmin.setOnClick(() -> {
            if (listener != null) listener.onManageQuestionsClicked();
        });

        bg.add(btnStart);
        bg.add(btnHistory);
        bg.add(btnHowTo);
        bg.add(btnAdmin);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                revalidate();
                repaint();
            }
        });

    }

    @Override
    public void doLayout() {
        super.doLayout();

        bg.setBounds(0, 0, getWidth(), getHeight());

        int W = bg.getWidth();
        int H = bg.getHeight();

        // 👇 MANUAL CONTROLS (CHANGE THESE)
        int leftMargin = (int) (W * 0.18);   // move buttons left/right
        int topStart = (int) (H * 0.31);   // move buttons up/down
        int btnW = (int) (W * 0.62);   // button width
        int btnH = (int) (H * 0.090);  // button height
        int gap = (int) (H * 0.030);  // space between buttons

        int x = leftMargin;
        int y = topStart;

        btnStart.setBounds(x, y, btnW, btnH);
        y += btnH + gap;

        btnHistory.setBounds(x, y, btnW, btnH);
        y += btnH + gap;

        btnHowTo.setBounds(x, y, btnW, btnH);
        y += btnH + gap;

        btnAdmin.setBounds(x, y, btnW, btnH);
    }


}
