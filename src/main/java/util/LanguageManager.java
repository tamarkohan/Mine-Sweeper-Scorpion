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

        // Language Display Names
        add("lang_english", "English", "English", "English", "English", "English");
        add("lang_hebrew", "עברית", "עברית", "עברית", "עברית", "עברית");
        add("lang_arabic", "العربية", "العربية", "العربية", "العربية", "العربية");
        add("lang_russian", "Русский", "Русский", "Русский", "Русский", "Русский");
        add("lang_spanish", "Español", "Español", "Español", "Español", "Español");

        // ActivationConfirmDialog
        add("question_cell", "Question Cell", "תא שאלה", "خلية سؤال", "Ячейка вопроса", "Celda de pregunta");
        add("this_is_question_cell", "This is a Question cell", "זהו תא שאלה", "هذه خلية سؤال", "Это ячейка вопроса", "Esta es una celda de pregunta");
        add("do_you_want_to_activate", "Do you want to activate it?", "האם ברצונך להפעיל אותו?", "هل تريد تفعيلها؟", "Хотите активировать?", "¿Quieres activarla?");
        add("activate", "Activate", "הפעל", "تفعيل", "Активировать", "Activar");
        add("surprise_cell", "Surprise Cell", "תא הפתעה", "خلية مفاجأة", "Ячейка сюрприза", "Celda de sorpresa");
        add("this_is_surprise_cell", "This is a Surprise cell", "זהו תא הפתעה", "هذه خلية مفاجأة", "Это ячейка сюрприза", "Esta es una celda de sorpresa");

        // Outcome Dialog
        add("outcome_correct", "CORRECT!", "נכון!", "صحيح!", "ПРАВИЛЬНО!", "¡CORRECTO!");
        add("outcome_wrong", "WRONG!", "שגוי!", "خطأ!", "НЕПРАВИЛЬНО!", "¡INCORRECTO!");
        add("outcome_skipped", "SKIPPED", "דילגת", "تم التخطي", "ПРОПУЩЕНО", "OMITIDO");
        add("outcome", "OUTCOME", "תוצאה", "النتيجة", "РЕЗУЛЬТАТ", "RESULTADO");
        add("result", "Result", "תוצאה", "النتيجة", "Результат", "Resultado");
        add("surprise_good", "Good Surprise!", "הפתעה טובה!", "مفاجأة جيدة!", "Хороший сюрприз!", "¡Buena sorpresa!");
        add("surprise_bad", "Bad Surprise!", "הפתעה רעה!", "مفاجأة سيئة!", "Плохой сюрприз!", "¡Mala sorpresa!");
        add("surprise", "SURPRISE!", "הפתעה!", "مفاجأة!", "СЮРПРИЗ!", "¡SORPRESA!");

        add("wrong_prefix", "Wrong", "שגוי", "خطأ", "Неправильно", "Incorrecto");
        add("correct_prefix", "Correct", "נכון", "صحيح", "Правильно", "Correcto");
        add("activation_cost", "Activation cost:", "עלות הפעלה:", "تكلفة التفعيل:", "Стоимость активации:", "Costo de activación:");
        add("score_label", "Score:", "ניקוד:", "النقاط:", "Счёт:", "Puntos:");
        add("lives_label", "Lives:", "חיים:", "الأرواح:", "Жизни:", "Vidas:");
        add("pts", "pts", "נק'", "نقطة", "очк.", "pts");
        add("life", "life", "חיים", "حياة", "жизнь", "vida");
        add("special_effect", "Special effect:", "אפקט מיוחד:", "تأثير خاص:", "Спецэффект:", "Efecto especial:");
        add("good_surprise_msg", "Good surprise!", "הפתעה טובה!", "مفاجأة جيدة!", "Хороший сюрприз!", "¡Buena sorpresa!");
        add("bad_surprise_msg", "Bad Surprise!", "הפתעה רעה!", "مفاجأة سيئة!", "Плохой сюрприз!", "¡Mala sorpresa!");
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

        // Question Management
        add("question_management", "Question Management", "ניהול שאלות", "إدارة الأسئلة", "Управление вопросами", "Gestión de preguntas");
        add("add", "Add", "הוסף", "إضافة", "Добавить", "Añadir");
        add("edit", "Edit", "ערוך", "تعديل", "Редактировать", "Editar");
        add("delete", "Delete", "מחק", "حذف", "Удалить", "Eliminar");
        add("save", "Save", "שמור", "حفظ", "Сохранить", "Guardar");
        add("apply", "Apply", "החל", "تطبيق", "Применить", "Aplicar");
        add("clear", "Clear", "נקה", "مسح", "Очистить", "Limpiar");
        add("difficulty", "Difficulty:", "רמת קושי:", "الصعوبة:", "Сложность:", "Dificultad:");
        add("correct_label", "Correct:", "תשובה נכונה:", "الإجابة الصحيحة:", "Правильный:", "Correcta:");
        add("all", "All", "הכל", "الكل", "Все", "Todos");
        add("select_question_edit", "Please select a question to edit", "אנא בחר שאלה לעריכה", "يرجى اختيار سؤال للتعديل", "Выберите вопрос для редактирования", "Por favor selecciona una pregunta para editar");
        add("select_question_delete", "Please select a question to delete", "אנא בחר שאלה למחיקה", "يرجى اختيار سؤال للحذف", "Выберите вопрос для удаления", "Por favor selecciona una pregunta para eliminar");
        add("no_selection", "No Selection", "לא נבחרה שאלה", "لا يوجد اختيار", "Нет выбора", "Sin selección");
        add("delete_question", "Delete Question", "מחיקת שאלה", "حذف سؤال", "Удалить вопрос", "Eliminar pregunta");
        add("delete_confirm", "Are you sure you want to delete this question?\nThis action cannot be undone.",
                "האם אתה בטוח שברצונך למחוק את השאלה הזו?\nלא ניתן לבטל פעולה זו.",
                "هل أنت متأكد أنك تريد حذف هذا السؤال؟\nلا يمكن التراجع عن هذا الإجراء.",
                "Вы уверены, что хотите удалить этот вопрос?\nЭто действие нельзя отменить.",
                "¿Estás seguro de que quieres eliminar esta pregunta?\nEsta acción no se puede deshacer.");
        add("translation_failed", "Translation failed. Check Azure key / internet.",
                "תרגום נכשל. בדקי מפתח Azure/אינטרנט.",
                "فشلت الترجمة. تحقق من مفتاح Azure / الإنترنت.",
                "Ошибка перевода. Проверьте ключ Azure / интернет.",
                "La traducción falló. Verifica la clave de Azure / internet.");
        add("error", "Error", "שגיאה", "خطأ", "Ошибка", "Error");
        add("could_not_load", "Could not load question", "לא ניתן לטעון את השאלה", "تعذر تحميل السؤال", "Не удалось загрузить вопрос", "No se pudo cargar la pregunta");
        add("questions_saved", "Questions saved to CSV.", "השאלות נשמרו לקובץ CSV.", "تم حفظ الأسئلة في CSV.", "Вопросы сохранены в CSV.", "Preguntas guardadas en CSV.");
        add("saved", "Saved", "נשמר", "تم الحفظ", "Сохранено", "Guardado");
        add("add_new_question", "Add New Question", "הוסף שאלה חדשה", "أضف سؤالاً جديداً", "Добавить новый вопрос", "Añadir nueva pregunta");
        add("edit_question", "Edit Question", "ערוך שאלה", "تعديل سؤال", "Редактировать вопрос", "Editar pregunta");
        add("id", "ID", "מזהה", "المعرف", "ID", "ID");
        add("question_text", "Question Text", "טקסט השאלה", "نص السؤال", "Текст вопроса", "Texto de la pregunta");
        add("option_a", "Option A", "תשובה א", "الخيار أ", "Вариант A", "Opción A");
        add("option_b", "Option B", "תשובה ב", "الخيار ب", "Вариант B", "Opción B");
        add("option_c", "Option C", "תשובה ג", "الخيار ج", "Вариант C", "Opción C");
        add("option_d", "Option D", "תשובה ד", "الخيار د", "Вариант D", "Opción D");
        add("correct_answer_label", "Correct Answer", "תשובה נכונה", "الإجابة الصحيحة", "Правильный ответ", "Respuesta correcta");
        add("difficulty_label", "Difficulty", "רמת קושי", "الصعوبة", "Сложность", "Dificultad");
        add("sort_hint", "Tip: Click on column headers to sort", "טיפ: ניתן ללחוץ על כותרות העמודות כדי למיין",
                "تلميح: انقر على عناوين الأعمدة للترتيب", "Совет: нажмите на заголовки столбцов для сортировки",
                "Consejo: Haz clic en los encabezados de las columnas para ordenar");

        // ** NEW KEYS FOR VALIDATION **
        add("validation_error", "Validation Error", "שגיאת אימות", "خطأ في التحقق", "Ошибка проверки", "Error de validación");
        add("fill_all_fields", "All fields must be filled.", "יש למלא את כל השדות.", "يجب ملء جميع الحقول.", "Все поля должны быть заполнены.", "Todos los campos deben ser completados.");

        add("exit_game", "Exit Game", "יציאה מהמשחק", "الخروج من اللعبة", "Выход из игры", "Salir del juego");
        add("exit_confirm", "Are you sure you want to exit?\nGame progress will be lost.",
                "האם אתה בטוח שברצונך לצאת?\nההתקדמות במשחק תאבד.",
                "هل أنت متأكد أنك تريد الخروج؟\nسيتم فقدان تقدم اللعبة.",
                "Вы уверены, что хотите выйти?\nПрогресс игры будет потерян.",
                "¿Estás seguro de que quieres salir?\nSe perderá el progreso del juego.");
        add("restart_game", "Restart Game", "התחלה מחדש", "إعادة اللعبة", "Начать заново", "Reiniciar juego");
        add("restart_confirm", "Are you sure you want to restart?\nCurrent progress will be lost.",
                "האם אתה בטוח שברצונך להתחיל מחדש?\nההתקדמות הנוכחית תאבד.",
                "هل أنت متأكد أنك تريد إعادة اللعبة؟\nسيتم فقدان التقدم الحالي.",
                "Вы уверены, что хотите начать заново?\nТекущий прогресс будет потерян.",
                "¿Estás seguro de que quieres reiniciar?\nSe perderá el progreso actual.");
        add("exit_title", "Exit", "יציאה", "خروج", "Выход", "Salir");
        add("exit_confirm_msg", "Are you sure you want to exit?", "האם אתה בטוח שברצונך לצאת?",
                "هل أنت متأكد أنك تريد الخروج؟", "Вы уверены, что хотите выйти?", "¿Estás seguro de que quieres salir?");
        add("restart_title", "Restart", "התחל מחדש", "إعادة", "Заново", "Reiniciar");
        add("restart_confirm_msg", "Are you sure you want to restart?\nCurrent progress will be lost.",
                "האם אתה בטוח שברצונך להתחיל מחדש?\nההתקדמות הנוכחית תאבד.",
                "هل أنت متأكد أنك تريد إعادة اللعبة؟\nسيتم فقدان التقدم الحالي.",
                "Вы уверены, что хотите начать заново?\nТекущий прогресс будет потерян.",
                "¿Estás seguro de que quieres reiniciar?\nSe perderá el progreso actual.");
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
        add("yes", "Yes", "כן", "نعم", "Да", "Sí");
        add("no", "No", "לא", "لا", "Нет", "No");
        add("game_history", "Game History", "היסטוריית משחקים", "سجل الألعاب", "История игр", "Historial de juegos");
        add("no_history", "No game history yet.", "אין היסטוריית משחקים עדיין.", "لا يوجد سجل ألعاب بعد.", "История игр пока пуста.", "Aún no hay historial de juegos.");
        add("back", "Back", "חזור", "رجوع", "Назад", "Volver");
        add("search", "Search", "חפש", "بحث", "Поиск", "Buscar");
        add("search_label", "Search:", "חיפוש:", "بحث:", "Поиск:", "Buscar:");
        add("result_label", "Result:", "תוצאה:", "النتيجة:", "Результат:", "Resultado:");
        add("won", "WON", "ניצחון", "فوز", "ПОБЕДА", "GANADO");
        add("lost", "LOST", "הפסד", "خسارة", "ПОРАЖЕНИЕ", "PERDIDO");
        add("players", "Players", "שחקנים", "اللاعبون", "Игроки", "Jugadores");
        add("date_time", "Date / Time", "תאריך / שעה", "التاريخ / الوقت", "Дата / Время", "Fecha / Hora");
        add("final_score", "Final Score", "ניקוד סופי", "النتيجة النهائية", "Итоговый счёт", "Puntuación final");
        add("lives_left", "Lives Left", "חיים נותרו", "الأرواح المتبقية", "Осталось жизней", "Vidas restantes");
        add("correct_answers", "Correct Ans", "תשובות נכונות", "إجابات صحيحة", "Правильных ответов", "Respuestas correctas");
        add("accuracy", "Accuracy", "דיוק", "الدقة", "Точность", "Precisión");
        add("duration", "Duration", "משך זמן", "المدة", "Продолжительность", "Duración");
        add("player", "Player", "שחקן", "اللاعب", "Игрок", "Jugador");
        add("total_games", "Total Games", "סה\"כ משחקים", "إجمالي الألعاب", "Всего игр", "Total de juegos");
        add("best_score", "Best Score", "תוצאה טובה", "أفضل نتيجة", "Лучший счёт", "Mejor puntuación");
        add("avg_accuracy", "Avg Accuracy", "דיוק ממוצע", "متوسط الدقة", "Средняя точность", "Precisión promedio");
        add("pref_difficulty", "Pref Difficulty", "רמה מועדפת", "الصعوبة المفضلة", "Любимая сложность", "Dificultad preferida");
        add("flag", "Flag", "דגל", "علامة", "Флаг", "Bandera");
        add("no_flags_left", "No flags left!\nYou already used all flags.\nRemove a flag to place a new one.",
                "אין דגלים נותרים!\nכבר השתמשת בכל הדגלים.\nהסר דגל כדי להניח חדש.",
                "لا توجد أعلام متبقية!\nلقد استخدمت جميع الأعلام.\nأزل علامة لوضع واحدة جديدة.",
                "Флаги закончились!\nВы использовали все флаги.\nУберите флаг, чтобы поставить новый.",
                "¡No quedan banderas!\nYa usaste todas las banderas.\nQuita una bandera para colocar una nueva.");
        add("reward", "Reward", "פרס", "مكافأة", "Награда", "Recompensa");
        add("penalty", "Penalty", "עונש", "عقوبة", "Штраф", "Penalización");
        add("reveal_mine", "reveal 1 mine", "חשיפת מוקש אחד", "كشف لغم واحد", "открыть 1 мину", "revelar 1 mina");
        add("revealed_mine", "revealed 1 mine", "נחשף מוקש אחד", "تم كشف لغم واحد", "открыта 1 мина", "se reveló 1 mina");
        add("reveal_3x3", "reveal random 3x3", "חשיפת 3x3 אקראי", "كشف 3x3 عشوائي", "открыть случайную 3x3 область", "revelar 3x3 aleatorio");
        add("revealed_3x3", "revealed random 3x3 area", "נחשף אזור 3x3 אקראי", "تم كشف منطقة 3x3 عشوائية", "открыта случайная область 3x3", "se reveló área 3x3 aleatoria");
        add("or_nothing", "OR nothing", "או כלום", "أو لا شيء", "ИЛИ ничего", "O nada");
        add("chosen_nothing", "Chosen: nothing", "נבחר: כלום", "المختار: لا شيء", "Выбрано: ничего", "Elegido: nada");
        add("chosen", "Chosen", "נבחר", "المختار", "Выбрано", "Elegido");
        add("how_to_play", "HOW TO PLAY", "איך לשחק", "كيفية اللعب", "КАК ИГРАТЬ", "CÓMO JUGAR");
        add("how_to_play_intro", "Two players, each has a board.", "שני שחקנים, לכל אחד לוח.", "لاعبان، لكل منهما لوحة.", "Два игрока, у каждого своё поле.", "Dos jugadores, cada uno tiene un tablero.");
        add("how_to_play_shared", "You share lives and score.", "אתם חולקים חיים וניקוד.", "تتشاركون في الأرواح والنقاط.", "Вы делите жизни и очки.", "Comparten vidas y puntuación.");
        add("how_to_play_turn_title", "Your turn:", "התור שלך:", "دورك:", "Ваш ход:", "Tu turno:");
        add("how_to_play_left_click", "Left click = reveal a cell.", "לחיצה שמאלית = חשוף תא.", "النقر الأيسر = كشف خلية.", "Левый клик = открыть клетку.", "Clic izquierdo = revelar celda.");
        add("how_to_play_right_click", "Right click = flag a cell you think is a mine.", "לחיצה ימנית = סמן תא שאתה חושב שהוא מוקש.", "النقر الأיمن = وضع علامة على خلية تعتقد أنها لغم.", "Правый клик = отметить клетку как мину.", "Clic derecho = marcar celda como mina.");
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

    public static boolean isRTL(Language lang) {
        return lang == Language.HE || lang == Language.AR;
    }

    public static String getDisplayName(Language lang) {
        return switch (lang) {
            case EN -> "English";
            case HE -> "עברית";
            case AR -> "العربية";
            case RU -> "Русский";
            case ES -> "Español";
        };
    }

    public static Language[] getAllLanguages() {
        return Language.values();
    }

    public static Language getNextLanguage(Language current) {
        Language[] all = Language.values();
        int idx = current.ordinal();
        return all[(idx + 1) % all.length];
    }

    public static float getFontSizeMultiplier(Language lang) {
        return (lang == Language.AR) ? 1.25f : 1.0f;
    }

    public static int getAdjustedFontSize(int baseSize, Language lang) {
        return Math.round(baseSize * getFontSizeMultiplier(lang));
    }

    public static String getLanguageCode(Language lang) {
        return switch (lang) {
            case EN -> "en";
            case HE -> "he";
            case AR -> "ar";
            case RU -> "ru";
            case ES -> "es";
        };
    }
}