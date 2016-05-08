package cz.muni.fi.scheduler.model.criteria;

@FunctionalInterface
public interface BlockWeightFunction {
    double value(long current, long delta);
}
