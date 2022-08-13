package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.patterns.Pattern;

import java.util.ArrayList;
import java.util.List;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.POP;

@Slf4j
public class PutPattern implements Pattern {
    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        boolean wasPatternUsed = false;
        for (RawInstruction rawInstruction : rawInstructions) {
            if (rawInstruction.isPseudoInstruction() || wasPatternUsed) {
                processedInstructions.add(rawInstruction);
                continue;
            }
            Instruction instruction = rawInstruction.getInstruction();
            if (instruction.getOpCode() == InstructionOpCode.PUT) {
                if (instruction.getFirstOperand().equals("rJ")) {
                    if (isThereAnotherSubroutineCallInstruction(rawInstruction)) {
                        log.info("Remove PUT instruction {}", rawInstruction);
                        wasPatternUsed = true;
                        continue;
                    }
                }
            }
            processedInstructions.add(rawInstruction);
        }
        return processedInstructions;
    }

    @Override
    public List<RawInstruction> branchPattern(List<RawInstruction> rawInstructions) {
        return null;
    }

    /**
     * Recursively find if subroutine has another call instruction after this one: method checks all possible paths (for branches it assumes both possibilities)
     */
    private boolean isThereAnotherSubroutineCallInstruction(RawInstruction rawInstruction) {
        if (rawInstruction.getInstruction().getOpCode() == POP) {
            return false;
        }
        if (rawInstruction.getInstruction().getOpCode() instanceof InstructionOpCode && InstructionOpCode.isSubroutineInstructionOpCode((InstructionOpCode) rawInstruction.getInstruction().getOpCode())) {
            return true;
        }

        boolean isThereAnotherCall = true;
        log.info("{}: {}", rawInstruction.getRawInstruction(), rawInstruction.getPossibleNextInstructions());
        for (RawInstruction nextInstruction : rawInstruction.getPossibleNextInstructions()) {
            isThereAnotherCall &= isThereAnotherSubroutineCallInstruction(nextInstruction);
        }
        return isThereAnotherCall;
    }
}
