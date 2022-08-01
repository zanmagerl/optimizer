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
        RawInstruction lastFoundPutInstruction = null;
        for (RawInstruction rawInstruction : rawInstructions) {
            if (rawInstruction.isPseudoInstruction()) {
                processedInstructions.add(rawInstruction);
                continue;
            }
            Instruction instruction = rawInstruction.getInstruction();
            if (instruction.getOpCode() == InstructionOpCode.PUT) {
                if (instruction.getFirstOperand().equals("rJ")) {
                    if (lastFoundPutInstruction != null) {
                        log.info("Remove PUT instruction {}", lastFoundPutInstruction);
                        processedInstructions.remove(lastFoundPutInstruction);
                    }
                    lastFoundPutInstruction = rawInstruction;
                }
            }
            if (lastFoundPutInstruction != null && instruction.getOpCode() == InstructionOpCode.POP) {
                lastFoundPutInstruction = null;
            }
            processedInstructions.add(rawInstruction);
        }
        return processedInstructions;
    }
}
