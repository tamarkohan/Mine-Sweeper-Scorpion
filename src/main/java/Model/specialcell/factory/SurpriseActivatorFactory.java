package Model.specialcell.factory;

import Model.Board;
import Model.Cell;
import Model.Game;
import Model.specialcell.SpecialCellActivator;
import Model.specialcell.SurpriseActivator;

public class SurpriseActivatorFactory extends SpecialCellActivatorFactory {

    @Override
    public Cell.CellContent supports() {
        return Cell.CellContent.SURPRISE;
    }

    @Override
    public SpecialCellActivator create(Game game, Board board) {
        return new SurpriseActivator(game, board);
    }
}
