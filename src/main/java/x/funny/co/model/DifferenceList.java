package x.funny.co.model;

import java.util.LinkedList;

public class DifferenceList extends LinkedList<Difference> {

    public void add(DifferenceType type, String component) {
        super.add(Difference.of(type, component));
    }

    public void addEquality(String component) {
        add(DifferenceType.EQUALITY, component);
    }

    public void addInsertion(String component) {
        add(DifferenceType.INSERTION, component);
    }

    public void addRemoval(String component) {
        add(DifferenceType.REMOVAL, component);
    }
}
