package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.utils.BranchingUtil;
import si.fri.mag.magerl.utils.CopyUtil;
import si.fri.mag.magerl.utils.RegisterUtil;
import si.fri.mag.magerl.utils.RoutineUtil;

import java.util.*;
import java.util.function.Predicate;

import static si.fri.mag.magerl.config.BranchingConfig.NUMBER_OF_PROGRAMS;
import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class PointlessInstructionPattern implements Pattern {

    public final List<Integer> patternUsages = new ArrayList<>();

    public final Map<String, Integer> patternsUsed = new HashMap<>();

    public final Map<String, Integer> patternsUsedAbs = new HashMap<>();

    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        boolean wasAlreadyUsed = false;
        for (int i = 0; i < rawInstructions.size(); i++) {
            RawInstruction rawInstruction = rawInstructions.get(i);
            if (wasAlreadyUsed || rawInstruction.isPseudoInstruction()) {
                processedInstructions.add(rawInstruction);
                continue;
            }
            Instruction instruction = rawInstruction.getInstruction();
            if (uselessSetInstruction(instruction)) {
                patternUsages.add(rawInstruction.getId());
                patternsUsed.put(rawInstruction.getSubroutine(), patternsUsed.getOrDefault(rawInstruction.getSubroutine(), 0) + 1);
                if (optimizationDecider.test(rawInstruction.getId())) {
                    log.debug("Useless instruction: {}", instruction);
                    wasAlreadyUsed = true;
                    continue;
                }
            }
            if (uselessSWYMInstruction(instruction)){
                patternUsages.add(rawInstruction.getId());
                patternsUsed.put(rawInstruction.getSubroutine(), patternsUsed.getOrDefault(rawInstruction.getSubroutine(), 0) + 1);
                if (optimizationDecider.test(rawInstruction.getId())) {
                    log.debug("Useless instruction: {}", instruction);
                    wasAlreadyUsed = true;
                    continue;
                }
            }
            if (instruction.getOpCode() == NEG && uselessAbsoluteValue(rawInstructions, rawInstruction)) {
                patternUsages.add(rawInstruction.getId());
                patternsUsedAbs.put(rawInstruction.getSubroutine(), patternsUsedAbs.getOrDefault(rawInstruction.getSubroutine(), 0) + 1);
                if (optimizationDecider.test(rawInstruction.getId())) {
                    log.debug("Useless absolute value block: {}-{}", rawInstruction, rawInstructions.get(i+1));
                    wasAlreadyUsed = true;
                    i++;
                    continue;
                }
            }

            processedInstructions.add(rawInstruction);
        }
        log.info("Pointless pattern: {}", patternsUsed.keySet().stream().filter(key -> RoutineUtil.routineMapping.containsKey(key)).map(patternsUsed::get).mapToInt(Integer::intValue).sum());
        log.info("Absolute value pattern: {}", patternsUsedAbs.keySet().stream().filter(key -> RoutineUtil.routineMapping.containsKey(key)).map(patternsUsedAbs::get).mapToInt(Integer::intValue).sum());

        return processedInstructions;
    }

    @Override
    public List<List<RawInstruction>> branchPattern(List<RawInstruction> rawInstructions) {
        return List.of(usePattern(rawInstructions));
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

    private boolean uselessAbsoluteValue(List<RawInstruction> rawInstructions, RawInstruction firstInstruction) {
        int blockStart = rawInstructions.indexOf(firstInstruction);
        if (isAbsoluteValueBlock(firstInstruction, rawInstructions.get(blockStart+1))) {
            RawInstruction writtenInstruction = findWrittenInstruction(firstInstruction.getPossiblePrecedingInstruction().get(0), firstInstruction.getInstruction().getSecondOperand());
            if (writtenInstruction.getInstruction().getOpCode() == SETL) {
                if (writtenInstruction.getInstruction().getSecondOperand().contains("#")) {
                    return Integer.parseInt(writtenInstruction.getInstruction().getSecondOperand().substring(1), 16) >= 0;
                }
                return Integer.parseInt(writtenInstruction.getInstruction().getSecondOperand()) >= 0;
            }
        }
        return false;
    }

    private boolean isAbsoluteValueBlock(RawInstruction firstInstruction, RawInstruction secondInstruction) {
        return firstInstruction.getInstruction().getOpCode() == NEG
                && secondInstruction.getInstruction().getOpCode() == CSN
                && Objects.equals(firstInstruction.getInstruction().getFirstOperand(), secondInstruction.getInstruction().getThirdOperand())
                && Objects.equals(firstInstruction.getInstruction().getSecondOperand(), secondInstruction.getInstruction().getFirstOperand())
                && Objects.equals(secondInstruction.getInstruction().getFirstOperand(), secondInstruction.getInstruction().getSecondOperand())
                && !RegisterUtil.isGlobalRegister(firstInstruction.getInstruction().getFirstOperand());
    }

    private RawInstruction findWrittenInstruction(RawInstruction iterInstruction, String register) {
        if (iterInstruction.getInstruction().getOpCode() instanceof InstructionOpCode && iterInstruction.getInstruction().isWrittenToRegister(register)) {
            return iterInstruction;
        }
        return findWrittenInstruction(iterInstruction.getPossiblePrecedingInstruction().get(0), register);
    }
}
