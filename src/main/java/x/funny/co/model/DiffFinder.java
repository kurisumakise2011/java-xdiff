package x.funny.co.model;

import java.util.LinkedList;

public abstract class DiffFinder {

    enum DifferenceType {
        INSERTION, REMOVAL, EQUALITY
    }

    static class Difference {
        final DifferenceType type;
        String text;

        public Difference(DifferenceType type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    public abstract void mergeDifferences(LinkedList<Difference> diffs);

    public abstract LinkedList<DiffFinder.Difference> computeDifferenceBetween(String left, String right);

    public void removalCase(DifferenceContext ctx) {}

    public void insertionCase(DifferenceContext ctx) {}

    public void equalityCase(DifferenceContext ctx) {}

    public static class DifferenceContext {
        LinkedList<Difference> differences;
        Difference current;
        Difference prevEquality;
    }
}
