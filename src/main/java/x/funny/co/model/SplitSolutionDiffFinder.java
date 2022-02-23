package x.funny.co.model;

import x.funny.co.util.AlgoUtils;

import java.util.Arrays;
import java.util.ListIterator;

public class SplitSolutionDiffFinder extends DiffFinder {

    @Override
    public DifferenceList computeDifferenceBetween(String left, String right) {
        if (left == null || right == null) {
            throw new ApplicationLogicRuntimeException("File content must not be null");
        }

        DifferenceList list = fastCheck(left, right);
        if (!list.isEmpty()) {
            return list;
        }

        int gcp = AlgoUtils.findGCP(left, right);
        String greatestCommonPrefix = left.substring(0, gcp);
        left = left.substring(gcp);
        right = right.substring(gcp);

        int gcs = AlgoUtils.findGCS(left, right);
        String greatestCommonSuffix = left.substring(left.length() - gcs);
        left = left.substring(0, left.length() - gcs);
        right = right.substring(0, right.length() - gcs);

        DifferenceList diffs = findDifference(left, right);

        if (greatestCommonPrefix.length() != 0) {
            diffs.addFirst(Difference.of(DifferenceType.EQUALITY, greatestCommonPrefix));
        }
        if (greatestCommonSuffix.length() != 0) {
            diffs.addLast(Difference.of(DifferenceType.EQUALITY, greatestCommonSuffix));
        }

        mergeDifferences(diffs);
        return diffs;
    }

    private DifferenceList fastCheck(String left, String right) {
        DifferenceList diffs = new DifferenceList();
        if (left.equals(right) && !left.isEmpty()) {
            diffs.addEquality(left);
        }
        return diffs;
    }

    @Override
    public void mergeDifferences(DifferenceList diffs) {
        diffs.addEquality("");
        ListIterator<Difference> traverser = diffs.listIterator();
        int removals = 0, insertions = 0;
        String textDelete = "";
        String textInsert = "";
        Difference current = traverser.next();
        Difference prevEquality = null;
        int len;

        while (current != null) {
            switch (current.getType()) {
                case INSERTION:
                    insertions++;
                    textInsert += current.getText();
                    prevEquality = null;
                    break;
                case REMOVAL:
                    removals++;
                    textDelete += current.getText();
                    prevEquality = null;
                    break;
                case EQUALITY:
                    if (removals + insertions > 1) {
                        boolean bothTypes = removals != 0 && insertions != 0;
                        traverser.previous();
                        while (removals-- > 0) {
                            traverser.previous();
                            traverser.remove();
                        }
                        while (insertions-- > 0) {
                            traverser.previous();
                            traverser.remove();
                        }
                        if (bothTypes) {
                            len = AlgoUtils.findGCP(textInsert, textDelete);
                            if (len != 0) {
                                if (traverser.hasPrevious()) {
                                    current = traverser.previous();
                                    current.appendText(textInsert.substring(0, len));
                                    traverser.next();
                                } else {
                                    traverser.add(Difference.of(DifferenceType.EQUALITY, textInsert.substring(0, len)));
                                }
                                textInsert = textInsert.substring(len);
                                textDelete = textDelete.substring(len);
                            }
                            len = AlgoUtils.findGCS(textInsert, textDelete);
                            if (len != 0) {
                                current = traverser.next();
                                current.setText(textInsert.substring(textInsert.length() - len) + current.getText());
                                textInsert = textInsert.substring(0, textInsert.length() - len);
                                textDelete = textDelete.substring(0, textDelete.length() - len);
                                traverser.previous();
                            }
                        }
                        if (textDelete.length() != 0) {
                            traverser.add(Difference.of(DifferenceType.REMOVAL, textDelete));
                        }
                        if (textInsert.length() != 0) {
                            traverser.add(Difference.of(DifferenceType.INSERTION, textInsert));
                        }
                        current = traverser.hasNext() ? traverser.next() : null;
                    } else if (prevEquality != null) {
                        prevEquality.setText(prevEquality.getText() + current.getText());
                        traverser.remove();
                        current = traverser.previous();
                        traverser.next();
                    }
                    insertions = 0;
                    removals = 0;
                    textDelete = "";
                    textInsert = "";
                    prevEquality = current;
                    break;
            }
            current = traverser.hasNext() ? traverser.next() : null;
        }
        if (diffs.getLast().isEmpty()) {
            diffs.removeLast();
        }
        boolean changed = false;
        traverser = diffs.listIterator();
        Difference prevDiff = traverser.hasNext() ? traverser.next() : null;
        current = traverser.hasNext() ? traverser.next() : null;
        Difference nextDiff = traverser.hasNext() ? traverser.next() : null;
        while (nextDiff != null) {
            if (prevDiff.getType() == DifferenceType.EQUALITY && nextDiff.getType() == DifferenceType.EQUALITY) {
                if (current.endsWith(prevDiff)) {
                    current.setText(prevDiff.getText() + current.substring(0, current.length() - prevDiff.length()));
                    nextDiff.setText(prevDiff.getText() + nextDiff.getText());

                    traverser.previous();
                    traverser.previous();
                    traverser.previous();

                    traverser.remove();
                    traverser.next();
                    current = traverser.next();
                    nextDiff = traverser.hasNext() ? traverser.next() : null;
                    changed = true;
                } else if (current.startsWith(nextDiff)) {
                    prevDiff.appendText(nextDiff.getText());
                    current.setText(current.substring(nextDiff.length()) + nextDiff.getText());
                    traverser.remove();
                    nextDiff = traverser.hasNext() ? traverser.next() : null;
                    changed = true;
                }
            }
            prevDiff = current;
            current = nextDiff;
            nextDiff = traverser.hasNext() ? traverser.next() : null;
        }
        if (changed) {
            mergeDifferences(diffs);
        }
    }

    private DifferenceList findDifference(String left, String right) {
        DifferenceList diffs = new DifferenceList();
        if (left.length() == 0) {
            diffs.addInsertion(right);
            return diffs;
        }
        if (right.length() == 0) {
            diffs.addRemoval(left);
            return diffs;
        }
        String longDiff = AlgoUtils.max(left, right);
        String shortDiff = AlgoUtils.min(left, right);
        int i = longDiff.indexOf(shortDiff);
        if (i != -1) {
            DifferenceType type = (left.length() > right.length()) ? DifferenceType.REMOVAL : DifferenceType.INSERTION;
            diffs.add(Difference.of(type, longDiff.substring(0, i)));
            diffs.addEquality(shortDiff);
            diffs.add(Difference.of(type, longDiff.substring(i + shortDiff.length())));
            return diffs;
        }
        if (!shortDiff.contains("\n") || !shortDiff.contains(" ")) {
            diffs.addRemoval(left);
            diffs.addInsertion(right);
            return diffs;
        }
        return bisect(left, right);
    }

    @Override
    public DifferenceList bisect(String left, String right) {
        int l = left.length();
        int r = right.length();
        int maxD = (l + r + 1) / 2;
        int vLength = 2 * maxD;
        int[] v1 = array(vLength);
        int[] v2 = array(vLength);
        v1[maxD + 1] = 0;
        v2[maxD + 1] = 0;
        int delta = l - r;
        boolean front = (delta % 2 != 0);
        var bound1 = new Bound();
        var bound2 = new Bound();
        for (int d = 0; d < maxD; d++) {
            for (int i = bound1.start - d; i <= d - bound1.end; i += 2) {
                int offset = maxD + i;
                int x = (i == -d || (i != d && v1[offset - 1] < v1[offset + 1])) ? v1[offset + 1] : v1[offset - 1] + 1;
                int dx = x - i;
                while (x < l && dx < r && left.charAt(x) == right.charAt(dx)) {
                    x++;
                    dx++;
                }
                v1[offset] = x;
                if (x > l) {
                    bound1.end += 2;
                } else if (dx > r) {
                    bound1.start += 2;
                } else if (front) {
                    int offset2 = maxD + delta - i;
                    if (offset2 >= 0 && offset2 < vLength && v2[offset2] != -1) {
                        if (x >= l - v2[offset2]) {
                            return split(left, right, x, dx);
                        }
                    }
                }
            }

            for (int i = bound2.start - d; i <= d - bound2.end; i += 2) {
                int offset = maxD + i;
                int x = (i == -d || (i != d && v2[offset - 1] < v2[offset + 1])) ? v2[offset + 1] : v2[offset - 1] + 1;
                int dx = x - i;
                while (x < l && dx < r && left.charAt(l - x - 1) == right.charAt(r - dx - 1)) {
                    x++;
                    dx++;
                }
                v2[offset] = x;
                if (x > l) {
                    bound2.end += 2;
                } else if (dx > r) {
                    bound2.start += 2;
                } else if (!front) {
                    int offset2 = maxD + delta - i;
                    if (offset2 >= 0 && offset2 < vLength && v1[offset2] != -1) {
                        int x1 = v1[offset2];
                        int y1 = maxD + x1 - offset2;
                        x = l - x;
                        if (x1 >= x) {
                            return split(left, right, x1, y1);
                        }
                    }
                }
            }
        }

        DifferenceList diffs = new DifferenceList();
        diffs.addRemoval(left);
        diffs.addInsertion(right);
        return diffs;
    }

    private int[] array(int len) {
        int[] arr = new int[len];
        Arrays.fill(arr, -1);
        return arr;
    }

    public static class Bound {
        private int start;
        private int end;
    }

    protected DifferenceList split(String left, String right, int x, int y) {
        DifferenceList differences = computeDifferenceBetween(left.substring(0, x), right.substring(0, y));
        differences.addAll(computeDifferenceBetween(left.substring(x), right.substring(y)));
        return differences;
    }
}
