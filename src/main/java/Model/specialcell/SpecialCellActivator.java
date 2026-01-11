package Model.specialcell;

import Model.Board;
import Model.Cell;
import Model.Game;

/**
 * TEMPLATE METHOD:
 * Defines the skeleton for activating a special cell (Question/Surprise).
 * Subclasses customize only the variable steps.
 */
public abstract class SpecialCellActivator {

    protected final Game game;
    protected final Board board;
    protected final Cell.CellContent content;
    protected final int cost;

    protected SpecialCellActivator(Game game, Board board, Cell.CellContent content) {
        this.game = game;
        this.board = board;
        this.content = content;
        this.cost = game.getDifficulty().getActivationCost();
    }

    /**
     * The TEMPLATE METHOD (skeleton).
     * final -> subclasses cannot change the algorithm order.
     */
    public final boolean activate() {
        // 1) Validate type (only QUESTION/SURPRISE)
        if (!isSupportedContent(content)) return false;

        // 2) Validate enough score
        if (!hasEnoughScore()) {
            game.setLastActionMessage(
                    "You need at least " + cost + " points to activate this " +
                            content.name().toLowerCase() + " cell."
            );
            return false;
        }

        // 3) Pre-checks (subclass can block, e.g., question system unavailable)
        String preError = preChecks();
        if (preError != null) {
            game.setLastActionMessage(preError);
            return false;
        }

        // 4) Snapshot before
        int beforeScore = game.getSharedScore();
        int beforeLives = game.getSharedLives();

        // 5) Pay activation cost
        payCost();

        // 6) Do the special activation (subclass-specific)
        ActivationResult result = doActivation();

        // 7) Extra hook (optional, default empty)
        String extra = extraEffects(result);

        // 8) Snapshot after
        int afterScore = game.getSharedScore();
        int afterLives = game.getSharedLives();

        // 9) Build message (subclass can format)
        String msg = buildMessage(result, beforeScore, beforeLives, afterScore, afterLives, extra);
        game.setLastActionMessage(msg);

        return true;
    }

    // ------------------------
    // FIXED steps helpers
    // ------------------------
    protected boolean isSupportedContent(Cell.CellContent c) {
        return c == Cell.CellContent.QUESTION || c == Cell.CellContent.SURPRISE;
    }

    protected boolean hasEnoughScore() {
        return game.getSharedScore() >= cost;
    }

    protected void payCost() {
        game.setSharedScore(game.getSharedScore() - cost);
        // setSharedScore already checks status in your code
    }

    // ------------------------
    // PRIMITIVE / HOOK methods
    // ------------------------

    /**
     * Subclass pre-checks. Return error message to block activation; or null to continue.
     */
    protected String preChecks() {
        return null;
    }

    /**
     * The variable core step.
     */
    protected abstract ActivationResult doActivation();

    /**
     * Optional extra behavior after activation; default none.
     */
    protected String extraEffects(ActivationResult result) {
        return "";
    }

    /**
     * Subclass builds the view message.
     */
    protected abstract String buildMessage(ActivationResult result,
                                           int beforeScore, int beforeLives,
                                           int afterScore, int afterLives,
                                           String extra);

    // ------------------------
    // Result object
    // ------------------------
    public static class ActivationResult {
        public final boolean success;
        public final boolean isCorrect;     // relevant for question; false for surprise
        public final String details;        // explanation lines

        public ActivationResult(boolean success, boolean isCorrect, String details) {
            this.success = success;
            this.isCorrect = isCorrect;
            this.details = details;
        }
    }
}
