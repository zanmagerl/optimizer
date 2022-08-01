package si.fri.mag.magerl.phases.impl;

import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.patterns.impl.*;
import si.fri.mag.magerl.phases.Phase;

import java.util.ArrayList;
import java.util.List;

public class PatternPhaseImpl implements Phase {

    private final List<Pattern> patterns = List.of(
            new PointlessInstructionPattern(),
            new ShiftPattern(),
            new PutPattern(),
            new CmpPattern(),
            new UnusedRegisterPattern()
    );

    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        List<RawInstruction> iter = new ArrayList<>(rawInstructions);
        for (Pattern pattern : patterns) {
            iter = pattern.usePattern(iter);
        }
        return iter;
    }
}
