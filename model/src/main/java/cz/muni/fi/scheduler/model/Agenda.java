package cz.muni.fi.scheduler.model;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Person;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.domain.EntityData;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.MemberSlot;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import cz.muni.fi.scheduler.utils.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Agenda records information about the current solution.
 *
 * For instance, it holds information about teacher's blocks.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class Agenda {
    private final Map<Person, EntityData>                     people;
    private final Map<Pair<Teacher, Integer>, List<Block>>    timeBlocks;
    private final Map<Pair<Teacher, Integer>, List<EntryRow>> memberBlocks;

    private final Map<Teacher, Long>         memBlockCounts;

    private int joinBlocks(List<Block> blocks) {
        if (blocks.size() == 1)
            return 1;

        Queue<Block> queue = new PriorityQueue<>(blocks);
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

    public Agenda() {
        people       = new HashMap<>();
        timeBlocks   = new HashMap<>();
        memberBlocks = new HashMap<>();

        memBlockCounts = new HashMap<>();
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
                k -> new ArrayList<>()
        );

        int oldsize = tblocks.size();
        tblocks.add(new Block(slot));

        memBlockCounts.remove(teacher);

        int diff = joinBlocks(tblocks) - oldsize;
        return memberBlocks.containsKey(Pair.of(teacher, slot.getParent().getDay())) ? 0 : diff;
    }

    public int markMemberSlot(Teacher teacher, MemberSlot slot) {
        requireNonNull(teacher, "teacher");
        requireNonNull(slot,    "slot");

        int day  = slot.getParent().getDay();
        int diff = analyzeMemberSlotAssign(teacher, slot);

        List<EntryRow> rows = memberBlocks.computeIfAbsent(Pair.of(teacher, day), t -> new ArrayList<>());

        rows.add(slot.getParent());
        memBlockCounts.remove(teacher);
        return diff;
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

        memBlockCounts.remove(teacher);

        int diff = split.size() - 1;
        return memberBlocks.containsKey(Pair.of(teacher, slot.getParent().getDay())) ? 0 : diff;
    }

    public int unmarkMemberSlot(Teacher teacher, MemberSlot slot) {
        requireNonNull(teacher, "teacher");
        requireNonNull(slot,    "slot");

        int day = slot.getParent().getDay();
        List<EntryRow> rows = memberBlocks.get(Pair.of(teacher, day));

        if (!rows.remove(slot.getParent()))
            return 0;

        memBlockCounts.remove(teacher);
        return rows.isEmpty()
                ? timeBlocks.getOrDefault(Pair.of(teacher, day), Arrays.asList()).size() - 1
                : 0;
    }

    public int analyzeTimeSlotAssign(Teacher teacher, TimeSlot slot) {
        requireNonNull(teacher, "teacher");
        requireNonNull(slot,    "slot");

        Block proposed = new Block(slot);
        List<Block> blocks = timeBlocks.get(Pair.of(teacher, slot.getParent().getDay()));

        // check that the block is not covered by member block
        List<EntryRow> rows = memberBlocks.get(Pair.of(teacher, slot.getParent().getDay()));

        if ((rows != null) && !rows.isEmpty())
            return 0;

        // there are no blocks assigned in the given entry row
        if (blocks == null)
            return 1;

        // at most 2 blocks from 'blocks' can join the proposed block
        int count = (int) blocks.stream().filter(b -> b.canJoin(proposed)).count();
        assert (count <= 2);

        int diff = 1 - count;
        return memberBlocks.containsKey(Pair.of(teacher, slot.getParent().getDay())) ? 0 : diff;
    }

    public int analyzeMemberSlotAssign(Teacher teacher, MemberSlot slot) {
        requireNonNull(teacher, "teacher");
        requireNonNull(slot,    "slot");

        int day  = slot.getParent().getDay();

        List<EntryRow> rows    = memberBlocks.computeIfAbsent(Pair.of(teacher,day), t -> new ArrayList<>());
        List<Block>    tblocks = timeBlocks.get(Pair.of(teacher, day));

        if (rows.isEmpty() && (tblocks != null))
            return 1 - tblocks.size();

        return rows.isEmpty() ? 1 : 0;
    }

    public int analyzeTimeSlotUnassign(Teacher teacher, TimeSlot slot) {
        requireNonNull(teacher, "teacher");
        requireNonNull(slot,    "slot");

        List<Block> blocks = timeBlocks.get(Pair.of(teacher, slot.getParent().getDay()));

        if (blocks == null)
            return 0;

        Block splitting = blocks.stream().filter(b -> b.contains(slot)).findAny().orElse(null);

        if (splitting == null)
            return 0;

        int diff = splitting.splitFactor(slot) - 1;
        return memberBlocks.containsKey(Pair.of(teacher, slot.getParent().getDay())) ? 0 : diff;
    }

    public int analyzeMemberSlotUnassign(Teacher teacher, MemberSlot slot) {
        requireNonNull(teacher, "teacher");
        requireNonNull(slot,    "slot");

        int day = slot.getParent().getDay();
        List<EntryRow> rows = memberBlocks.get(Pair.of(teacher, day));

        if (!rows.contains(slot.getParent()))
            return 0;

        memBlockCounts.remove(teacher);
        return rows.size() > 1
                ? 0
                  // if there are other blocks covering this one, nothing will change
                : timeBlocks.getOrDefault(Pair.of(teacher, day), Arrays.asList()).size() - 1;
                  // else count how many blocks will appear and remove 1 (we are computing the _difference_)
    }

    public Map<Integer, List<Block>> getBlocks(Teacher teacher) {
        Map<Integer, List<Block>> blocks = new HashMap<>();

        memberBlocks.entrySet().stream()
                .filter(entry -> entry.getKey().first().equals(teacher) && !entry.getValue().isEmpty())
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().stream().map(EntryRow::asBlock).collect(Collectors.toList())))
                .forEach(p -> blocks.put(p.first().second(), p.second()));

        timeBlocks.entrySet().stream()
                .filter(entry -> entry.getKey().first().equals(teacher))
                .filter(entry -> memberBlocks.getOrDefault(entry.getKey(), Arrays.asList()).isEmpty())
                .map(entry -> Pair.of(entry.getKey().second(), entry.getValue()))
                .forEach(p -> blocks.put(p.first(), p.second()));

        return blocks;
    }

    public long blockCount(Teacher teacher) {
        return memBlockCounts.computeIfAbsent(teacher, t -> {
            long daysCovered = memberBlocks.entrySet().stream()
                    .filter(entry -> entry.getKey().first().equals(teacher))
                    .filter(entry -> !entry.getValue().isEmpty())
                    .mapToInt(entry -> entry.getKey().second())
                    .distinct()
                    .count();

            long nonCoveredBlocks = timeBlocks.entrySet().stream()
                    .filter(entry -> entry.getKey().first().equals(teacher))
                    .filter(entry -> memberBlocks.getOrDefault(entry.getKey(), Arrays.asList()).isEmpty())
                    .mapToLong(entry -> entry.getValue().size())
                    .sum();

            return daysCovered + nonCoveredBlocks;
        });
    }

    @Deprecated
    public int blockSum() {
        Set<Teacher> collect = timeBlocks.keySet().stream()
                .map(entry -> entry.first())
                .collect(Collectors.toSet());

        memberBlocks.keySet().stream()
                .map(entry -> entry.first())
                .collect(Collectors.toCollection(() -> collect));

        return collect.stream()
                .mapToInt(t -> (int) blockCount(t))
                .sum();
    }
}
