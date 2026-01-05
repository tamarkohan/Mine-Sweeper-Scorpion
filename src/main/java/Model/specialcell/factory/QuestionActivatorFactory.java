package Model.specialcell.factory;

import Model.Board;
import Model.Cell;
import Model.Game;
import Model.specialcell.QuestionActivator;
import Model.specialcell.SpecialCellActivator;

public class QuestionActivatorFactory extends SpecialCellActivatorFactory {

    @Override
    public Cell.CellContent supports() {
        return Cell.CellContent.QUESTION;
    }

    @Override
    public SpecialCellActivator create(Game game, Board board) {
        return new QuestionActivator(game, board);
    }
}
