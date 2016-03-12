package cz.muni.fi.scheduler.data;

public enum Repetition {
    NOTHING   (0),
    ORAL_EXAM (1),
    DEFENCE   (2),
    BOTH      (3);

    private final int value;

    private Repetition(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
