package Model.specialcell;

import Model.Board;
import Model.Cell;
import Model.Game;

import java.util.Random;

public class SurpriseActivator extends SpecialCellActivator {

    public SurpriseActivator(Game game, Board board) {
        super(game, board, Cell.CellContent.SURPRISE);
    }

    @Override
    protected ActivationResult doActivation() {
        Random rand = new Random();
        boolean good = rand.nextBoolean();
        int value = game.getDifficulty().getSurpriseValue();

        if (good) {
            // reward: +points +life (life capped inside addLife logic)
            game.setSharedScore(game.getSharedScore() + value);
            game.addLife(value);
            return new ActivationResult(true, false,
                    " Surprise result: GOOD\n" +
                            "Reward: +" + value + " pts, +1 life.");
        } else {
            // penalty: -points -life
            game.setSharedScore(game.getSharedScore() - value);
            game.deductLife(1);
            return new ActivationResult(true, false,
                    " Surprise result: BAD\n" +
                            "Penalty: -" + value + " pts, -1 life.");
        }
    }

    @Override
    protected String buildMessage(ActivationResult result,
                                  int beforeScore, int beforeLives,
                                  int afterScore, int afterLives,
                                  String extra) {
        return " Surprise activated!\n" +
                "Activation cost: -" + cost + " pts\n" +
                result.details + "\n" +
                "Score: " + beforeScore + " → " + afterScore + "\n" +
                "Lives: " + beforeLives + " → " + afterLives;
    }
}
