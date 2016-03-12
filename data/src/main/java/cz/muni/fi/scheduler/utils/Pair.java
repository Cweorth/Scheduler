package cz.muni.fi.scheduler.utils;

import java.util.Objects;

public class Pair<U,V> {
    private final U u;
    private final V v;

    public Pair(U first, V second) {
        this.u = first;
        this.v = second;
    }

    public static <U,V> Pair<U,V> of(U first, V second) {
        return new Pair<>(first, second);
    }

    //<editor-fold defaultstate="collapsed" desc="[  Getters  ]">

    public U first()  { return u; }
    public V second() { return v; }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="[  HashCode, Equals & ToString  ]">

    @Override
    public int hashCode() {
        return Objects.hash(u, v);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Pair) || (obj.hashCode() != hashCode()))
            return false;

        final Pair<?,?> other = (Pair<?,?>) obj;

        return Objects.equals(u, other.u)
            && Objects.equals(v, other.v);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", u, v);
    }

    //</editor-fold>

}
