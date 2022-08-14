package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.utils.BranchingUtil;
import si.fri.mag.magerl.utils.CopyUtil;

import java.util.*;
import java.util.function.Predicate;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class CmpPattern implements Pattern {

    private final List<Integer> patternUsages = new ArrayList<>();

    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> processedInstruction = new ArrayList<>();
        boolean wasPatternUsed = false;
        for (int i = 0; i < rawInstructions.size() - 1; i++) {
            if (rawInstructions.get(i).isPseudoInstruction() || wasPatternUsed) {
                processedInstruction.add(rawInstructions.get(i));
                continue;
            }
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if (instruction.getOpCode() == CMP) {
                if (Objects.equals(instruction.getFirstOperand(), instruction.getSecondOperand()) && Objects.equals(instruction.getThirdOperand(), "0")) {
                    Instruction branchInstruction = rawInstructions.get(i+1).getInstruction();
                    if (branchInstruction != null && InstructionOpCode.isBranchInstructionOpCode((InstructionOpCode) branchInstruction.getOpCode()) && Objects.equals(instruction.getFirstOperand(), branchInstruction.getFirstOperand())) {
                        if (notReadBeforeWrittenAgain(rawInstructions, i+1, branchInstruction.getFirstOperand())){
                            patternUsages.add(rawInstructions.get(i).getId());
                            if (optimizationDecider.test(rawInstructions.get(i).getId())) {
                                log.debug("Removing unneeded CMP instruction: {}, {}", rawInstructions.get(i), rawInstructions.get(i + 1));
                                processedInstruction.add(rawInstructions.get(i + 1));
                                wasPatternUsed = true;
                                i++;
                                continue;
                            }
                        }
                    }
                }
            }
            processedInstruction.add(rawInstructions.get(i));
        }
        processedInstruction.add(rawInstructions.get(rawInstructions.size()-1));

        return processedInstruction;
    }

    @Override
    public List<List<RawInstruction>> branchPattern(List<RawInstruction> rawInstructions) {
        patternUsages.clear();
        rawInstructions = usePattern(rawInstructions, x -> false);
        log.debug("Pattern {} is used in {}", this.getClass(), patternUsages);
        List<List<Integer>> combinations = BranchingUtil.getBranchingOptions(patternUsages);
        List<List<RawInstruction>> possibilities = new ArrayList<>();
        for (List<Integer> combination : combinations) {
            possibilities.add(usePattern(CopyUtil.copyRawInstructions(rawInstructions), combination::contains));
        }
        return possibilities;
    }

    private boolean notReadBeforeWrittenAgain(List<RawInstruction> rawInstructions, int index, String firstOperand) {
        boolean notRead = true;
        for (int i = index; i < rawInstructions.size(); i++) {
            if (rawInstructions.get(i).isPseudoInstruction()) {
                continue;
            }
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if (instruction.getOpCode() instanceof InstructionOpCode) {
                if (InstructionOpCode.isBranchInstructionOpCode((InstructionOpCode) instruction.getOpCode())) {
                    notRead = notReadBeforeWrittenAgain(
                            rawInstructions,
                            rawInstructions.indexOf(rawInstructions.stream().filter(rawInstruction -> {
                                if (rawInstruction.getInstruction() != null) {
                                    return Objects.equals(rawInstruction.getInstruction().getLabel(), instruction.getSecondOperand());
                                }
                                return false;
                            }).findFirst().get()),
                            firstOperand);
                }
                if (!InstructionOpCode.isStoreInstructionOpCode((InstructionOpCode) instruction.getOpCode()) && Objects.equals(firstOperand, instruction.getSecondOperand()) || Objects.equals(firstOperand, instruction.getThirdOperand())) {
                    notRead = false;
                }
            }
            if (!notRead) {
                return false;
            }
            if (instruction.getOpCode() == POP || Objects.equals(instruction.getFirstOperand(), firstOperand)) {
                break;
            }
        }
        return true;
    }
}
