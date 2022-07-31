package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.patterns.Pattern;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PointlessInstructionPattern implements Pattern {

    @Override
    public List<RawInstruction> usePattern(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        for (RawInstruction rawInstruction : rawInstructions) {
            if (rawInstruction.isPseudoInstruction()) {
                processedInstructions.add(rawInstruction);
                continue;
            }
            Instruction instruction = rawInstruction.getInstruction();
            if (uselessSetInstruction(instruction)) {
                log.info("Useless instruction: {}", instruction);
                continue;
            }
            if (uselessSWYMInstruction(instruction)){
                log.info("Useless instruction: {}", instruction);
                continue;
            }

            processedInstructions.add(rawInstruction);
        }
        return processedInstructions;
    }

    /**
     * Useless SET instruction, e.g. SET $0,$0
     */
    private boolean uselessSetInstruction(Instruction instruction) {
        return instruction.getOpCode() == InstructionOpCode.SET && instruction.getFirstOperand().equals(instruction.getSecondOperand());
    }

    /**
     * Useless SWYM instruction, that is basically a nop instruction
     */
    private boolean uselessSWYMInstruction(Instruction instruction) {
        return instruction.getOpCode() == InstructionOpCode.SWYM;
    }
}
