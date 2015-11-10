package tu_darmstadt.sudoku.controller;

import tu_darmstadt.sudoku.game.*;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by Chris on 06.11.2015.
 */
public class GameController {

    private int size;
    private GameField gameField;
    private ArrayList<CheckError> errorList = new ArrayList<CheckError>();
    private GameSettings settings = new GameSettings();

//    private SudokuSolver solver;
//    private SudokuGenerator generator;

    public GameController() {
        this(GameType.Default_9x9);
    }

    public GameController(GameType type) {
        gameField = new GameField(type);
        size = gameField.getSize();
    }

    /*public boolean loadLevel(GameField level) {
        if(GameField.isValid(level)) {
            gameField = level;
        }
    }*/

    public void setValue(int row, int col, int value) {
        GameCell cell = gameField.getCell(row, col);
        if (!cell.isFixed() && isValidNumber(value)) {
            cell.deleteNotes();
            cell.setValue(value);

            if(settings.getEnableAutomaticNoteDeletion()) {
                LinkedList<GameCell> updateList = new LinkedList<GameCell>();
                updateList.addAll(gameField.getRow(cell.getRow()));
                updateList.addAll(gameField.getColumn(cell.getCol()));
                updateList.addAll(gameField.getSection(cell.getRow(), cell.getCol()));
                deleteNote(updateList, value);
            }

        }
    }

    public void deleteNote(List<GameCell> updateList, int value) {
        for(GameCell c : updateList) {
            c.deleteNote(value);
        }
    }

    public int getValue(int row, int col) {
        GameCell cell = gameField.getCell(row, col);
        return cell.getValue();
    }

    public int getSize() {
        return size;
    }

    /**
     * Test if the given parameter is a number from 1 to {code size}.
     * @param val the value to be checked.
     * @return true, if {@code val} is a number from 1 to {code size}.
     * @see GameController#getSize()
     */
    public boolean isValidNumber(int val) {
        return 0 < val && val <= size;
    }

    public boolean isSolved() {
        boolean solved = true;

        // this will automatically build the CheckError list. so we reset it before we call the checks
        errorList = new ArrayList<CheckError>();

        for(int i = 0; i < size; i++) {
            if(!checkList(gameField.getRow(i))) solved = false;
            if(!checkList(gameField.getColumn(i))) solved = false;
            if(!checkList(gameField.getSection(i))) solved = false;
        }
        return solved;
    }

    /**
     * Checks the given list if every number occurs only once.
     * This method will automatically build the errorList.
     * @param list the list of {@link GameCell}s that is supposed to be tested.
     * @return true if every cell has a value and every value occurs only once
     */
    private boolean checkList(List<GameCell> list) {
        boolean isNothingEmpty = true;
        CheckError lastFound = null;

        for(int i = 0; i < list.size(); i++) {
            for(int j = i + 1; j < list.size(); j++) {
                GameCell c1 = list.get(i);
                GameCell c2 = list.get(j);

                if(c1.getValue() == 0 || c2.getValue() == 0) {
                    isNothingEmpty = false;
                }

                // Same value in one set should not exist
                if(c1.getValue() != 0 && c1.getValue() == c2.getValue()) {
                    // we found an error..

                    LinkedList<CheckError> removeList = new LinkedList<CheckError>();

                    // check if one of the cells is already in conflict with something else
                    if(errorList.size() == 0) {
                        lastFound = new CheckError(c1, c2);
                        errorList.add(lastFound);
                    }
                    for(CheckError ce : errorList) {
                        if(ce.contains(c1) || ce.contains(c2)) {
                            ce.add(c1);
                            ce.add(c2);

                            if(lastFound != null && !lastFound.equals(ce)) {
                                lastFound.merge(ce);
                                removeList.add(ce);
                            } else {
                                lastFound = ce;
                            }
                        } else {
                            lastFound = new CheckError(c1, c2);
                            errorList.add(lastFound);
                        }
                    }

                    // remove the empty errors, that have been merged.
                    for(CheckError ce : removeList) {
                        errorList.remove(ce);
                    }
                }
            }
        }
        return isNothingEmpty ? (errorList.size() == 0) : false;
    }

    public List<CheckError> getErrorList() {
        return errorList;
    }

    /** Debug only method
     *
     * @return the Field represented as a String
     */
    public String getFieldAsString() {
        return gameField.toString();
    }
}
