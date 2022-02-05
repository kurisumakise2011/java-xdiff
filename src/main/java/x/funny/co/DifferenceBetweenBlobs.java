package x.funny.co;

import java.awt.*;

public class DifferenceBetweenBlobs {
    private Blob left;
    private Blob right;

    public DifferenceBetweenBlobs() {
    }

    public void appendFile(Blob target, String constraint) {
        if (BorderLayout.WEST.equals(constraint)) {
            left = target;
        }
        if (BorderLayout.EAST.equals(constraint)) {
            right = target;
        }
    }

    public void findDiff() {
        if (!canBeCompared()) {
            return;
        }
    }

    public boolean canBeCompared() {
        return left != null && right != null;
    }

    public Blob getLeft() {
        return left;
    }

    public Blob getRight() {
        return right;
    }

    public void show() {

    }
}
