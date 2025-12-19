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
        btnStart   = new IconButton("/ui/menu/start_new_game_btn.png", true);
        btnHistory = new IconButton("/ui/menu/view_game_history_btn.png", true);
        btnHowTo   = new IconButton("/ui/menu/how_to_play_btn.png", true);
        btnAdmin   = new IconButton("/ui/menu/question_manager_btn.png", true);

        btnStart.setOnClick(() -> { if (listener != null) listener.onStartGameClicked(); });
        btnHistory.setOnClick(() -> { if (listener != null) listener.onHistoryClicked(); });
        btnHowTo.setOnClick(() -> { if (listener != null) listener.onHowToPlayClicked(); });
        btnAdmin.setOnClick(() -> { if (listener != null) listener.onManageQuestionsClicked(); });

        bg.add(btnStart);
        bg.add(btnHistory);
        bg.add(btnHowTo);
        bg.add(btnAdmin);
    }

    @Override
    public void doLayout() {
        super.doLayout();

        int W = getWidth();
        int H = getHeight();

        int btnW = Math.min(620, (int)(W * 0.62));
        int btnH = 74;
        int gap  = 24;

        int totalH = btnH * 4 + gap * 3;
        int x = (W - btnW) / 2;
        int y = (H - totalH) / 2 + 70; // push down under title

        btnStart.setBounds(
                140,   // X
                190,   // Y
                600,   // WIDTH
                60     // HEIGHT
        );

        // VIEW GAME HISTORY
        btnHistory.setBounds(
                140,
                270,
                600,
                60
        );

        // HOW TO PLAY
        btnHowTo.setBounds(
                140,
                350,
                600,
                60
        );

        // QUESTION MANAGER
        btnAdmin.setBounds(
                140,
                430,
                600,
                60
        );
    }
}
