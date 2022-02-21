package x.funny.co.model;

public abstract class DiffFinder {

    public abstract void mergeDifferences(DifferenceList diffs);

    public abstract DifferenceList computeDifferenceBetween(String left, String right);

    public abstract DifferenceList bisect(String left, String right);
}
