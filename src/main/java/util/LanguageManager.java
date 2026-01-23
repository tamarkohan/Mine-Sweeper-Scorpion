package util;

import java.util.HashMap;
import java.util.Map;

/**
 * LanguageManager - Supports EN, HE, AR, RU, ES
 *
 * RTL Languages: HE (Hebrew), AR (Arabic)
 * LTR Languages: EN (English), RU (Russian), ES (Spanish)
 */
public class LanguageManager {

    public enum Language { EN, HE, AR, RU, ES }

    private static final Map<String, Map<Language, String>> translations = new HashMap<>();

    static {
        // Game Result Dialog
        add("you_won", "🎊 YOU WON! 🎊", "🎊 ניצחתם! 🎊", "🎊 فزتم! 🎊", "🎊 ВЫ ПОБЕДИЛИ! 🎊", "🎊 ¡GANASTE! 🎊");
        add("game_over", "💀 GAME OVER 💀", "💀 המשחק נגמר 💀", "💀 انتهت اللعبة 💀", "💀 ИГРА ОКОНЧЕНА 💀", "💀 FIN DEL JUEGO 💀");
        add("stat_score", "Score:", "ניקוד:", "النقاط:", "Счёт:", "Puntuación:");
        add("stat_lives", "Lives Remaining:", "חיים שנותרו:", "الأرواح المتبقية:", "Осталось жизней:", "Vidas restantes:");
        add("stat_questions", "Questions Answered:", "שאלות שנענו:", "الأسئلة المُجابة:", "Отвечено вопросов:", "Preguntas respondidas:");
        add("stat_correct", "Correct Answers:", "תשובות נכונות:", "الإجابات الصحيحة:", "Правильных ответов:", "Respuestas correctas:");
        add("stat_surprises", "Surprises Opened:", "הפתעות שנפתחו:", "المفاجآت المفتوحة:", "Открыто сюрпризов:", "Sorpresas abiertas:");
        add("time", "Time", "זמן", "الوقت", "Время", "Tiempo");
        add("stat_accuracy", "Accuracy:", "דיוק:", "الدقة:", "Точность:", "Precisión:");
        add("restart", "Restart", "התחל מחדש", "إعادة", "Заново", "Reiniciar");
        add("exit", "Exit", "יציאה", "خروج", "Выход", "Salir");

        // GamePanel
        add("score", "SCORE", "ניקוד", "النقاط", "СЧЁТ", "PUNTOS");
        add("lives", "LIVES", "חיים", "الأرواح", "ЖИЗНИ", "VIDAS");
        add("mines_left", "MINES LEFT", "מוקשים נותרו", "الألغام المتبقية", "ОСТАЛОСЬ МИН", "MINAS RESTANTES");
        add("wait_turn", "WAIT FOR YOUR TURN", "המתן לתורך", "انتظر دورك", "ЖДИТЕ СВОЕЙ ОЧЕРЕДИ", "ESPERA TU TURNO");

        // QuestionDialog
        add("question", "Question", "שאלה", "سؤال", "Вопрос", "Pregunta");
        add("submit", "Submit", "שלח", "إرسال", "Ответить", "Enviar");
        add("cancel", "Cancel", "ביטול", "إلغاء", "Отмена", "Cancelar");
        add("no_answer_selected", "Please choose an answer first.", "אנא בחר תשובה תחילה.", "يرجى اختيار إجابة أولاً.", "Сначала выберите ответ.", "Por favor, elige una respuesta primero.");
        add("no_answer_title", "No answer selected", "לא נבחרה תשובה", "لم يتم اختيار إجابة", "Ответ не выбран", "No se seleccionó respuesta");
        add("correct", "CORRECT ✓", "נכון ✓", "صحيح ✓", "ПРАВИЛЬНО ✓", "CORRECTO ✓");
        add("wrong", "WRONG ✗", "שגוי ✗", "خطأ ✗", "НЕПРАВИЛЬНО ✗", "INCORRECTO ✗");
        add("your_answer", "Your answer:", "התשובה שלך:", "إجابتك:", "Ваш ответ:", "Tu respuesta:");
        add("correct_answer", "Correct answer:", "התשובה הנכונה:", "الإجابة الصحيحة:", "Правильный ответ:", "Respuesta correcta:");
        add("ok", "OK", "אישור", "موافق", "ОК", "OK");

        // Language Toast
        add("lang_english", "English", "English", "English", "English", "English");
        add("lang_hebrew", "עברית", "עברית", "עברית", "עברית", "עברית");
        add("lang_arabic", "العربية", "العربية", "العربية", "العربية", "العربية");
        add("lang_russian", "Русский", "Русский", "Русский", "Русский", "Русский");
        add("lang_spanish", "Español", "Español", "Español", "Español", "Español");

        // ActivationConfirmDialog - Question Cell
        add("question_cell", "Question Cell", "תא שאלה", "خلية سؤال", "Ячейка вопроса", "Celda de pregunta");
        add("this_is_question_cell", "This is a Question cell", "זהו תא שאלה", "هذه خلية سؤال", "Это ячейка вопроса", "Esta es una celda de pregunta");
        add("do_you_want_to_activate", "Do you want to activate it?", "האם ברצונך להפעיל אותו?", "هل تريد تفعيلها؟", "Хотите активировать?", "¿Quieres activarla?");
        add("activate", "Activate", "הפעל", "تفعيل", "Активировать", "Activar");

        // ActivationConfirmDialog - Surprise Cell
        add("surprise_cell", "Surprise Cell", "תא הפתעה", "خلية مفاجأة", "Ячейка сюрприза", "Celda de sorpresa");
        add("this_is_surprise_cell", "This is a Surprise cell", "זהו תא הפתעה", "هذه خلية مفاجأة", "Это ячейка сюрприза", "Esta es una celda de sorpresa");

        // Outcome Dialog - Question Results
        add("outcome_correct", "CORRECT!", "נכון!", "صحيح!", "ПРАВИЛЬНО!", "¡CORRECTO!");
        add("outcome_wrong", "WRONG!", "שגוי!", "خطأ!", "НЕПРАВИЛЬНО!", "¡INCORRECTO!");
        add("outcome_skipped", "SKIPPED", "דילגת", "تم التخطي", "ПРОПУЩЕНО", "OMITIDO");
        add("outcome", "OUTCOME", "תוצאה", "النتيجة", "РЕЗУЛЬТАТ", "RESULTADO");

        // Outcome Dialog - Surprise Results
        add("surprise_good", "SURPRISE: GOOD!", "הפתעה: טוב!", "مفاجأة: جيد!", "СЮРПРИЗ: ХОРОШО!", "¡SORPRESA: BUENA!");
        add("surprise_bad", "SURPRISE: BAD!", "הפתעה: רע!", "مفاجأة: سيء!", "СЮРПРИЗ: ПЛОХО!", "¡SORPRESA: MALA!");
        add("surprise", "SURPRISE!", "הפתעה!", "مفاجأة!", "СЮРПРИЗ!", "¡SORPRESA!");

        // Outcome message parts
        add("wrong_prefix", "Wrong", "שגוי", "خطأ", "Неправильно", "Incorrecto");
        add("correct_prefix", "Correct", "נכון", "صحيح", "Правильно", "Correcto");
        add("activation_cost", "Activation cost", "עלות הפעלה", "تكلفة التفعيل", "Стоимость активации", "Costo de activación");
        add("score_label", "Score", "ניקוד", "النقاط", "Счёт", "Puntos");
        add("lives_label", "Lives", "חיים", "الأرواح", "Жизни", "Vidas");
        add("pts", "pts", "נק'", "نقاط", "очк.", "pts");
        add("life", "life", "חיים", "حياة", "жизнь", "vida");

        // Difficulty levels
        add("difficulty_easy", "EASY", "קל", "سهل", "ЛЕГКО", "FÁCIL");
        add("difficulty_medium", "MEDIUM", "בינוני", "متوسط", "СРЕДНЕ", "MEDIO");
        add("difficulty_hard", "HARD", "קשה", "صعب", "СЛОЖНО", "DIFÍCIL");
        add("difficulty_expert", "EXPERT", "מומחה", "خبير", "ЭКСПЕРТ", "EXPERTO");

        // Question Management Frame
        add("question_management", "Question Management", "ניהול שאלות", "إدارة الأسئلة", "Управление вопросами", "Gestión de preguntas");
        add("add", "Add", "הוסף", "إضافة", "Добавить", "Añadir");
        add("edit", "Edit", "ערוך", "تعديل", "Редактировать", "Editar");
        add("delete", "Delete", "מחק", "حذف", "Удалить", "Eliminar");
        add("save", "Save", "שמור", "حفظ", "Сохранить", "Guardar");
        add("apply", "Apply", "החל", "تطبيق", "Применить", "Aplicar");
        add("clear", "Clear", "נקה", "مسح", "Очистить", "Limpiar");
        add("difficulty", "Difficulty:", "רמת קושי:", "الصعوبة:", "Сложность:", "Dificultad:");
        add("correct_label", "Correct:", "תשובה נכונה:", "الإجابة الصحيحة:", "Правильный:", "Correcta:");
        add("id", "ID:", "מזהה:", "المعرف:", "ID:", "ID:");
        add("all", "All", "הכל", "الكل", "Все", "Todos");
        add("saved_msg", "Questions saved to CSV.", "השאלות נשמרו לקובץ CSV.", "تم حفظ الأسئلة في ملف CSV.", "Вопросы сохранены в CSV.", "Preguntas guardadas en CSV.");
        add("saved_title", "Saved", "נשמר", "تم الحفظ", "Сохранено", "Guardado");

        // Add/Edit Question Dialog
        add("add_question", "Add Question", "הוסף שאלה", "إضافة سؤال", "Добавить вопрос", "Añadir pregunta");
        add("edit_question", "Edit Question", "ערוך שאלה", "تعديل سؤال", "Редактировать вопрос", "Editar pregunta");
        add("add_new_question", "Add New Question", "הוסף שאלה חדשה", "إضافة سؤال جديد", "Добавить новый вопрос", "Añadir nueva pregunta");
        add("text", "Text", "טקסט", "النص", "Текст", "Texto");
        add("option_a", "Option A", "אפשרות א", "الخيار أ", "Вариант A", "Opción A");
        add("option_b", "Option B", "אפשרות ב", "الخيار ب", "Вариант B", "Opción B");
        add("option_c", "Option C", "אפשרות ג", "الخيار ج", "Вариант C", "Opción C");
        add("option_d", "Option D", "אפשרות ד", "الخيار د", "Вариант D", "Opción D");
        add("correct_option", "Correct", "נכונה", "الصحيحة", "Правильный", "Correcta");
        add("difficulty_level", "Difficulty", "רמה", "المستوى", "Уровень", "Nivel");
        add("text_empty", "Text is empty.", "הטקסט ריק.", "النص فارغ.", "Текст пустой.", "El texto está vacío.");
        add("invalid_input", "Invalid input: ", "קלט לא תקין: ", "إدخال غير صالح: ", "Неверный ввод: ", "Entrada inválida: ");
        add("error", "Error", "שגיאה", "خطأ", "Ошибка", "Error");

        // Admin Access Dialog
        add("admin_access", "Admin Access", "גישת מנהל", "وصول المسؤول", "Доступ администратора", "Acceso de administrador");
        add("enter_admin_password", "Enter Admin Password:", "הזן סיסמת מנהל:", "أدخل كلمة مرور المسؤول:", "Введите пароль администратора:", "Ingresa la contraseña de admin:");
        add("access_denied", "Access denied.", "הגישה נדחתה.", "تم رفض الوصول.", "Доступ запрещён.", "Acceso denegado.");
        add("wrong_password", "Wrong password", "סיסמה שגויה", "كلمة مرور خاطئة", "Неверный пароль", "Contraseña incorrecta");

        // Table headers for Question Management
        add("header_id", "ID", "מזהה", "المعرف", "ID", "ID");
        add("header_text", "Text", "טקסט", "النص", "Текст", "Texto");
        add("header_a", "A", "א", "أ", "A", "A");
        add("header_b", "B", "ב", "ب", "B", "B");
        add("header_c", "C", "ג", "ج", "C", "C");
        add("header_d", "D", "ד", "د", "D", "D");
        add("header_correct", "Correct", "נכונה", "الصحيحة", "Правильный", "Correcta");
        add("header_difficulty", "Difficulty", "רמה", "المستوى", "Уровень", "Nivel");

        // Exit Confirmation
        add("exit_title", "Exit", "יציאה", "خروج", "Выход", "Salir");
        add("exit_confirm_msg", "Are you sure you want to exit?\nProgress will be lost.",
                "האם אתה בטוח שברצונך לצאת?\nההתקדמות תאבד.",
                "هل أنت متأكد أنك تريد الخروج؟\nسيتم فقدان التقدم.",
                "Вы уверены, что хотите выйти?\nПрогресс будет потерян.",
                "¿Estás seguro de que quieres salir?\nSe perderá el progreso.");

        // StartPanel specific
        add("player1", "PLAYER 1", "שחקן 1", "اللاعب 1", "ИГРОК 1", "JUGADOR 1");
        add("player2", "PLAYER 2", "שחקן 2", "اللاعب 2", "ИГРОК 2", "JUGADOR 2");
        add("level", "LEVEL:", "רמת קושי:", "المستوى:", "УРОВЕНЬ:", "NIVEL:");
        add("shared_lives", "Shared Lives", "חיים משותפים", "أرواح مشتركة", "Общие жизни", "Vidas compartidas");
        add("board", "Board", "לוח", "اللوحة", "Поле", "Tablero");
        add("mines_per_player", "Mines to play", "מוקשים לשחקן", "ألغام للعب", "Мин для игры", "Minas para jugar");
        add("questions_count", "Questions", "שאלות", "أسئلة", "Вопросы", "Preguntas");
        add("surprises_count", "Surprises", "הפתעות", "مفاجآت", "Сюрпризы", "Sorpresas");
        add("missing_names", "Please enter names for both players.", "אנא הזן שמות לשני השחקנים.", "يرجى إدخال أسماء لكلا اللاعبين.", "Введите имена обоих игроков.", "Por favor, ingresa los nombres de ambos jugadores.");
        add("missing_names_title", "Missing Names", "חסרים שמות", "أسماء مفقودة", "Отсутствуют имена", "Faltan nombres");

        // Yes/No buttons
        add("yes", "Yes", "כן", "نعم", "Да", "Sí");
        add("no", "No", "לא", "لا", "Нет", "No");

        // Game History
        add("game_history", "Game History", "היסטוריית משחקים", "سجل الألعاب", "История игр", "Historial de juegos");
        add("no_history", "No game history yet.", "אין היסטוריית משחקים עדיין.", "لا يوجد سجل ألعاب بعد.", "История игр пока пуста.", "Aún no hay historial de juegos.");
        add("back", "Back", "חזור", "رجوع", "Назад", "Volver");

        // How to Play
        add("how_to_play", "How to Play", "איך לשחק", "كيفية اللعب", "Как играть", "Cómo jugar");
    }

    private static void add(String key, String en, String he, String ar, String ru, String es) {
        Map<Language, String> map = new HashMap<>();
        map.put(Language.EN, en);
        map.put(Language.HE, he);
        map.put(Language.AR, ar);
        map.put(Language.RU, ru);
        map.put(Language.ES, es);
        translations.put(key, map);
    }

    public static String get(String key, Language lang) {
        Map<Language, String> map = translations.get(key);
        if (map == null) return key;
        String result = map.get(lang);
        return result == null ? key : result;
    }

    /**
     * Check if a language is RTL (Right-to-Left)
     */
    public static boolean isRTL(Language lang) {
        return lang == Language.HE || lang == Language.AR;
    }

    /**
     * Get the language display name in its native form
     */
    public static String getDisplayName(Language lang) {
        return switch (lang) {
            case EN -> "English";
            case HE -> "עברית";
            case AR -> "العربية";
            case RU -> "Русский";
            case ES -> "Español";
        };
    }

    /**
     * Get all available languages
     */
    public static Language[] getAllLanguages() {
        return Language.values();
    }

    /**
     * Get the next language in rotation (for cycling through languages)
     */
    public static Language getNextLanguage(Language current) {
        Language[] all = Language.values();
        int idx = current.ordinal();
        return all[(idx + 1) % all.length];
    }
}