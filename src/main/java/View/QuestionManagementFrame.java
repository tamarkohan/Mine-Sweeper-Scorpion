package View;

import Controller.GameController;
import Model.Question;
import Model.QuestionManager;
import util.LanguageManager;
import util.SoundToggleOverlay;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import util.SoundManager;

public class QuestionManagementFrame extends JFrame {

    private final QuestionManager manager;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JScrollPane tableScroll;

    private JComboBox<String> difficultyFilter;
    private JComboBox<String> correctAnswerFilter;
    private JLabel diffLabel;
    private JLabel corrLabel;
    private JLabel lblSortHint;
    private JButton clearBtn;
    private JPanel filterPanel;

    private final JButton btnAdd;
    private final JButton btnEdit;
    private final JButton btnDelete;

    private JButton burgerBtn;
    private JPanel drawerPanel;
    private boolean drawerOpen = false;
    private JButton drawerClearBtn;
    private JLabel drawerTitle;
    private JPanel glass;

    private final IconButton btnLanguage;
    private final JLabel toastLabel;
    private final Timer toastTimer;
    private static final String THINKING_ICON = "/ui/icons/thinking.png";

    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255);
    private static final Color HINT_COLOR = new Color(180, 180, 180);
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30);
    private static final Color TABLE_ROW_BG = new Color(20, 20, 20);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80);
    private static final Color DIALOG_BG = new Color(5, 6, 10);
    private static final Color DIALOG_PANEL = new Color(11, 15, 26);

    public QuestionManagementFrame(QuestionManager manager) {
        this(manager, null);
    }

    public QuestionManagementFrame(QuestionManager manager, Runnable onExitToMenu) {
        super("Question Management");
        this.manager = manager;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "closeWindow");
        getRootPane().getActionMap().put("closeWindow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                if (onExitToMenu != null) onExitToMenu.run();
            }
        });

        String[] cols = {"ID", "Text", "A", "B", "C", "D", "Correct", "Difficulty"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class;
                return String.class;
            }
        };

        table = createStyledTable(model);
        attachHeaderClickSound(table);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.setComparator(0, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));
        sorter.setComparator(6, (a, b) -> {
            String sa = a == null ? "" : a.toString();
            String sb = b == null ? "" : b.toString();
            return sa.compareTo(sb);
        });

        filterPanel = createFilterPanel();

        tableScroll = createStyledScrollPane(table);

        btnAdd = createStyledButton("Add");
        btnEdit = createStyledButton("Edit");
        btnDelete = createStyledButton("Delete");
        attachClickSound(btnAdd);
        attachClickSound(btnEdit);
        attachClickSound(btnDelete);

        IconButton btnExit = new IconButton("/ui/icons/back.png");
        btnExit.setPreferredSize(new Dimension(46, 46));
        btnExit.setSafePadPx(2);
        btnExit.setOnClick(() -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });

        btnLanguage = new IconButton("/ui/icons/language.png", true);
        btnLanguage.setPreferredSize(new Dimension(46, 46));
        btnLanguage.setOnClick(this::showLanguagePopup);

        btnAdd.addActionListener(e -> addQuestion());
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                editQuestion(table.convertRowIndexToModel(selectedRow));
            }
        });
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                deleteQuestion(table.convertRowIndexToModel(selectedRow));
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottomBar.add(btnExit, BorderLayout.WEST);
        bottomBar.add(buttonPanel, BorderLayout.CENTER);
        bottomBar.add(btnLanguage, BorderLayout.EAST);

        lblSortHint = new JLabel();
        lblSortHint.setForeground(HINT_COLOR);
        lblSortHint.setFont(new Font("Dialog", Font.ITALIC, 12));

        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        hintPanel.setOpaque(false);
        hintPanel.add(lblSortHint);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 30, 10, 30));
        tableWrapper.add(filterPanel, BorderLayout.NORTH);
        tableWrapper.add(hintPanel, BorderLayout.CENTER);

        JPanel mainTable = new JPanel(new BorderLayout());
        mainTable.setOpaque(false);
        mainTable.add(tableWrapper, BorderLayout.NORTH);
        mainTable.add(tableScroll, BorderLayout.CENTER);
        mainTable.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JPanel root = new BackgroundPanel("/ui/menu/question_management_bg.png");
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(100, 20, 10, 20));
        root.add(mainTable, BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);

        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setOpaque(true);
        toastLabel.setBackground(new Color(0, 0, 0, 180));
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        toastLabel.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        toastLabel.setVisible(false);
        getLayeredPane().add(toastLabel, JLayeredPane.POPUP_LAYER);

        setContentPane(root);
        SoundToggleOverlay.attach(this);

        setupGlassPane();
        createDrawerPanel();

        updateUIText();
        updateFilterComboItems();
        loadTable();

        toastTimer = new Timer(2000, e -> toastLabel.setVisible(false));
        toastTimer.setRepeats(false);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (drawerOpen) positionDrawer();
            }
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                if (drawerOpen) positionDrawer();
            }
        });

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private void setupGlassPane() {
        glass = new JPanel(null);
        glass.setOpaque(false);
        glass.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (drawerPanel != null && drawerPanel.isVisible()) {
                    Point p = SwingUtilities.convertPoint(glass, e.getPoint(), drawerPanel);
                    if (p.x < 0 || p.y < 0 || p.x > drawerPanel.getWidth() || p.y > drawerPanel.getHeight()) {
                        closeDrawer();
                    }
                }
            }
        });
        setGlassPane(glass);
        glass.setVisible(false);
    }

    private void openDrawer() {
        drawerOpen = true;
        glass.setVisible(true);
        drawerPanel.setVisible(true);
        positionDrawer();
        drawerPanel.requestFocusInWindow();
    }

    private void closeDrawer() {
        drawerOpen = false;
        if (drawerPanel != null) drawerPanel.setVisible(false);
        if (glass != null) glass.setVisible(false);
    }

    private void toggleDrawer() {
        if (drawerOpen) closeDrawer();
        else openDrawer();
    }

    private void positionDrawer() {
        if (drawerPanel == null || burgerBtn == null || glass == null) return;

        Point p = SwingUtilities.convertPoint(burgerBtn.getParent(), burgerBtn.getLocation(), glass);
        int w = 280;
        int h = drawerPanel.getPreferredSize().height;
        int x = p.x + burgerBtn.getWidth() - w;
        int y = p.y + burgerBtn.getHeight() + 8;

        drawerPanel.setSize(w, h);
        drawerPanel.setLocation(Math.max(10, x), Math.max(10, y));
        drawerPanel.revalidate();
        drawerPanel.repaint();
    }

    private void createDrawerPanel() {
        drawerPanel = new JPanel();
        drawerPanel.setLayout(new BoxLayout(drawerPanel, BoxLayout.Y_AXIS));
        drawerPanel.setBackground(new Color(5, 6, 10, 235));
        drawerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_COLOR, 1),
                new EmptyBorder(12, 12, 12, 12)
        ));
        drawerPanel.setVisible(false);

        drawerTitle = new JLabel("Filters");
        drawerTitle.setForeground(ACCENT_COLOR);
        drawerTitle.setFont(new Font("Dialog", Font.BOLD, 14));

        corrLabel = new JLabel("Correct:");
        corrLabel.setForeground(TEXT_COLOR);
        corrLabel.setFont(new Font("Dialog", Font.BOLD, 14));

        drawerClearBtn = createStyledButton("Clear");
        attachClickSound(drawerClearBtn);
        drawerClearBtn.addActionListener(e -> {
            difficultyFilter.setSelectedIndex(0);
            correctAnswerFilter.setSelectedIndex(0);
            applyFilters();
            closeDrawer();
        });

        drawerPanel.add(drawerTitle);
        drawerPanel.add(Box.createVerticalStrut(12));
        drawerPanel.add(corrLabel);
        drawerPanel.add(Box.createVerticalStrut(6));
        drawerPanel.add(correctAnswerFilter);
        drawerPanel.add(Box.createVerticalStrut(12));
        drawerPanel.add(drawerClearBtn);

        glass.add(drawerPanel);
    }

    private void showLanguagePopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(new Color(30, 30, 40));

        for (LanguageManager.Language lang : LanguageManager.Language.values()) {
            JMenuItem item = new JMenuItem(LanguageManager.getDisplayName(lang));
            item.setBackground(new Color(30, 30, 40));
            item.setForeground(Color.WHITE);
            item.setFont(new Font("Dialog", Font.PLAIN, 14));

            if (lang == GameController.getInstance().getCurrentLanguage()) {
                item.setForeground(ACCENT_COLOR);
                item.setFont(new Font("Dialog", Font.BOLD, 14));
            }

            item.addActionListener(e -> handleLanguageSelection(lang));
            popup.add(item);
        }

        popup.show(btnLanguage, 0, -popup.getPreferredSize().height);
    }

    private void handleLanguageSelection(LanguageManager.Language newLang) {
        if (newLang == GameController.getInstance().getCurrentLanguage()) return;

        btnLanguage.setIconPath(THINKING_ICON);
        btnLanguage.setOnClick(null);

        new Thread(() -> {
            try {
                GameController gc = GameController.getInstance();
                gc.setCurrentLanguage(newLang);
                gc.getQuestionManager().switchLanguageFromCache();
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    updateUIText();
                    updateFilterComboItems();
                    loadTable();
                    showLanguageToast();
                    btnLanguage.setIconPath("/ui/icons/language.png");
                    btnLanguage.setOnClick(this::showLanguagePopup);
                });
            }
        }).start();
    }

    private void addQuestion() {
        int newId = manager.getNextId();
        NeonQuestionDialog dialog = new NeonQuestionDialog(this, null, newId);
        dialog.setVisible(true);
        Question q = dialog.getResult();
        if (q != null) {
            try {
                manager.addOrReplaceQuestionBilingual(q);
            } catch (Exception ex) {
                manager.addOrReplaceQuestion(q);
            }
            loadTable();
            applyFilters();
        }
    }

    private void editQuestion(int row) {
        if (row < 0) return;
        Question existing = buildQuestionFromRow(row);
        if (existing == null) return;

        NeonQuestionDialog dialog = new NeonQuestionDialog(this, existing, existing.getId());
        dialog.setVisible(true);
        Question q = dialog.getResult();
        if (q != null) {
            try {
                manager.addOrReplaceQuestionBilingual(q);
            } catch (Exception ex) {
                manager.addOrReplaceQuestion(q);
            }
            loadTable();
            applyFilters();
        }
    }

    private void deleteQuestion(int row) {
        if (row < 0) return;

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        String title = getDeleteTitle(lang);
        String msg = getDeleteMessage(lang);

        SoundManager.exitDialog();
        Color accentBlue = new Color(0, 255, 255);

        boolean confirm = ConfirmDialog.show(this, title, msg, accentBlue, LanguageManager.isRTL(lang));
        if (!confirm) return;

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        manager.deleteQuestion(id);
        manager.saveQuestions();
        manager.loadQuestions();
        loadTable();
        applyFilters();
    }

    private String getDeleteTitle(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "מחיקת שאלה";
            case AR -> "حذف السؤال";
            case RU -> "Удалить вопрос";
            case ES -> "Eliminar pregunta";
            default -> "Delete Question";
        };
    }

    private String getDeleteMessage(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "האם אתה בטוח שברצונך למחוק את השאלה הזו?\nלא ניתן לבטל פעולה זו.";
            case AR -> "هل أنت متأكد أنك تريد حذف هذا السؤال؟\nلا يمكن الغاء هذا الإجراء.";
            case RU -> "Вы уверены, что хотите удалить этот вопрос?\nЭто действие нельзя отменить.";
            case ES -> "¿Estás seguro de que quieres eliminar esta pregunta?\nEsta acción no se puede deshacer.";
            default -> "Are you sure you want to delete this question?\nThis action cannot be undone.";
        };
    }

    private String getUITitle(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "ניהול שאלות";
            case AR -> "إدارة الأسئلة";
            case RU -> "Управление вопросами";
            case ES -> "Gestión de preguntas";
            default -> "Question Management";
        };
    }

    private String getAddText(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "הוסף";
            case AR -> "إضافة";
            case RU -> "Добавить";
            case ES -> "Añadir";
            default -> "Add";
        };
    }

    private String getEditText(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "ערוך";
            case AR -> "تعديل";
            case RU -> "Редактировать";
            case ES -> "Editar";
            default -> "Edit";
        };
    }

    private String getDeleteText(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "מחק";
            case AR -> "حذف";
            case RU -> "Удалить";
            case ES -> "Eliminar";
            default -> "Delete";
        };
    }

    private String getDiffLabelText(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "רמת קושי:";
            case AR -> "الصعوبة:";
            case RU -> "Сложность:";
            case ES -> "Dificultad:";
            default -> "Difficulty:";
        };
    }

    private String getCorrLabelText(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "תשובה נכונה:";
            case AR -> "الإجابة الصحيحة:";
            case RU -> "Правильный ответ:";
            case ES -> "Respuesta correcta:";
            default -> "Correct:";
        };
    }

    private String getClearText(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "נקה";
            case AR -> "مسح";
            case RU -> "Очистить";
            case ES -> "Limpiar";
            default -> "Clear";
        };
    }

    private String getFiltersTitle(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "מסננים";
            case AR -> "فلاتر";
            case RU -> "Фильтры";
            case ES -> "Filtros";
            default -> "Filters";
        };
    }

    private String[] getTableHeaders(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> new String[]{"מזהה", "טקסט", "א", "ב", "ג", "ד", "נכונה", "רמה"};
            case AR -> new String[]{"التسلسلي", "نص", "أ", "ب", "ج", "د", "الصحيح", "المستوى"};
            case RU -> new String[]{"ID", "Текст", "А", "Б", "В", "Г", "Правильный", "Сложность"};
            case ES -> new String[]{"ID", "Texto", "A", "B", "C", "D", "Correcta", "Dificultad"};
            default -> new String[]{"ID", "Text", "A", "B", "C", "D", "Correct", "Difficulty"};
        };
    }

    private void updateUIText() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        setTitle(getUITitle(lang));
        btnAdd.setText(getAddText(lang));
        btnEdit.setText(getEditText(lang));
        btnDelete.setText(getDeleteText(lang));

        diffLabel.setText(getDiffLabelText(lang));
        if (corrLabel != null) corrLabel.setText(getCorrLabelText(lang));
        clearBtn.setText(getClearText(lang));
        if (drawerClearBtn != null) drawerClearBtn.setText(getClearText(lang));
        if (drawerTitle != null) drawerTitle.setText(getFiltersTitle(lang));

        lblSortHint.setText(LanguageManager.get("sort_hint", lang));
        model.setColumnIdentifiers(getTableHeaders(lang));

        ComponentOrientation o = isRTL ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT;
        table.setComponentOrientation(o);
        table.getTableHeader().setComponentOrientation(o);
        if (tableScroll != null) {
            tableScroll.setComponentOrientation(o);
            tableScroll.getViewport().setComponentOrientation(o);
        }
        filterPanel.setComponentOrientation(o);
        rebuildFilterPanel(isRTL);
        if (drawerPanel != null) drawerPanel.setComponentOrientation(o);
        table.getTableHeader().resizeAndRepaint();
    }

    private void rebuildFilterPanel(boolean isRTL) {
        filterPanel.removeAll();
        FlowLayout layout = (FlowLayout) filterPanel.getLayout();
        layout.setAlignment(isRTL ? FlowLayout.RIGHT : FlowLayout.LEFT);

        if (isRTL) {
            filterPanel.add(clearBtn);
            filterPanel.add(burgerBtn);
            filterPanel.add(difficultyFilter);
            filterPanel.add(diffLabel);
        } else {
            filterPanel.add(diffLabel);
            filterPanel.add(difficultyFilter);
            filterPanel.add(burgerBtn);
            filterPanel.add(clearBtn);
        }

        filterPanel.revalidate();
        filterPanel.repaint();

        if (drawerOpen) positionDrawer();
    }

    private void updateFilterComboItems() {
        int diffIdx = difficultyFilter.getSelectedIndex();
        int corrIdx = correctAnswerFilter.getSelectedIndex();
        difficultyFilter.removeAllItems();
        correctAnswerFilter.removeAllItems();

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        switch (lang) {
            case HE -> {
                difficultyFilter.addItem("הכל");
                difficultyFilter.addItem("קל");
                difficultyFilter.addItem("בינוני");
                difficultyFilter.addItem("קשה");
                difficultyFilter.addItem("מומחה");
                correctAnswerFilter.addItem("הכל");
            }
            case AR -> {
                difficultyFilter.addItem("الكل");
                difficultyFilter.addItem("سهل");
                difficultyFilter.addItem("متوسط");
                difficultyFilter.addItem("صعب");
                difficultyFilter.addItem("خبير");
                correctAnswerFilter.addItem("الكل");
            }
            case RU -> {
                difficultyFilter.addItem("Все");
                difficultyFilter.addItem("Легкий");
                difficultyFilter.addItem("Средний");
                difficultyFilter.addItem("Сложный");
                difficultyFilter.addItem("Эксперт");
                correctAnswerFilter.addItem("Все");
            }
            case ES -> {
                difficultyFilter.addItem("Todos");
                difficultyFilter.addItem("Fácil");
                difficultyFilter.addItem("Medio");
                difficultyFilter.addItem("Difícil");
                difficultyFilter.addItem("Experto");
                correctAnswerFilter.addItem("Todos");
            }
            default -> {
                difficultyFilter.addItem("All");
                difficultyFilter.addItem("EASY");
                difficultyFilter.addItem("MEDIUM");
                difficultyFilter.addItem("HARD");
                difficultyFilter.addItem("EXPERT");
                correctAnswerFilter.addItem("All");
            }
        }

        correctAnswerFilter.addItem("A");
        correctAnswerFilter.addItem("B");
        correctAnswerFilter.addItem("C");
        correctAnswerFilter.addItem("D");

        if (diffIdx >= 0 && diffIdx < difficultyFilter.getItemCount()) difficultyFilter.setSelectedIndex(diffIdx);
        else difficultyFilter.setSelectedIndex(0);
        if (corrIdx >= 0 && corrIdx < correctAnswerFilter.getItemCount()) correctAnswerFilter.setSelectedIndex(corrIdx);
        else correctAnswerFilter.setSelectedIndex(0);
    }

    private void showLanguageToast() {
        toastLabel.setText(LanguageManager.getDisplayName(GameController.getInstance().getCurrentLanguage()));
        Dimension size = toastLabel.getPreferredSize();
        int w = size.width + 30;
        int h = 30;
        Point btnLoc = SwingUtilities.convertPoint(btnLanguage.getParent(), btnLanguage.getLocation(), getLayeredPane());
        toastLabel.setBounds(btnLoc.x + (btnLanguage.getWidth() - w) / 2, btnLoc.y - h - 10, w, h);
        toastLabel.setVisible(true);
        toastTimer.restart();
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(TABLE_ROW_BG);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(TEXT_COLOR);
        table.setRowHeight(25);
        table.setFont(new Font("Dialog", Font.PLAIN, 14));
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, v, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(CENTER);
                l.setFont(new Font("Dialog", Font.PLAIN, 13));
                if (isSel) {
                    l.setBackground(TABLE_SELECTION_BG);
                    l.setForeground(TEXT_COLOR);
                } else {
                    l.setBackground(TABLE_ROW_BG);
                    l.setForeground(TEXT_COLOR);
                }
                return l;
            }
        });

        JTableHeader h = table.getTableHeader();
        h.setForeground(ACCENT_COLOR);
        h.setBackground(TABLE_HEADER_BG);
        h.setFont(new Font("Dialog", Font.BOLD, 14));

        return table;
    }

    private JScrollPane createStyledScrollPane(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(ACCENT_COLOR);
        btn.setFont(new Font("Dialog", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT_COLOR), new EmptyBorder(5, 15, 5, 15)));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 60, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(40, 40, 40));
            }
        });
        return btn;
    }

    private Border neonBorder(Color c) {
        return new CompoundBorder(new LineBorder(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200), 2, true),
                new EmptyBorder(10, 12, 10, 12));
    }

    private void styleNeonLabel(JLabel l) {
        l.setForeground(TEXT_COLOR);
        l.setFont(new Font("Dialog", Font.BOLD, 14));
    }

    private void styleNeonCombo(JComboBox<?> c) {
        c.setBackground(DIALOG_PANEL);
        c.setForeground(TEXT_COLOR);
        c.setFont(new Font("Dialog", Font.PLAIN, 14));
        c.setBorder(neonBorder(ACCENT_COLOR));
    }

    private void loadTable() {
        model.setRowCount(0);
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        for (Question q : manager.getAllQuestions()) {
            List<String> o = new ArrayList<>(q.getOptions());
            while (o.size() < 4) o.add("");
            char correctLetter = Character.toUpperCase(q.getCorrectOption());
            model.addRow(new Object[]{
                    Integer.valueOf(q.getId()),
                    q.getText(),
                    o.get(0), o.get(1), o.get(2), o.get(3),
                    String.valueOf(correctLetter),
                    translateDifficulty(q.getDifficultyLevel(), lang)
            });
        }
        applyFilters();
    }

    private String translateDifficulty(String diff, LanguageManager.Language lang) {
        if (diff == null) return "";
        if (lang == LanguageManager.Language.EN) return diff;

        return switch (lang) {
            case HE -> switch (diff.toUpperCase()) {
                case "EASY" -> "קל";
                case "MEDIUM" -> "בינוני";
                case "HARD" -> "קשה";
                case "EXPERT" -> "מומחה";
                default -> diff;
            };
            case AR -> switch (diff.toUpperCase()) {
                case "EASY" -> "سهل";
                case "MEDIUM" -> "متوسط";
                case "HARD" -> "صعب";
                case "EXPERT" -> "خبير";
                default -> diff;
            };
            case RU -> switch (diff.toUpperCase()) {
                case "EASY" -> "Легкий";
                case "MEDIUM" -> "Средний";
                case "HARD" -> "Сложный";
                case "EXPERT" -> "Эксперт";
                default -> diff;
            };
            case ES -> switch (diff.toUpperCase()) {
                case "EASY" -> "Fácil";
                case "MEDIUM" -> "Medio";
                case "HARD" -> "Difícil";
                case "EXPERT" -> "Experto";
                default -> diff;
            };
            default -> diff;
        };
    }

    private Question buildQuestionFromRow(int row) {
        try {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            String text = model.getValueAt(row, 1).toString();
            List<String> opts = new ArrayList<>();
            for (int i = 2; i <= 5; i++) {
                Object val = model.getValueAt(row, i);
                opts.add(val == null ? "" : val.toString());
            }
            char correct = model.getValueAt(row, 6).toString().charAt(0);
            String diffDisplay = model.getValueAt(row, 7).toString();
            String diff = mapDifficultyToEnglish(diffDisplay);
            return new Question(id, text, opts, correct, diff);
        } catch (Exception e) {
            return null;
        }
    }

    private String mapDifficultyToEnglish(String diff) {
        if (diff == null) return "EASY";
        return switch (diff) {
            case "קל", "سهل", "Легкий", "Fácil" -> "EASY";
            case "בינוני", "متوسط", "Средний", "Medio" -> "MEDIUM";
            case "קשה", "صعب", "Сложный", "Difícil" -> "HARD";
            case "מומחה", "خبير", "Эксперт", "Experto" -> "EXPERT";
            default -> diff.toUpperCase();
        };
    }

    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setOpaque(false);

        diffLabel = new JLabel("Difficulty:");
        diffLabel.setForeground(TEXT_COLOR);
        diffLabel.setFont(new Font("Dialog", Font.BOLD, 14));

        difficultyFilter = new JComboBox<>(new String[]{"All", "EASY", "MEDIUM", "HARD", "EXPERT"});
        styleCombo(difficultyFilter);
        attachComboClickSound(difficultyFilter);

        correctAnswerFilter = new JComboBox<>(new String[]{"All", "A", "B", "C", "D"});
        styleCombo(correctAnswerFilter);
        attachComboClickSound(correctAnswerFilter);

        clearBtn = createStyledButton("Clear");
        attachClickSound(clearBtn);

        burgerBtn = new JButton("☰");
        burgerBtn.setPreferredSize(new Dimension(46, 36));
        burgerBtn.setBackground(new Color(40, 40, 40));
        burgerBtn.setForeground(ACCENT_COLOR);
        burgerBtn.setFont(new Font("Dialog", Font.BOLD, 18));
        burgerBtn.setFocusPainted(false);
        burgerBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_COLOR),
                new EmptyBorder(2, 10, 2, 10)
        ));
        burgerBtn.addActionListener(e -> toggleDrawer());
        attachClickSound(burgerBtn);

        clearBtn.addActionListener(e -> {
            difficultyFilter.setSelectedIndex(0);
            correctAnswerFilter.setSelectedIndex(0);
            applyFilters();
            closeDrawer();
        });

        difficultyFilter.addActionListener(e -> applyFilters());
        correctAnswerFilter.addActionListener(e -> applyFilters());
        correctAnswerFilter.setLightWeightPopupEnabled(false);
        correctAnswerFilter.setPreferredSize(new Dimension(240, 34));
        correctAnswerFilter.setMaximumSize(new Dimension(240, 34));


        p.add(diffLabel);
        p.add(difficultyFilter);
        p.add(burgerBtn);
        p.add(clearBtn);

        return p;
    }

    private void styleCombo(JComboBox<String> box) {
        box.setBackground(Color.WHITE);
        box.setForeground(Color.BLACK);
        box.setFont(new Font("Dialog", Font.PLAIN, 14));
        box.setFocusable(false);
    }

    private void applyFilters() {
        if (sorter == null) return;

        String diff = (String) difficultyFilter.getSelectedItem();
        String corr = (String) correctAnswerFilter.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        String diffFilter = mapDifficultyToFilter(diff);
        if (diffFilter != null && !"All".equalsIgnoreCase(diffFilter) && !isAllFilter(diffFilter)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(diffFilter) + "$", 7));
        }

        if (corr != null && !"All".equalsIgnoreCase(corr) && !isAllFilter(corr)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(corr) + "$", 6));
        }

        if (filters.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private boolean isAllFilter(String value) {
        return value.equals("הכל") || value.equals("الكل") || value.equals("Все") || value.equals("Todos");
    }

    private String mapDifficultyToFilter(String value) {
        if (value == null) return "All";
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        if (isAllFilter(value) || "All".equalsIgnoreCase(value)) return "All";

        return translateDifficulty(mapDifficultyToEnglish(value), lang);
    }

    private static int parseIntSafe(Object o) {
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }

    private void attachClickSound(AbstractButton btn) {
        btn.addActionListener(e -> SoundManager.click());
    }

    private void attachComboClickSound(JComboBox<?> combo) {
        final boolean[] initialized = {false};
        combo.addActionListener(e -> {
            if (!initialized[0]) { initialized[0] = true; return; }
            SoundManager.click();
        });
    }

    private void attachTypingSound(JTextField field) {
        final int cooldownMs = 35;
        final long[] last = {0L};

        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void ping() {
                long now = System.currentTimeMillis();
                if (now - last[0] >= cooldownMs) {
                    SoundManager.typeKey();
                    last[0] = now;
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { ping(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { ping(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { ping(); }
        });
    }

    private void attachHeaderClickSound(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                SoundManager.click();
            }
        });
    }

    private void attachComboOpenClickSound(JComboBox<?> combo) {
        combo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                SoundManager.click();
            }
        });

        java.awt.Component[] comps = combo.getComponents();
        for (java.awt.Component c : comps) {
            if (c instanceof AbstractButton b) {
                b.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        SoundManager.click();
                    }
                });
            }
        }
    }

    private class NeonQuestionDialog extends JDialog {
        private Question result = null;
        private final JTextField idField = new JTextField();
        private final JTextField textField = new JTextField();
        private final JTextField aField = new JTextField();
        private final JTextField bField = new JTextField();
        private final JTextField cField = new JTextField();
        private final JTextField dField = new JTextField();
        private final JComboBox<String> correctCombo = new JComboBox<>();
        private final JComboBox<String> diffCombo = new JComboBox<>();
        private final LanguageManager.Language lang;

        NeonQuestionDialog(JFrame owner, Question existing, int autoId) {
            super(owner, existing == null ? "Add Question" : "Edit Question", true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            if (owner != null && owner.getIconImage() != null) {
                setIconImage(owner.getIconImage());
            } else {
                setIconImage(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB));
            }

            lang = GameController.getInstance().getCurrentLanguage();
            boolean isRTL = LanguageManager.isRTL(lang);

            setTitle(existing == null ? getDialogAddTitle() : getDialogEditTitle());

            setupCorrectCombo();
            setupDiffCombo();

            int fieldW = 360;
            Dimension fieldMax = new Dimension(fieldW, 36);
            for (JTextField f : new JTextField[]{idField, textField, aField, bField, cField, dField}) {
                f.setPreferredSize(new Dimension(fieldW, 36));
                f.setMaximumSize(fieldMax);
            }

            JPanel root = new JPanel(new BorderLayout(14, 14));
            root.setBackground(DIALOG_BG);
            root.setBorder(new EmptyBorder(16, 16, 16, 16));

            if (isRTL) root.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            JLabel header = new JLabel(existing == null ? getDialogAddTitle() : getDialogEditTitle());
            header.setForeground(ACCENT_COLOR);
            header.setFont(new Font("Dialog", Font.BOLD, 22));
            if (isRTL) {
                header.setHorizontalAlignment(SwingConstants.RIGHT);
                header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            root.add(header, BorderLayout.NORTH);

            JPanel card = new JPanel(new GridBagLayout());
            card.setBackground(DIALOG_PANEL);
            card.setBorder(neonBorder(ACCENT_COLOR));
            if (isRTL) card.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(8, 8, 8, 8);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1;

            if (existing != null) {
                idField.setText(String.valueOf(existing.getId()));
                idField.setEditable(false);
                textField.setText(existing.getText());

                List<String> exOpts = new ArrayList<>(existing.getOptions());
                while (exOpts.size() < 4) exOpts.add("");

                aField.setText(exOpts.get(0));
                bField.setText(exOpts.get(1));
                cField.setText(exOpts.get(2));
                dField.setText(exOpts.get(3));

                char correctChar = Character.toUpperCase(existing.getCorrectOption());
                int correctIndex = correctChar - 'A';
                if (correctIndex >= 0 && correctIndex < 4) {
                    correctCombo.setSelectedIndex(correctIndex);
                }

                String diff = existing.getDifficultyLevel().toUpperCase();
                int diffIndex = switch (diff) {
                    case "EASY" -> 0;
                    case "MEDIUM" -> 1;
                    case "HARD" -> 2;
                    case "EXPERT" -> 3;
                    default -> 0;
                };
                diffCombo.setSelectedIndex(diffIndex);
            } else {
                idField.setText(String.valueOf(autoId));
                idField.setEditable(false);
                correctCombo.setSelectedIndex(0);
                diffCombo.setSelectedIndex(0);
            }

            styleNeonField(idField);
            styleNeonField(textField);
            styleNeonField(aField);
            styleNeonField(bField);
            styleNeonField(cField);
            styleNeonField(dField);
            styleNeonCombo(correctCombo);
            styleNeonCombo(diffCombo);

            attachComboOpenClickSound(correctCombo);
            attachComboOpenClickSound(diffCombo);
            attachTypingSound(textField);
            attachTypingSound(aField);
            attachTypingSound(bField);
            attachTypingSound(cField);
            attachTypingSound(dField);

            addRow(card, gc, 0, getLabelId(), idField, isRTL);
            addRow(card, gc, 1, getLabelText(), textField, isRTL);
            addRow(card, gc, 2, getLabelOptA(), aField, isRTL);
            addRow(card, gc, 3, getLabelOptB(), bField, isRTL);
            addRow(card, gc, 4, getLabelOptC(), cField, isRTL);
            addRow(card, gc, 5, getLabelOptD(), dField, isRTL);
            addRow(card, gc, 6, getLabelCorrect(), correctCombo, isRTL);
            addRow(card, gc, 7, getLabelDiff(), diffCombo, isRTL);

            root.add(card, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(isRTL ? FlowLayout.LEFT : FlowLayout.RIGHT, 10, 0));
            buttons.setBackground(DIALOG_BG);

            JButton btnCancel = createStyledButton(getBtnCancel());
            JButton btnSaveDialog = createStyledButton(getBtnSave());
            attachClickSound(btnCancel);
            attachClickSound(btnSaveDialog);

            btnCancel.addActionListener(e -> dispose());
            btnSaveDialog.addActionListener(e -> onSave());

            if (isRTL) {
                buttons.add(btnSaveDialog);
                buttons.add(btnCancel);
            } else {
                buttons.add(btnCancel);
                buttons.add(btnSaveDialog);
            }
            root.add(buttons, BorderLayout.SOUTH);

            setContentPane(root);
            pack();
            setSize(750, 720);
            setResizable(true);
            setMinimumSize(new Dimension(650, 650));
            setLocationRelativeTo(owner);
        }

        private void setupCorrectCombo() {
            switch (lang) {
                case HE -> { correctCombo.addItem("א"); correctCombo.addItem("ב"); correctCombo.addItem("ג"); correctCombo.addItem("ד"); }
                case AR -> { correctCombo.addItem("أ"); correctCombo.addItem("ب"); correctCombo.addItem("ج"); correctCombo.addItem("د"); }
                case RU -> { correctCombo.addItem("А"); correctCombo.addItem("Б"); correctCombo.addItem("В"); correctCombo.addItem("Г"); }
                default -> { correctCombo.addItem("A"); correctCombo.addItem("B"); correctCombo.addItem("C"); correctCombo.addItem("D"); }
            }
        }

        private void setupDiffCombo() {
            switch (lang) {
                case HE -> { diffCombo.addItem("קל"); diffCombo.addItem("בינוני"); diffCombo.addItem("קשה"); diffCombo.addItem("מומחה"); }
                case AR -> { diffCombo.addItem("سهل"); diffCombo.addItem("متوسط"); diffCombo.addItem("صعب"); diffCombo.addItem("خبير"); }
                case RU -> { diffCombo.addItem("Легкий"); diffCombo.addItem("Средний"); diffCombo.addItem("Сложный"); diffCombo.addItem("Эксперт"); }
                case ES -> { diffCombo.addItem("Fácil"); diffCombo.addItem("Medio"); diffCombo.addItem("Difícil"); diffCombo.addItem("Experto"); }
                default -> { diffCombo.addItem("EASY"); diffCombo.addItem("MEDIUM"); diffCombo.addItem("HARD"); diffCombo.addItem("EXPERT"); }
            }
        }

        private String getDialogAddTitle() {
            return switch (lang) {
                case HE -> "הוסף שאלה חדשה";
                case AR -> "أضف سؤالاً جديداً";
                case RU -> "Добавить новый вопрос";
                case ES -> "Añadir nueva pregunta";
                default -> "Add New Question";
            };
        }

        private String getDialogEditTitle() {
            return switch (lang) {
                case HE -> "ערוך שאלה";
                case AR -> "تعديل سؤال";
                case RU -> "Редактировать вопрос";
                case ES -> "Editar pregunta";
                default -> "Edit Question";
            };
        }

        private String getLabelId() { return switch (lang) { case HE -> "מזהה"; case AR -> "الرقم التسلسلي"; case RU -> "ID"; case ES -> "ID"; default -> "ID"; }; }
        private String getLabelText() { return switch (lang) { case HE -> "טקסט השאלה"; case AR -> "نص السؤال"; case RU -> "Текст вопроса"; case ES -> "Texto de la pregunta"; default -> "Question Text"; }; }
        private String getLabelOptA() { return switch (lang) { case HE -> "תשובה א"; case AR -> "الخيار أ"; case RU -> "Вариант А"; case ES -> "Opción A"; default -> "Option A"; }; }
        private String getLabelOptB() { return switch (lang) { case HE -> "תשובה ב"; case AR -> "الخيار ب"; case RU -> "Вариант Б"; case ES -> "Opción B"; default -> "Option B"; }; }
        private String getLabelOptC() { return switch (lang) { case HE -> "תשובה ג"; case AR -> "الخيار ج"; case RU -> "Вариант В"; case ES -> "Opción C"; default -> "Option C"; }; }
        private String getLabelOptD() { return switch (lang) { case HE -> "תשובה ד"; case AR -> "الخيار د"; case RU -> "Вариант Г"; case ES -> "Opción D"; default -> "Option D"; }; }
        private String getLabelCorrect() { return switch (lang) { case HE -> "תשובה נכונה"; case AR -> "الإجابة الصحيحة"; case RU -> "Правильный ответ"; case ES -> "Respuesta correcta"; default -> "Correct Answer"; }; }
        private String getLabelDiff() { return switch (lang) { case HE -> "רמת קושי"; case AR -> "الصعوبة"; case RU -> "Сложность"; case ES -> "Dificultad"; default -> "Difficulty"; }; }
        private String getBtnCancel() { return switch (lang) { case HE -> "ביטול"; case AR -> "إلغاء"; case RU -> "Отмена"; case ES -> "Cancelar"; default -> "Cancel"; }; }
        private String getBtnSave() { return switch (lang) { case HE -> "שמור"; case AR -> "حفظ"; case RU -> "Сохранить"; case ES -> "Guardar"; default -> "Save"; }; }

        private void styleNeonField(JTextField f) {
            f.setBackground(DIALOG_PANEL);
            f.setForeground(TEXT_COLOR);
            f.setCaretColor(ACCENT_COLOR);
            f.setFont(new Font("Dialog", Font.PLAIN, 14));
            f.setBorder(neonBorder(ACCENT_COLOR));
            if (LanguageManager.isRTL(lang)) f.setHorizontalAlignment(JTextField.RIGHT);
            else f.setHorizontalAlignment(JTextField.LEFT);
            if (!f.isEditable()) f.setForeground(Color.GRAY);
        }

        private void addRow(JPanel card, GridBagConstraints gc, int row, String label, JComponent input, boolean isRTL) {
            JLabel l = new JLabel(label + ":");
            styleNeonLabel(l);

            if (isRTL) {
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                gc.gridx = 1; gc.gridy = row; gc.weightx = 1; gc.anchor = GridBagConstraints.EAST;
                card.add(input, gc);
                gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.anchor = GridBagConstraints.WEST;
                card.add(l, gc);
            } else {
                gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.anchor = GridBagConstraints.WEST;
                card.add(l, gc);
                gc.gridx = 1; gc.gridy = row; gc.weightx = 1; gc.anchor = GridBagConstraints.EAST;
                card.add(input, gc);
            }
        }

        private void onSave() {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String text = textField.getText().trim();

                if (text.isEmpty()) {
                    throw new IllegalArgumentException(getEmptyTextError());
                }

                List<String> opts = new ArrayList<>();
                opts.add(aField.getText().trim());
                opts.add(bField.getText().trim());
                opts.add(cField.getText().trim());
                opts.add(dField.getText().trim());

                for (int i = 0; i < opts.size(); i++) {
                    if (opts.get(i).isEmpty()) {
                        char optLetter = (char) ('A' + i);
                        throw new IllegalArgumentException(getEmptyOptionError(optLetter));
                    }
                }

                int correctIndex = correctCombo.getSelectedIndex();
                char correct = (char) ('A' + correctIndex);

                int diffIndex = diffCombo.getSelectedIndex();
                String diff = switch (diffIndex) {
                    case 0 -> "EASY";
                    case 1 -> "MEDIUM";
                    case 2 -> "HARD";
                    case 3 -> "EXPERT";
                    default -> "EASY";
                };

                result = new Question(id, text, opts, correct, diff);
                dispose();
            } catch (Exception ex) {
                String errTitle = getErrorTitle();
                String errMsg = getInvalidInputPrefix() + ex.getMessage();
                JOptionPane.showMessageDialog(this, errMsg, errTitle, JOptionPane.ERROR_MESSAGE);
            }
        }

        private String getEmptyTextError() { return switch (lang) { case HE -> "טקסט השאלה ריק."; case AR -> "نص السؤال فارغ."; case RU -> "Текст вопроса пуст."; case ES -> "El texto de la pregunta está vacío."; default -> "Question text is empty."; }; }
        private String getEmptyOptionError(char opt) { return switch (lang) { case HE -> "תשובה " + opt + " ריקה."; case AR -> "الخيار " + opt + " فارغ."; case RU -> "Вариант " + opt + " пуст."; case ES -> "La opción " + opt + " está vacía."; default -> "Option " + opt + " is empty."; }; }
        private String getErrorTitle() { return switch (lang) { case HE -> "שגיאה"; case AR -> "خطأ"; case RU -> "Ошибка"; case ES -> "Error"; default -> "Error"; }; }
        private String getInvalidInputPrefix() { return switch (lang) { case HE -> "קלט לא תקין: "; case AR -> "تفاصيل غير صحيحة: "; case RU -> "Неверный ввод: "; case ES -> "Entrada no válida: "; default -> "Invalid input: "; }; }

        public Question getResult() {
            return result;
        }
    }

    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String resourcePath) {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}
