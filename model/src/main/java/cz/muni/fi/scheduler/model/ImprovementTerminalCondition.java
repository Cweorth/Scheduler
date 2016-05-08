package cz.muni.fi.scheduler.model;

import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import java.time.LocalTime;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;

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
    private double      bestIncomplete;
    private LocalTime   lastPrint;

    public ImprovementTerminalCondition(DataProperties properties) {
        maxIterations = properties.getPropertyInt("ImprovementTerminalCondition.maxIterations", 5000);

        counter        = 0;
        bestSolution   = Double.MAX_VALUE;
        bestIncomplete = Double.MAX_VALUE;
        lastPrint      = LocalTime.now();
        logger.debug("initialized");
    }

    @Override
    public boolean canContinue(Solution<Slot, Ticket> currentSolution) {
        if ((counter > maxIterations) && (Double.compare(bestSolution, Double.MAX_VALUE) < 0)) {
            logger.info("max iterations reached without improving, stopping");
            return false;
        }

        ++counter;

        double value = currentSolution.getModel().getTotalValue(currentSolution.getAssignment());

        if (value < bestIncomplete) {
            bestIncomplete = value;
        }

        if (currentSolution.isComplete() && (value < bestSolution)) {
            bestSolution = value;
            counter = 0;
        }

        if (bestSolution < 0.0) {
            logger.error("bestSolution is negative!");
        }

        LocalTime now = LocalTime.now();
        if (lastPrint.plusSeconds(1).isBefore(now)) {
            logger.info(String.format(
                    "current %6.2f best %6.2f assigned %3d iteration %d",
                    Double.min(value, 999.0),         // another hack :D
                    Double.min(bestSolution, 999.0),
                    currentSolution.getAssignment().nrAssignedVariables(),
                    counter
                ));
            lastPrint = now;
        }

        //if (value < 100.0)
        //    throw new RuntimeException("Current value is negative.");

        return (bestSolution >= 0.0); // temporary hack :(
    }

}
