package si.fri.mag.magerl.phases.impl;

import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.Phase;

import java.util.List;
import java.util.Objects;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.NEG;
import static si.fri.mag.magerl.models.opcode.InstructionOpCode.NEGU;

public class CleaningPhaseImpl implements Phase {

    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        return rawInstructions.stream()
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

}
