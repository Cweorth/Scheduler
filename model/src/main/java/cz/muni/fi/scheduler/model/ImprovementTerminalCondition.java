package cz.muni.fi.scheduler.model;

import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import org.apache.log4j.Logger;

import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.termination.TerminationCondition;
import org.cpsolver.ifs.util.DataProperties;

/**
 * Terminates the search after a number of iterations without finding a better
 * solution.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class ImprovementTerminalCondition implements TerminationCondition<Slot, Ticket> {
    private static final Logger logger = Logger.getLogger("ImprovementTerminalCondition");

    private final int   maxIterations;
    private int         counter;
    private double      bestSolution;

    public ImprovementTerminalCondition(DataProperties properties) {
        maxIterations = properties.getPropertyInt("ImprovementTerminalCondition.maxIterations", 5000);

        counter      = 0;
        bestSolution = Double.MIN_VALUE;
        logger.debug("initialized");
    }

    @Override
    public boolean canContinue(Solution<Slot, Ticket> currentSolution) {
        if (counter > maxIterations) {
            logger.debug("max iterations reached without improving, stopping");
            return false;
        }

        ++counter;

        if (currentSolution.isComplete()) {
            double value = currentSolution.getModel().getTotalValue(currentSolution.getAssignment());

            if (value < bestSolution) {
                logger.info("found the best solution (so far) with value of " + String.valueOf(value));
                bestSolution = value;
                counter = 0;
            }
        }

        return true;
    }

}
