package Model.specialcell.factory;

import Model.Board;
import Model.Cell;
import Model.Game;
import Model.specialcell.SpecialCellActivator;

/**
 * FACTORY METHOD (Creator):
 * Each factory knows how to create one SpecialCellActivator type.
 */
public abstract class SpecialCellActivatorFactory {

    /** Which cell content this factory supports (QUESTION / SURPRISE). */
    public abstract Cell.CellContent supports();

    /** Factory Method */
    public abstract SpecialCellActivator create(Game game, Board board);
}
