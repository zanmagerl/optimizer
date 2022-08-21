package si.fri.mag.magerl.phases.impl;

import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.Phase;
import si.fri.mag.magerl.utils.RoutineUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.NEG;
import static si.fri.mag.magerl.models.opcode.InstructionOpCode.NEGU;
import static si.fri.mag.magerl.models.opcode.PseudoOpCode.IS;

public class CleaningPhaseImpl implements Phase {

    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        return removeUnusedSubroutines(rawInstructions)
                .stream()
                .filter(instruction -> neededInstruction(instruction.getRawInstruction()))
                .map(this::correctNegationInstruction)
                .toList();
    }

    private boolean neededInstruction(String instruction) {
        return !instruction.startsWith("#");
    }

    private RawInstruction correctNegationInstruction(RawInstruction rawInstruction) {
        if (rawInstruction.isPseudoInstruction()) {
            return rawInstruction;
        }
        Instruction instruction = rawInstruction.getInstruction();
        if (instruction.getOpCode() == NEGU) {
            if (Objects.equals(instruction.getSecondOperand(), "0")) {
                rawInstruction.setInstruction(instruction.toBuilder()
                        .opCode(NEG)
                        .secondOperand(instruction.getThirdOperand())
                        .thirdOperand(null)
                        .build());
                return rawInstruction;
            }
        }
        return rawInstruction;
    }

    private List<RawInstruction> removeUnusedSubroutines(List<RawInstruction> instructions) {
        if (RoutineUtil.routineMapping.isEmpty()){
            return instructions;
        }
        List<RawInstruction> cleanedInstructions = new ArrayList<>();
        boolean isInUnusedRoutine = false;
        for (RawInstruction rawInstruction : instructions) {
            if (rawInstruction.isPseudoInstruction()) {
                cleanedInstructions.add(rawInstruction);
                isInUnusedRoutine = false;
                continue;
            }
            if (isInUnusedRoutine) continue;
            if (rawInstruction.getInstruction().hasLabel()) {
                String label = rawInstruction.getInstruction().getLabel();
                if (RoutineUtil.routineMapping.containsKey(label) || label.startsWith("L") || rawInstruction.getInstruction().getOpCode() != IS || Objects.equals(label, "Main")) {
                    cleanedInstructions.add(rawInstruction);
                } else {
                    isInUnusedRoutine = true;
                }
            } else {
                cleanedInstructions.add(rawInstruction);
            }
        }
        return cleanedInstructions;
    }

}
