package cz.muni.fi.scheduler.model.neighbourhood;

import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.heuristics.NeighbourSelection;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;

@Deprecated
public class DummySwap implements NeighbourSelection<Slot, Ticket> {
    private static final Logger logger = Logger.getLogger("DummySwap");

    public DummySwap(DataProperties properties)
    { }

    @Override
    public void init(Solver<Slot, Ticket> solver) {

    }

    @Override
    public Neighbour<Slot, Ticket> selectNeighbour(Solution<Slot, Ticket> solution) {
        logger.debug("called");

        return null;
    }

}
