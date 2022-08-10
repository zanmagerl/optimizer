package si.fri.mag.magerl.phases.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.Phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.SET;

@Slf4j
public class SwapPhaseImpl implements Phase {
    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        for (int i = 0; i < rawInstructions.size() - 1; i++) {
            if (rawInstructions.get(i).isPseudoInstruction()) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }
            // Two consecutive SET instructions
            if (shouldSafeSwapThem(rawInstructions.get(i).getInstruction(), rawInstructions.get(i+1).getInstruction())) {
                processedInstructions.add(rawInstructions.get(i+1));
                processedInstructions.add(rawInstructions.get(i));
                i++;
            } else {
                processedInstructions.add(rawInstructions.get(i));
            }
        }
        processedInstructions.add(rawInstructions.get(rawInstructions.size()-1));
        return processedInstructions;
    }

    private boolean shouldSafeSwapThem(Instruction firstInstruction, Instruction secondInstruction) {
        if (firstInstruction.getOpCode() == SET && secondInstruction.getOpCode() == SET) {
            return !Objects.equals(firstInstruction.getFirstOperand(), secondInstruction.getSecondOperand()) && !Objects.equals(secondInstruction.getFirstOperand(), firstInstruction.getSecondOperand());
        }
        return false;
    }
}
