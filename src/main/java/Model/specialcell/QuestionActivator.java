package Model.specialcell;

import Model.*;

public class QuestionActivator extends SpecialCellActivator {

    private Question question; // chosen question cached

    public QuestionActivator(Game game, Board board) {
        super(game, board, Cell.CellContent.QUESTION);
    }

    @Override
    protected String preChecks() {
        if (game.getQuestionManager() == null) return "Question system is not available.";
        // presenter is private in Game; we will add a getter OR use a helper method (see step 6 below)
        if (!game.hasQuestionPresenter()) return "Question system is not available.";
        return null;
    }

    @Override
    protected ActivationResult doActivation() {
        QuestionManager qm = game.getQuestionManager();

        question = qm.getRandomUnusedQuestionAnyLevel();
        if (question == null) {
            return new ActivationResult(false, false, "No questions available.");
        }

        //  get 3-state result from UI
        QuestionResult ans = game.presentQuestion(question);

        //  SKIPPED: only activation cost was paid (by template), no wrong penalty, no stats
        if (ans == QuestionResult.SKIPPED) {
            return new ActivationResult(true, false,
                    "You didn't answer the question.\nActivation cost was deducted.");
        }

        boolean isCorrect = (ans == QuestionResult.CORRECT);
        Game.QuestionLevel level = question.getQuestionLevel();

        ScoreRules.Result r = game.processQuestionAnswer(level, isCorrect);

        String base =
                (isCorrect ? "Correct!\n" : "Wrong!\n") +
                        r.details;

        return new ActivationResult(true, isCorrect, base);
    }


    @Override
    protected String extraEffects(ActivationResult result) {
        if (question == null) return "";

        boolean isCorrect = result.isCorrect;
        Game.QuestionLevel level = question.getQuestionLevel();

        //  existing "Easy game special effects"
        if (game.getDifficulty() == Model.Difficulty.EASY && isCorrect) {
            if (level == Game.QuestionLevel.MEDIUM) {
                board.revealRandomMine();
                return "\nSpecial effect: revealed 1 mine (reward).";
            } else if (level == Game.QuestionLevel.HARD) {
                board.revealRandom3x3AreaReward();
                return "\nSpecial effect: revealed random 3x3 area (reward).";
            }
        }
        return "";
    }

    @Override
    protected String buildMessage(ActivationResult result,
                                  int beforeScore, int beforeLives,
                                  int afterScore, int afterLives,
                                  String extra) {

        // if question missing etc
        if (!result.success) {
            return result.details;
        }

        return result.details + "\n" +
                "Activation cost: -" + cost + " pts" +
                extra +
                "\nScore: " + beforeScore + " → " + afterScore +
                "\nLives: " + beforeLives + " → " + afterLives;
    }
}
