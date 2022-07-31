package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.patterns.Pattern;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PutPattern implements Pattern {
    @Override
    public List<RawInstruction> usePattern(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        RawInstruction removedPutInstruction = null;
        for (RawInstruction rawInstruction : rawInstructions) {
            if (rawInstruction.isPseudoInstruction()) {
                processedInstructions.add(rawInstruction);
                continue;
            }
            Instruction instruction = rawInstruction.getInstruction();
            if (instruction.getOpCode() == InstructionOpCode.PUT) {
                if (instruction.getFirstOperand().equals("rJ")) {
                    removedPutInstruction = rawInstruction;
                    log.info("Remove PUT instruction {}", rawInstruction);
                    continue;
                }
            }
            if (removedPutInstruction != null && instruction.getOpCode() == InstructionOpCode.POP) {
                processedInstructions.add(removedPutInstruction);
                removedPutInstruction = null;
            }
            processedInstructions.add(rawInstruction);
        }
        return processedInstructions;
    }
}
