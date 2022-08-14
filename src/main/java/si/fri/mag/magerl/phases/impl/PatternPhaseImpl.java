package si.fri.mag.magerl.phases.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.metrics.Metric;
import si.fri.mag.magerl.metrics.impl.AbsoluteMetric;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.patterns.impl.*;
import si.fri.mag.magerl.phases.Phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class PatternPhaseImpl implements Phase {

    private final List<Pattern> patterns = List.of(
            new PointlessInstructionPattern(),
            new ShiftPattern(),
            new PutPattern(),
            new CmpPattern(),
            new SwapPattern(),
            new UnusedRegisterPattern()
    );

    private final Metric metric = new AbsoluteMetric();

    private static final boolean BRANCHING = true;

    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        if (BRANCHING) {
            List<RawInstruction> iter = new ArrayList<>(rawInstructions);
            List<List<RawInstruction>> currentPossiblePrograms = new ArrayList<>();
            currentPossiblePrograms.add(iter);
            for (Pattern pattern : patterns) {
                List<List<RawInstruction>> processedPrograms = new ArrayList<>();
                int counter = 0;
                for (List<RawInstruction> currentPossibleProgram : currentPossiblePrograms) {
                    log.info("{}", counter++);
                    List<List<RawInstruction>> procc = pattern.branchPattern(currentPossibleProgram);
                    processedPrograms.addAll(procc);
                }
                currentPossiblePrograms = processedPrograms;
                log.info("After pattern {} we have {} number of programs", pattern.getClass(), currentPossiblePrograms.size());
            }
            return currentPossiblePrograms.stream().reduce(Collections.emptyList(), (a, b) -> metric.value(a) < metric.value(b) ? a : b);
        } else {
            List<RawInstruction> iter = new ArrayList<>(rawInstructions);
            for (Pattern pattern : patterns) {
                List<RawInstruction> procc = pattern.usePattern(iter);
                iter = procc;
            }
            return iter;
        }
    }
}
