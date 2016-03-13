package cz.muni.fi.scheduler.model;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Person;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.domain.EntityData;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import cz.muni.fi.scheduler.utils.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

/**
 * The Agenda records information about the current solution.
 *
 * For instance, it holds information about teacher's blocks.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class Agenda {
    private final Configuration             config;
    private final Map<Person, EntityData>   people;
    private final Map<Pair<Teacher, Integer>, List<Block>> timeBlocks;

    private int joinBlocks(List<Block> blocks) {
        if (blocks.size() == 1)
            return 1;

        Queue<Block> queue = new LinkedList<>(blocks);
        blocks.clear();

        while (!queue.isEmpty()) {
            Block current = queue.remove();

            while (!queue.isEmpty() && (current.canJoin(queue.peek()))) {
                current = current.join(queue.remove());
            }

            blocks.add(current);
        }

        return blocks.size();
    }

    public Agenda(Configuration config) {
        this.config = requireNonNull(config, "config");
        people      = new HashMap<>();
        timeBlocks  = new HashMap<>();
    }

    public EntityData getPerson(Person person) {
        requireNonNull(person, "person");
        return people.computeIfAbsent(person, k -> new EntityData(k));
    }

    public int markTimeSlot(Teacher teacher, TimeSlot slot) {
        requireNonNull(teacher, "teacher");
        requireNonNull(slot,    "slot");
        List<Block> tblocks = timeBlocks.computeIfAbsent(
                Pair.of(teacher, slot.getParent().getDay()),
                k -> new ArrayList<>(1)
        );

        tblocks.add(new Block(slot));
        return joinBlocks(tblocks) - 1;
    }

    public int unmarkTimeSlot(Teacher teacher, TimeSlot slot) {
        requireNonNull(teacher, "teacher");
        requireNonNull(slot,    "slot");

        List<Block>     blocks = timeBlocks.get(Pair.of(teacher, slot.getParent().getDay()));
        Optional<Block> qblock = blocks.stream().filter(b -> b.contains(slot)).findFirst();

        if (!qblock.isPresent())
            return 0;

        Block block = qblock.get();
        blocks.remove(block);
        List<Block> split = block.split(slot);
        blocks.addAll(split);

        return split.size() - 1;
    }

    public int analyzeTimeSlot(Teacher teacher, TimeSlot slot) {
        requireNonNull(teacher, "teacher");
        requireNonNull(slot,    "slot");

        Block proposed = new Block(slot);
        List<Block> blocks = timeBlocks.get(Pair.of(teacher, slot.getParent().getDay()));

        if (blocks == null)
            return 1;

        // at most 2 blocks from 'blocks' can join the proposed block
        int count = (int) blocks.stream().filter(b -> b.canJoin(proposed)).count();
        assert (count <= 2);

        return count - 1;
    }

    public int blockCount(Teacher teacher) {
        int count = 0;

        for (int i = 0; i < config.dates.size(); ++i) {
            List<Block> blocks = timeBlocks.get(Pair.of(teacher, i));
            if (blocks != null)
                count += blocks.size();
        }

        return count;
    }
}
