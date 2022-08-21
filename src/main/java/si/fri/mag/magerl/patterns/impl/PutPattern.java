package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.utils.BranchingUtil;
import si.fri.mag.magerl.utils.CopyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static si.fri.mag.magerl.config.BranchingConfig.BRANCHING_FACTOR;
import static si.fri.mag.magerl.config.BranchingConfig.NUMBER_OF_PROGRAMS;
import static si.fri.mag.magerl.models.opcode.InstructionOpCode.POP;

@Slf4j
public class PutPattern implements Pattern {

    private final List<Integer> patternUsages = new ArrayList<>();

    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        boolean wasPatternUsed = false;
        for (RawInstruction rawInstruction : rawInstructions) {
            if (rawInstruction.isPseudoInstruction() || wasPatternUsed) {
                processedInstructions.add(rawInstruction);
                continue;
            }
            Instruction instruction = rawInstruction.getInstruction();
            if (instruction.getOpCode() == InstructionOpCode.PUT) {
                memo = new HashMap<>();
                if (instruction.getFirstOperand().equals("rJ")) {
                    if (isThereAnotherSubroutineCallInstruction(rawInstruction)) {
                        patternUsages.add(rawInstruction.getId());
                        if (optimizationDecider.test(rawInstruction.getId())) {
                            log.debug("Remove PUT instruction {}", rawInstruction);
                            wasPatternUsed = true;
                            continue;
                        }
                    }
                }
            }
            processedInstructions.add(rawInstruction);
        }
        return processedInstructions;
    }

    @Override
    public List<List<RawInstruction>> branchPattern(List<RawInstruction> rawInstructions) {
        return List.of(usePattern(rawInstructions));
    }

    /**
     * Recursively find if subroutine has another call instruction after this one: method checks all possible paths (for branches it assumes both possibilities)
     */
    private static Map<RawInstruction, Boolean> memo = new HashMap<>();
    private boolean isThereAnotherSubroutineCallInstruction(RawInstruction rawInstruction) {
        if (memo.containsKey(rawInstruction)) {
            return memo.get(rawInstruction);
        }
        if (rawInstruction.isPseudoInstruction()) {
            return false;
        }
        if (rawInstruction.getInstruction().getOpCode() instanceof InstructionOpCode && InstructionOpCode.isSubroutineInstructionOpCode((InstructionOpCode) rawInstruction.getInstruction().getOpCode())) {
            return true;
        }
        if (rawInstruction.getInstruction().getOpCode() instanceof InstructionOpCode && rawInstruction.getInstruction().getOpCode() == POP) {
            return false;
        }
        memo.put(rawInstruction, false);
        boolean isThereAnotherCall = true;
        for (RawInstruction nextInstruction : rawInstruction.getPossibleNextInstructions()) {
            isThereAnotherCall &= isThereAnotherSubroutineCallInstruction(nextInstruction);
        }
        memo.put(rawInstruction, isThereAnotherCall);
        return isThereAnotherCall;
    }
}
