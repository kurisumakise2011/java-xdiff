package x.funny.co.model;

import java.util.LinkedList;
import java.util.ListIterator;

public class SplitSolutionDiffFinder extends DiffFinder {

    @Override
    public LinkedList<DiffFinder.Difference> computeDifferenceBetween(String left, String right) {
        if (left == null || right == null) {
            throw new ApplicationLogicRuntimeException("File content must not be null");
        }

        LinkedList<Difference> list = fastCheck(left, right);
        if (!list.isEmpty()) {
            return list;
        }

        int gcp = findGCP(left, right);
        String greatestCommonPrefix = left.substring(0, gcp);
        left = left.substring(gcp);
        right = right.substring(gcp);

        int gcs = findGCS(left, right);
        String greatestCommonSuffix = left.substring(left.length() - gcs);
        left = left.substring(0, left.length() - gcs);
        right = right.substring(0, right.length() - gcs);

        LinkedList<Difference> diffs = findDifference(left, right);

        if (greatestCommonPrefix.length() != 0) {
            diffs.addFirst(new Difference(DifferenceType.EQUALITY, greatestCommonPrefix));
        }
        if (greatestCommonSuffix.length() != 0) {
            diffs.addLast(new Difference(DifferenceType.EQUALITY, greatestCommonSuffix));
        }

        mergeDifferences(diffs);
        return diffs;
    }

    private LinkedList<DiffFinder.Difference> fastCheck(String left, String right) {
        LinkedList<DiffFinder.Difference> diffs = new LinkedList<>();
        if (left.equals(right) && !left.isEmpty()) {
            diffs.add(new Difference(DifferenceType.EQUALITY, left));
        }
        return diffs;
    }

    @Override
    public void mergeDifferences(LinkedList<Difference> diffs) {
        DifferenceContext context = new DifferenceContext();
        context.differences = diffs;
        diffs.add(new Difference(DifferenceType.EQUALITY, ""));
        ListIterator<Difference> traverser = diffs.listIterator();
        int removals = 0, insertions = 0;
        String textDelete = "";
        String textInsert = "";
        Difference current = traverser.next();
        Difference prevEquality = null;
        int len;

        context.current = current;
        context.prevEquality = null;

        while (current != null) {
            switch (current.type) {
                case INSERTION:
                    insertionCase(context);
                    insertions++;
                    textInsert += current.text;
                    prevEquality = null;
                    break;
                case REMOVAL:
                    removalCase(context);
                    removals++;
                    textDelete += current.text;
                    prevEquality = null;
                    break;
                case EQUALITY:
                    equalityCase(context);
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
                            len = findGCP(textInsert, textDelete);
                            if (len != 0) {
                                if (traverser.hasPrevious()) {
                                    current = traverser.previous();
                                    current.text += textInsert.substring(0, len);
                                    traverser.next();
                                } else {
                                    traverser.add(new Difference(DifferenceType.EQUALITY,
                                            textInsert.substring(0, len)));
                                }
                                textInsert = textInsert.substring(len);
                                textDelete = textDelete.substring(len);
                            }
                            len = findGCS(textInsert, textDelete);
                            if (len != 0) {
                                current = traverser.next();
                                current.text = textInsert.substring(textInsert.length() -
                                        len) + current.text;
                                textInsert = textInsert.substring(0, textInsert.length() -
                                        len);
                                textDelete = textDelete.substring(0, textDelete.length() -
                                        len);
                                traverser.previous();
                            }
                        }
                        if (textDelete.length() != 0) {
                            traverser.add(new Difference(DifferenceType.REMOVAL, textDelete));
                        }
                        if (textInsert.length() != 0) {
                            traverser.add(new Difference(DifferenceType.INSERTION, textInsert));
                        }
                        current = traverser.hasNext() ? traverser.next() : null;
                    } else if (prevEquality != null) {
                        prevEquality.text = prevEquality.text + current.text;
                        traverser.remove();
                        current = traverser.previous();
                        traverser.next();
                    }
                    insertions = 0;
                    removals = 0;
                    textDelete = "";
                    textInsert = "";
                    prevEquality = current;
                    context.prevEquality = prevEquality;
                    break;
            }
            current = traverser.hasNext() ? traverser.next() : null;
        }
        if (diffs.getLast().text.length() == 0 || diffs.getLast().text.isEmpty()) {
            diffs.removeLast();
        }
        boolean changed = false;
        traverser = diffs.listIterator();
        Difference prevDiff = traverser.hasNext() ? traverser.next() : null;
        current = traverser.hasNext() ? traverser.next() : null;
        Difference nextDiff = traverser.hasNext() ? traverser.next() : null;
        while (nextDiff != null) {
            if (prevDiff.type == DifferenceType.EQUALITY &&
                    nextDiff.type == DifferenceType.EQUALITY) {
                if (current.text.endsWith(prevDiff.text)) {
                    current.text = prevDiff.text + current.text.substring(0, current.text.length() - prevDiff.text.length());
                    nextDiff.text = prevDiff.text + nextDiff.text;
                    // skip next difference
                    traverser.previous();
                    // skip current difference
                    traverser.previous();
                    // skip previous difference
                    traverser.previous();

                    traverser.remove();

                    // current difference
                    traverser.next();
                    current = traverser.next();
                    nextDiff = traverser.hasNext() ? traverser.next() : null;
                    changed = true;
                } else if (current.text.startsWith(nextDiff.text)) {
                    prevDiff.text += nextDiff.text;
                    current.text = current.text.substring(nextDiff.text.length()) + nextDiff.text;
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

    public static int findGCP(String left, String right) {
        int n = Math.min(left.length(), right.length());
        for (int i = 0; i < n; i++) {
            if (left.charAt(i) != right.charAt(i)) {
                return i;
            }
        }
        return n;
    }

    public static int findGCS(String left, String right) {
        int n = Math.min(left.length(), right.length());
        for (int i = 1; i <= n; i++) {
            if (left.charAt(left.length() - i) != right.charAt(right.length() - i)) {
                return i - 1;
            }
        }
        return n;
    }

    private LinkedList<Difference> findDifference(String left, String right) {
        LinkedList<Difference> diffs = new LinkedList<>();
        if (left.length() == 0) {
            diffs.add(new Difference(DifferenceType.INSERTION, right));
            return diffs;
        }
        if (right.length() == 0) {
            diffs.add(new Difference(DifferenceType.REMOVAL, left));
            return diffs;
        }
        String longDiff = left.length() > right.length() ? left : right;
        String shortDiff = left.length() > right.length() ? right : left;
        int i = longDiff.indexOf(shortDiff);
        if (i != -1) {
            DifferenceType type = (left.length() > right.length()) ? DifferenceType.REMOVAL : DifferenceType.INSERTION;
            diffs.add(new Difference(type, longDiff.substring(0, i)));
            diffs.add(new Difference(DifferenceType.EQUALITY, shortDiff));
            diffs.add(new Difference(type, longDiff.substring(i + shortDiff.length())));
            return diffs;
        }
        if (shortDiff.length() == 1) {
            diffs.add(new Difference(DifferenceType.REMOVAL, left));
            diffs.add(new Difference(DifferenceType.INSERTION, right));
            return diffs;
        }
        return bisect(left, right);
    }

    protected LinkedList<Difference> bisect(String left, String right) {
        int l = left.length();
        int r = right.length();
        int maxD = (l + r + 1) / 2;
        int vLength = 2 * maxD;
        int[] v1 = new int[vLength];
        int[] v2 = new int[vLength];
        for (int x = 0; x < vLength; x++) {
            v1[x] = -1;
            v2[x] = -1;
        }
        v1[maxD + 1] = 0;
        v2[maxD + 1] = 0;
        int delta = l - r;
        boolean front = (delta % 2 != 0);
        int k1start = 0;
        int k1end = 0;
        int k2start = 0;
        int k2end = 0;
        for (int d = 0; d < maxD; d++) {
            for (int k1 = -d + k1start; k1 <= d - k1end; k1 += 2) {
                int k1Offset = maxD + k1;
                int x1;
                if (k1 == -d || (k1 != d && v1[k1Offset - 1] < v1[k1Offset + 1])) {
                    x1 = v1[k1Offset + 1];
                } else {
                    x1 = v1[k1Offset - 1] + 1;
                }
                int y1 = x1 - k1;
                while (x1 < l && y1 < r && left.charAt(x1) == right.charAt(y1)) {
                    x1++;
                    y1++;
                }
                v1[k1Offset] = x1;
                if (x1 > l) {
                    k1end += 2;
                } else if (y1 > r) {
                    k1start += 2;
                } else if (front) {
                    int k2Offset = maxD + delta - k1;
                    if (k2Offset >= 0 && k2Offset < vLength && v2[k2Offset] != -1) {
                        int x2 = l - v2[k2Offset];
                        if (x1 >= x2) return split(left, right, x1, y1);
                    }
                }
            }

            for (int k2 = -d + k2start; k2 <= d - k2end; k2 += 2) {
                int k2Offset = maxD + k2;
                int x2;
                if (k2 == -d || (k2 != d && v2[k2Offset - 1] < v2[k2Offset + 1])) {
                    x2 = v2[k2Offset + 1];
                } else {
                    x2 = v2[k2Offset - 1] + 1;
                }
                int y2 = x2 - k2;
                while (x2 < l && y2 < r && left.charAt(l - x2 - 1) == right.charAt(r - y2 - 1)) {
                    x2++;
                    y2++;
                }
                v2[k2Offset] = x2;
                if (x2 > l) {
                    k2end += 2;
                } else if (y2 > r) {
                    k2start += 2;
                } else if (!front) {
                    int k1Offset = maxD + delta - k2;
                    if (k1Offset >= 0 && k1Offset < vLength && v1[k1Offset] != -1) {
                        int x1 = v1[k1Offset];
                        int y1 = maxD + x1 - k1Offset;
                        x2 = l - x2;
                        if (x1 >= x2) {
                            return split(left, right, x1, y1);
                        }
                    }
                }
            }
        }

        LinkedList<Difference> diffs = new LinkedList<>();
        diffs.add(new Difference(DifferenceType.REMOVAL, left));
        diffs.add(new Difference(DifferenceType.INSERTION, right));
        return diffs;
    }

    protected LinkedList<Difference> split(String left, String right, int x, int y) {
        LinkedList<Difference> differences = computeDifferenceBetween(left.substring(0, x), right.substring(0, y));
                differences.addAll(computeDifferenceBetween(left.substring(x), right.substring(y)));
        return differences;
    }
}
