package Model.specialcell.factory;

import Model.Board;
import Model.Cell;
import Model.Game;
import Model.specialcell.SpecialCellActivator;

import java.util.HashMap;
import java.util.Map;

public class ActivatorFactoryRegistry {

    private final Map<Cell.CellContent, SpecialCellActivatorFactory> factories = new HashMap<>();

    public ActivatorFactoryRegistry(SpecialCellActivatorFactory... factoriesArr) {
        for (SpecialCellActivatorFactory f : factoriesArr) {
            factories.put(f.supports(), f);
        }
    }

    public SpecialCellActivator create(Cell.CellContent content, Game game, Board board) {
        SpecialCellActivatorFactory f = factories.get(content);
        return (f == null) ? null : f.create(game, board);
    }
}
