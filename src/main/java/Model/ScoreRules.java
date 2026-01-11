package Model;

import java.util.Random;

/**
 * Applies scoring/life changes for answering questions,
 * exactly according to the requirements table.
 */
public class ScoreRules {

    public static class Result {
        public final int deltaScore;
        public final int deltaLives;
        public final String details;   //  explanation text

        public Result(int deltaScore, int deltaLives, String details) {
            this.deltaScore = deltaScore;
            this.deltaLives = deltaLives;
            this.details = details;
        }
    }


    private static final Random rnd = new Random();

    /**
     * @param gameDifficulty EASY / MEDIUM / HARD
     * @param qLevel         EASY / MEDIUM / HARD / EXPERT
     * @param correct        whether the answer was correct
     */
    public static Result compute(Difficulty gameDifficulty, Game.QuestionLevel qLevel, boolean correct) {
        return switch (gameDifficulty) {
            case EASY -> easyGame(qLevel, correct);
            case MEDIUM -> mediumGame(qLevel, correct);
            case HARD -> hardGame(qLevel, correct);
        };
    }

    // ==========================
    // EASY GAME RULES
    // ==========================
    private static Result easyGame(Game.QuestionLevel q, boolean correct) {
        if (correct) {
            return switch (q) {
                case EASY -> new Result(+3, +1, "Correct EASY: +3 pts, +1 life.");
                case MEDIUM -> new Result(+6, 0, "Correct MEDIUM: +6 pts. Special effect: reveal 1 mine (Easy game).");
                case HARD ->
                        new Result(+10, 0, "Correct HARD: +10 pts. Special effect: reveal random 3x3 (Easy game).");
                case EXPERT -> new Result(+15, +2, "Correct EXPERT: +15 pts, +2 lives.");
            };
        } else {
            return switch (q) {
                case EASY -> {
                    boolean penalty = rnd.nextBoolean(); // OR nothing
                    yield penalty
                            ? new Result(-3, 0, "Wrong EASY: -3 pts (OR nothing). Chosen: -3 pts.")
                            : new Result(0, 0, "Wrong EASY: -3 pts (OR nothing). Chosen: nothing.");
                }
                case MEDIUM -> {
                    boolean penalty = rnd.nextBoolean();
                    yield penalty
                            ? new Result(-6, 0, "Wrong MEDIUM: -6 pts (OR nothing). Chosen: -6 pts.")
                            : new Result(0, 0, "Wrong MEDIUM: -6 pts (OR nothing). Chosen: nothing.");
                }
                case HARD -> new Result(-10, 0, "Wrong HARD: -10 pts.");
                case EXPERT -> new Result(-15, -1, "Wrong EXPERT: -15 pts, -1 life.");
            };
        }
    }



    // ==========================
// MEDIUM GAME RULES
// ==========================
    private static Result mediumGame(Game.QuestionLevel q, boolean correct) {
        if (correct) {
            return switch (q) {
                case EASY -> new Result(+8, +1, "Correct EASY: +8 pts, +1 life.");
                case MEDIUM -> new Result(+10, +1, "Correct MEDIUM: +10 pts, +1 life.");
                case HARD -> new Result(+15, +1, "Correct HARD: +15 pts, +1 life.");
                case EXPERT -> new Result(+20, +2, "Correct EXPERT: +20 pts, +2 lives.");
            };
        } else {
            return switch (q) {
                case EASY -> new Result(-8, 0, "Wrong EASY: -8 pts.");
                case MEDIUM -> {
                    boolean penalty = rnd.nextBoolean(); // OR nothing
                    yield penalty
                            ? new Result(-10, -1, "Wrong MEDIUM: (-10 pts, -1 life) OR nothing. Chosen: (-10 pts, -1 life).")
                            : new Result(0, 0, "Wrong MEDIUM: (-10 pts, -1 life) OR nothing. Chosen: nothing.");
                }
                case HARD -> new Result(-15, -1, "Wrong HARD: -15 pts, -1 life.");
                case EXPERT -> {
                    boolean optionA = rnd.nextBoolean(); // OR between two penalties
                    yield optionA
                            ? new Result(-20, -1, "Wrong EXPERT: (-20 pts, -1 life) OR (-20 pts, -2 lives). Chosen: (-20 pts, -1 life).")
                            : new Result(-20, -2, "Wrong EXPERT: (-20 pts, -1 life) OR (-20 pts, -2 lives). Chosen: (-20 pts, -2 lives).");
                }
            };
        }
    }

    // ==========================
// HARD GAME RULES
// ==========================
    private static Result hardGame(Game.QuestionLevel q, boolean correct) {
        if (correct) {
            return switch (q) {
                case EASY -> new Result(+10, +1, "Correct EASY: +10 pts, +1 life.");
                case MEDIUM -> {
                    boolean optionA = rnd.nextBoolean(); // OR between +1 or +2 lives
                    yield optionA
                            ? new Result(+15, +1, "Correct MEDIUM: (+15 pts, +1 life) OR (+15 pts, +2 lives). Chosen: (+15 pts, +1 life).")
                            : new Result(+15, +2, "Correct MEDIUM: (+15 pts, +1 life) OR (+15 pts, +2 lives). Chosen: (+15 pts, +2 lives).");
                }
                case HARD -> new Result(+20, +2, "Correct HARD: +20 pts, +2 lives.");
                case EXPERT -> new Result(+40, +3, "Correct EXPERT: +40 pts, +3 lives.");
            };
        } else {
            return switch (q) {
                case EASY -> new Result(-10, -1, "Wrong EASY: -10 pts, -1 life.");
                case MEDIUM -> {
                    boolean optionA = rnd.nextBoolean();
                    yield optionA
                            ? new Result(-15, -1, "Wrong MEDIUM: (-15 pts, -1 life) OR (-15 pts, -2 lives). Chosen: (-15 pts, -1 life).")
                            : new Result(-15, -2, "Wrong MEDIUM: (-15 pts, -1 life) OR (-15 pts, -2 lives). Chosen: (-15 pts, -2 lives).");
                }
                case HARD -> new Result(-20, -2, "Wrong HARD: -20 pts, -2 lives.");
                case EXPERT -> new Result(-40, -3, "Wrong EXPERT: -40 pts, -3 lives.");
            };
        }
    }

}
