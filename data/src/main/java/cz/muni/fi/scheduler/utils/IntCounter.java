package cz.muni.fi.scheduler.utils;

public class IntCounter {
    private final int start;
    private       int current;

    public IntCounter() {
        this.start = this.current = 0;
    }

    public IntCounter(int start) {
        this.start = this.current = start;
    }

    public int  get()           { return current; }
    public void set(int value)  { this.current = value; }
    public int  inc()           { return ++current; }
}
