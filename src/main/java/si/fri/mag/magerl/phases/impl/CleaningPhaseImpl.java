package si.fri.mag.magerl.phases.impl;

import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.Phase;

import java.util.List;

public class CleaningPhaseImpl implements Phase {

    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        return rawInstructions.stream()
                .filter(instruction -> neededInstruction(instruction.getRawInstruction()))
                .toList();
    }

    private boolean neededInstruction(String instruction) {
        return !instruction.startsWith("#");
    }
}
