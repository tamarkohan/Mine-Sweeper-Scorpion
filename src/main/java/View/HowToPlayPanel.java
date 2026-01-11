package View;

import javax.swing.*;
import java.awt.*;

/**
 * How to Play panel displaying game instructions.
 * UI only - no game logic or state changes.
 * Follows MVC separation by only displaying information.
 */
public class HowToPlayPanel extends JPanel {

    public interface BackToMenuListener {
        void onBackToMenu();
    }

    private final BackToMenuListener listener;
    private BackgroundPanel bg;
    private NeonFramePanel contentFrame;  // Blue rectangle container
    private JScrollPane scrollPane;
    private JPanel contentPanel;          // Text content panel
    private NeonTextLabel titleLabel;
    private JLabel[] instructionLabels;
    private IconButton btnBack;

    // Layout constants - single container design
    private static final int CONTENT_PADDING_LEFT_RIGHT = 35;  // Padding inside blue rectangle
    private static final int CONTENT_PADDING_TOP_BOTTOM = 25;  // Padding inside blue rectangle
    private static final int MAX_CONTENT_WIDTH = 500;  // Limited width for readability
    private static final int SECTION_HEADER_GAP = 20;  // Space between sections
    private static final int BULLET_GAP = 8;  // Space between bullet points
    private static final int BULLET_INDENT = 24;  // Indentation for bullet points
    
    // Typography sizes - clean and readable
    private static final int TITLE_FONT_SIZE = 28;  // Slightly reduced
    private static final int SECTION_HEADER_FONT_SIZE = 16;  // Slightly reduced
    private static final int BULLET_FONT_SIZE = 13;  // Consistent
    private static final int SUB_BULLET_FONT_SIZE = 12;  // Slightly smaller

    // Instruction sections
    private static final String[] INSTRUCTIONS = {
        "HOW TO REVEAL A CELL",
        "• Left-click on any hidden cell to reveal it",
        "",
        "HOW TO PLACE/REMOVE A FLAG",
        "• Right-click on a cell to place a flag (marks suspected mine)",
        "• Right-click again on a flagged cell to remove the flag",
        "",
        "SCORE AND LIVES",
        "• Score and lives are shared between both players",
        "• Revealing safe cells adds to your score",
        "• Revealing a mine deducts a life",
        "• Remaining lives are converted to bonus score at game end",
        "",
        "SPECIAL CELLS",
        "• Question (Q): Reveal to unlock. Pay points to answer a quiz.",
        "  - Correct answer gives bonus points and lives",
        "  - Wrong answer may cost points and lives",
        "• Surprise (S): Reveal to unlock. Pay points for random effect.",
        "  - Can be positive (bonuses) or negative (penalties)"
    };

    public HowToPlayPanel(BackToMenuListener listener) {
        this.listener = listener;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Background panel with null layout for absolute positioning
        bg = new BackgroundPanel("/ui/start/bG.png");
        bg.setLayout(null);
        bg.setBackground(Color.BLACK);
        add(bg, BorderLayout.CENTER);

        // Create blue rectangle container (single boundary for all content)
        Color neonCyan = new Color(0, 255, 255);
        contentFrame = new NeonFramePanel(neonCyan, 0, 20);
        contentFrame.setOpaque(false);
        contentFrame.setLayout(new BorderLayout());

        // Content panel with vertical layout and padding
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Add inner padding inside blue rectangle
        contentPanel.setBorder(BorderFactory.createEmptyBorder(
            CONTENT_PADDING_TOP_BOTTOM,
            CONTENT_PADDING_LEFT_RIGHT,
            CONTENT_PADDING_TOP_BOTTOM,
            CONTENT_PADDING_LEFT_RIGHT
        ));

        // Title (centered within content panel)
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel = new NeonTextLabel("HOW TO PLAY", neonCyan);
        // Set reduced title font size
        titleLabel.setFont(new Font("Arial", Font.BOLD, TITLE_FONT_SIZE));
        titlePanel.add(titleLabel);
        contentPanel.add(titlePanel);
        contentPanel.add(Box.createVerticalStrut(SECTION_HEADER_GAP));

        // Instruction labels with proper spacing
        instructionLabels = new JLabel[INSTRUCTIONS.length];
        boolean prevWasBullet = false;
        boolean isFirstItem = true;

        for (int i = 0; i < INSTRUCTIONS.length; i++) {
            String text = INSTRUCTIONS[i];
            boolean isEmpty = text.isEmpty();
            
            if (isEmpty) {
                // Empty line indicates section break - spacing will be added before next header
                prevWasBullet = false;
                continue;
            }

            JLabel label = new JLabel(text);
            label.setOpaque(false);

            // Check if this is a section header (all caps, no bullet)
            boolean isHeader = text.toUpperCase().equals(text) && !text.startsWith("•");
            boolean isBullet = text.startsWith("•");
            
            // Add spacing before section headers (except the first one)
            if (isHeader && !isFirstItem) {
                contentPanel.add(Box.createVerticalStrut(SECTION_HEADER_GAP));
            }
            
            if (isHeader) {
                // Section header: cyan neon, bold, clean size
                label.setFont(new Font("Arial", Font.BOLD, SECTION_HEADER_FONT_SIZE));
                label.setForeground(neonCyan);
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                prevWasBullet = false;
            } else if (isBullet) {
                // Bullet point: white, regular font, indented
                // Add small gap before bullet if previous was also a bullet
                if (!isFirstItem && prevWasBullet) {
                    contentPanel.add(Box.createVerticalStrut(BULLET_GAP));
                }
                label.setFont(new Font("Arial", Font.PLAIN, BULLET_FONT_SIZE));
                label.setForeground(Color.WHITE);
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                label.setBorder(BorderFactory.createEmptyBorder(0, BULLET_INDENT, 0, 0));
                prevWasBullet = true;
            } else {
                // Sub-bullet (indented further): smaller font
                label.setFont(new Font("Arial", Font.PLAIN, SUB_BULLET_FONT_SIZE));
                label.setForeground(Color.WHITE);
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                label.setBorder(BorderFactory.createEmptyBorder(0, BULLET_INDENT + 20, 0, 0));
                prevWasBullet = false;
            }

            // Limit line width for readability - wrap long text
            label.setMaximumSize(new Dimension(MAX_CONTENT_WIDTH, Integer.MAX_VALUE));
            
            instructionLabels[i] = label;
            contentPanel.add(label);
            
            isFirstItem = false;
        }

        // Create transparent scroll pane for content inside blue rectangle
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);  // Transparent viewport
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(new Color(0, 0, 0, 0));  // Transparent background
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));  // Transparent viewport background
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Style scrollbar to match neon theme - subtle
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(0, 255, 255, 80);
                this.trackColor = new Color(0, 0, 0, 0);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        // Add scroll pane directly to blue rectangle
        contentFrame.add(scrollPane, BorderLayout.CENTER);

        // Back button (positioned outside frame)
        btnBack = new IconButton("/ui/icons/back.png");
        btnBack.setOnClick(() -> {
            if (listener != null) {
                listener.onBackToMenu();
            }
        });

        bg.add(contentFrame);
        bg.add(btnBack);

        // Layout components when panel is resized
        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutComponents();
            }
        });

        SwingUtilities.invokeLater(this::layoutComponents);
    }

    private void layoutComponents() {
        int W = bg.getWidth();
        int H = bg.getHeight();

        // Calculate blue rectangle size - fixed width based on content + padding
        int contentWidth = MAX_CONTENT_WIDTH + CONTENT_PADDING_LEFT_RIGHT * 2;
        int blueRectWidth = contentWidth;
        
        // Fixed height that fits most screens, scrolling handles overflow
        int blueRectHeight = Math.min(H - 120, 540);
        
        // Center the blue rectangle on screen
        int blueRectX = (W - blueRectWidth) / 2;
        int blueRectY = (H - blueRectHeight) / 2 - 10;
        
        contentFrame.setBounds(blueRectX, blueRectY, blueRectWidth, blueRectHeight);

        // Back button at bottom left (outside blue rectangle)
        int btnSize = (int)(Math.min(W, H) * 0.06);
        btnBack.setBounds((int)(W * 0.03), (int)(H * 0.92), btnSize, btnSize);

        bg.revalidate();
        bg.repaint();
    }
}
