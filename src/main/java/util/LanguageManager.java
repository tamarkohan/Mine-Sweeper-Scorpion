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
        add("result", "Result", "תוצאה", "النتيجة", "Результат", "Resultado");

        // Outcome Dialog - Surprise Results
        add("surprise_good", "Good Surprise!", "הפתעה טובה!", "مفاجأة جيدة!", "Хороший сюрприз!", "¡Buena sorpresa!");
        add("surprise_bad", "Bad Surprise!", "הפתעה רעה!", "مفاجأة سيئة!", "Плохой сюрприз!", "¡Mala sorpresa!");
        add("surprise", "SURPRISE!", "הפתעה!", "مفاجأة!", "СЮРПРИЗ!", "¡SORPRESA!");

        // Outcome message parts (for translation in dialogs)
        add("wrong_prefix", "Wrong", "שגוי", "خطأ", "Неправильно", "Incorrecto");
        add("correct_prefix", "Correct", "נכון", "صحيح", "Правильно", "Correcto");
        add("activation_cost", "Activation cost:", "עלות הפעלה:", "تكلفة التفعيل:", "Стоимость активации:", "Costo de activación:");
        add("score_label", "Score:", "ניקוד:", "النقاط:", "Счёт:", "Puntos:");
        add("lives_label", "Lives:", "חיים:", "الأرواح:", "Жизни:", "Vidas:");
        add("pts", "pts", "נק'", "نقطة", "очк.", "pts");
        add("life", "life", "חיים", "حياة", "жизнь", "vida");
        add("special_effect", "Special effect:", "אפקט מיוחד:", "تأثير خاص:", "Спецэффект:", "Efecto especial:");
        add("good_surprise_msg", "Good surprise!", "הפתעה טובה!", "مفاجأة جيدة!", "Хороший сюрприз!", "¡Buena sorpresa!");
        add("bad_surprise_msg", "Bad surprise!", "הפתעה רעה!", "مفاجأة سيئة!", "Плохой сюрприз!", "¡Mala sorpresa!");
        add("surprise_activated", "The surprise was activated!", "ההפתעה הופעלה!", "تم تفعيل المفاجأة!", "Сюрприз активирован!", "¡La sorpresa fue activada!");
        add("surprise_result", "Surprise result:", "תוצאת ההפתעה:", "نتيجة المفاجأة:", "Результат сюрприза:", "Resultado de la sorpresa:");
        add("good", "Good", "טוב", "جيد", "Хорошо", "Bueno");
        add("bad", "Bad", "רע", "سيء", "Плохо", "Malo");
        add("didnt_answer", "You didn't answer the question.", "לא ענית על השאלה.", "لم تجب على السؤال.", "Вы не ответили на вопрос.", "No respondiste la pregunta.");
        add("activation_cost_deducted", "Activation cost was deducted.", "עלות ההפעלה נוכתה.", "تم خصم تكلفة التفعيل.", "Стоимость активации списана.", "Se dedujo el costo de activación.");

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

        // Exit/Restart Confirmation (ONLY for in-game, not for menus)
        add("exit_title", "Exit", "יציאה", "خروج", "Выход", "Salir");
        add("exit_confirm_msg", "Are you sure you want to exit?\nProgress will be lost.",
                "האם אתה בטוח שברצונך לצאת?\nההתקדמות תאבד.",
                "هل أنت متأكد أنك تريد الخروج؟\nسيتم فقدان التقدم.",
                "Вы уверены, что хотите выйти?\nПрогресс будет потерян.",
                "¿Estás seguro de que quieres salir?\nSe perderá el progreso.");
        add("exit_game", "Exit Game", "יציאה מהמשחק", "الخروج من اللعبة", "Выйти из игры", "Salir del juego");
        add("exit_game_confirm", "Are you sure you want to exit?\nGame progress will be lost.",
                "האם אתה בטוח שברצונך לצאת?\nהתקדמות המשחק תאבד.",
                "هل أنت متأكد أنك تريد الخروج؟\nسيتم فقدان تقدم اللعبة.",
                "Вы уверены, что хотите выйти?\nПрогресс игры будет потерян.",
                "¿Estás seguro de que quieres salir?\nSe perderá el progreso del juego.");
        add("restart_game", "Restart Game", "התחל מחדש", "إعادة اللعبة", "Перезапустить игру", "Reiniciar juego");
        add("restart_confirm", "Are you sure you want to restart?\nCurrent progress will be lost.",
                "האם אתה בטוח שברצונך להתחיל מחדש?\nההתקדמות הנוכחית תאבד.",
                "هل أنت متأكد أنك تريد إعادة اللعبة؟\nسيتم فقدان التقدم الحالي.",
                "Вы уверены, что хотите начать заново?\nТекущий прогресс будет потерян.",
                "¿Estás seguro de que quieres reiniciar?\nSe perderá el progreso actual.");

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

        // How to Play - Full translation
        add("how_to_play", "HOW TO PLAY", "איך לשחק", "كيفية اللعب", "КАК ИГРАТЬ", "CÓMO JUGAR");
        add("how_to_play_intro", "Two players, each has a board.", "שני שחקנים, לכל אחד לוח.", "لاعبان، لكل منهما لوحة.", "Два игрока, у каждого своё поле.", "Dos jugadores, cada uno tiene un tablero.");
        add("how_to_play_shared", "You share lives and score.", "אתם חולקים חיים וניקוד.", "تتشاركون في الأرواح والنقاط.", "Вы делите жизни и очки.", "Comparten vidas y puntuación.");
        add("how_to_play_turn_title", "Your turn:", "התור שלך:", "دورك:", "Ваш ход:", "Tu turno:");
        add("how_to_play_left_click", "Left click = reveal a cell.", "לחיצה שמאלית = חשוף תא.", "النقر الأيسر = كشف خلية.", "Левый клик = открыть клетку.", "Clic izquierdo = revelar celda.");
        add("how_to_play_right_click", "Right click = flag a cell you think is a mine.", "לחיצה ימנית = סמן תא שאתה חושב שהוא מוקש.", "النقر الأيمن = وضع علامة على خلية تعتقد أنها لغم.", "Правый клик = отметить клетку как мину.", "Clic derecho = marcar celda como mina.");
        add("how_to_play_turn_switch", "After your move, the turn switches.", "אחרי המהלך שלך, התור עובר.", "بعد حركتك، ينتقل الدور.", "После хода очередь переходит.", "Después de tu movimiento, el turno cambia.");
        add("how_to_play_cell_types", "Cell types:", "סוגי תאים:", "أنواع الخلايا:", "Типы клеток:", "Tipos de celdas:");
        add("how_to_play_mine", "Mine – losing a life if revealed.", "מוקש – מאבדים חיים אם נחשף.", "لغم – تفقد حياة إذا كُشف.", "Мина – теряете жизнь при открытии.", "Mina – pierdes una vida si se revela.");
        add("how_to_play_number", "Number – tells how many mines around.", "מספר – מראה כמה מוקשים סביב.", "رقم – يخبرك بعدد الألغام المحيطة.", "Число – показывает количество мин вокруг.", "Número – indica cuántas minas hay alrededor.");
        add("how_to_play_question", "Question (Q) – after reveal, you can pay points and answer a quiz (correct gives bonus, wrong can hurt).", "שאלה (Q) – אחרי חשיפה, אפשר לשלם נקודות ולענות על חידון (נכון נותן בונוס, שגוי יכול להזיק).", "سؤال (Q) – بعد الكشف، يمكنك دفع نقاط والإجابة على سؤال (الصحيح يعطي مكافأة، الخطأ يمكن أن يضر).", "Вопрос (Q) – после открытия можно заплатить очки и ответить на вопрос (правильный даёт бонус, неправильный может навредить).", "Pregunta (Q) – después de revelar, puedes pagar puntos y responder un quiz (correcto da bonus, incorrecto puede dañar).");
        add("how_to_play_surprise", "Surprise (S) – after reveal, you can pay points for random good/bad effect.", "הפתעה (S) – אחרי חשיפה, אפשר לשלם נקודות לאפקט טוב/רע אקראי.", "مفاجأة (S) – بعد الكشف، يمكنك دفع نقاط للحصول على تأثير عشوائي جيد/سيء.", "Сюрприз (S) – после открытия можно заплатить очки за случайный эффект.", "Sorpresa (S) – después de revelar, puedes pagar puntos por efecto aleatorio bueno/malo.");
        add("how_to_play_win_lose", "Win / Lose:", "ניצחון / הפסד:", "الفوز / الخسارة:", "Победа / Поражение:", "Ganar / Perder:");
        add("how_to_play_win", "Win = all safe cells cleared.", "ניצחון = כל התאים הבטוחים נחשפו.", "الفوز = تم كشف جميع الخلايا الآمنة.", "Победа = все безопасные клетки открыты.", "Ganar = todas las celdas seguras reveladas.");
        add("how_to_play_lose", "Lose = shared lives reach 0.", "הפסד = החיים המשותפים הגיעו ל-0.", "الخسارة = الأرواح المشتركة تصل إلى 0.", "Поражение = общие жизни достигли 0.", "Perder = las vidas compartidas llegan a 0.");
        add("how_to_play_bonus", "Remaining lives turn into extra score at the end.", "חיים שנותרו הופכים לניקוד נוסף בסוף.", "الأرواح المتبقية تتحول إلى نقاط إضافية في النهاية.", "Оставшиеся жизни превращаются в очки в конце.", "Las vidas restantes se convierten en puntos extra al final.");
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

    /**
     * Get font size multiplier for Arabic (needs bigger text)
     */
    public static float getFontSizeMultiplier(Language lang) {
        return (lang == Language.AR) ? 1.25f : 1.0f;
    }

    /**
     * Get adjusted font size for current language
     */
    public static int getAdjustedFontSize(int baseSize, Language lang) {
        return Math.round(baseSize * getFontSizeMultiplier(lang));
    }
}