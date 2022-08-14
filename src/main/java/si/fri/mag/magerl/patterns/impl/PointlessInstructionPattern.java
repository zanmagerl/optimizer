package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.utils.BranchingUtil;
import si.fri.mag.magerl.utils.CopyUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
public class PointlessInstructionPattern implements Pattern {

    private final List<Integer> patternUsages = new ArrayList<>();

    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        boolean wasAlreadyUsed = false;
        for (RawInstruction rawInstruction : rawInstructions) {
            if (wasAlreadyUsed || rawInstruction.isPseudoInstruction()) {
                processedInstructions.add(rawInstruction);
                continue;
            }
            Instruction instruction = rawInstruction.getInstruction();
            if (uselessSetInstruction(instruction)) {
                patternUsages.add(rawInstruction.getId());
                if (optimizationDecider.test(rawInstruction.getId())) {
                    log.debug("Useless instruction: {}", instruction);
                    wasAlreadyUsed = true;
                    continue;
                }
            }
            if (uselessSWYMInstruction(instruction)){
                patternUsages.add(rawInstruction.getId());
                if (optimizationDecider.test(rawInstruction.getId())) {
                    log.debug("Useless instruction: {}", instruction);
                    wasAlreadyUsed = true;
                    continue;
                }
            }
            processedInstructions.add(rawInstruction);
        }
        return processedInstructions;
    }

    @Override
    public List<List<RawInstruction>> branchPattern(List<RawInstruction> rawInstructions) {
        patternUsages.clear();
        rawInstructions = usePattern(rawInstructions, x -> false);
        List<List<Integer>> combinations = BranchingUtil.getBranchingOptions(patternUsages);
        log.info("Combinations {}, patternUsages: {}", combinations.size(), patternUsages);
        List<List<RawInstruction>> possibilities = new ArrayList<>();
        for (List<Integer> combination : combinations) {
            possibilities.add(usePattern(CopyUtil.copyRawInstructions(rawInstructions), combination::contains));
        }
        return possibilities;
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
