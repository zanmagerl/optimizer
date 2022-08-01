package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.patterns.Pattern;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ShiftPattern implements Pattern {
    @Override
    public List<RawInstruction> usePattern(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        for (int i = 0; i < rawInstructions.size() - 2; i++) {
            if (rawInstructions.get(i).isPseudoInstruction() || rawInstructions.get(i + 1).isPseudoInstruction() || rawInstructions.get(i + 2).isPseudoInstruction()) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }
            processedInstructions.add(rawInstructions.get(i));
            if (isPotentiallyUselessShiftBlock(rawInstructions.get(i + 1), rawInstructions.get(i + 2))) {
                if (InstructionOpCode.isSignedLoadInstruction((InstructionOpCode) rawInstructions.get(i).getInstruction().getOpCode())) {
                    log.info("Useless shifting: {}, {}, {}", rawInstructions.get(i), rawInstructions.get(i + 1), rawInstructions.get(i + 2));
                    i += 2;
                }
            }

        }
        // Do not forget to include last two instructions!
        processedInstructions.addAll(rawInstructions.subList(rawInstructions.size() - 2, rawInstructions.size()));
        return processedInstructions;
    }

    private boolean isPotentiallyUselessShiftBlock(RawInstruction firstInstruction, RawInstruction secondInstruction) {
        return firstInstruction.getInstruction().getOpCode() == InstructionOpCode.SLU
                && secondInstruction.getInstruction().getOpCode() == InstructionOpCode.SR
                && firstInstruction.getInstruction().getFirstOperand().equals(firstInstruction.getInstruction().getSecondOperand())
                && secondInstruction.getInstruction().getFirstOperand().equals(secondInstruction.getInstruction().getSecondOperand())
                && firstInstruction.getInstruction().getFirstOperand().equals(secondInstruction.getInstruction().getFirstOperand());
    }

    private boolean couldReplaceShiftBlockWithSet(RawInstruction firstInstruction, RawInstruction secondInstruction) {

        return false;
    }
}
