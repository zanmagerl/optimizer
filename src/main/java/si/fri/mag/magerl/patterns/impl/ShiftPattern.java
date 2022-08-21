package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.utils.BranchingUtil;
import si.fri.mag.magerl.utils.CopyUtil;
import si.fri.mag.magerl.utils.RegisterUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static si.fri.mag.magerl.config.BranchingConfig.BRANCHING_FACTOR;
import static si.fri.mag.magerl.config.BranchingConfig.NUMBER_OF_PROGRAMS;

@Slf4j
public class ShiftPattern implements Pattern {

    private final List<Integer> patternUsages = new ArrayList<>();

    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        boolean wasPatternUsed = false;
        for (int i = 0; i < rawInstructions.size() - 2; i++) {
            if (wasPatternUsed || rawInstructions.get(i).isPseudoInstruction() || rawInstructions.get(i + 1).isPseudoInstruction() || rawInstructions.get(i + 2).isPseudoInstruction() || rawInstructions.get(i).getInstruction().getOpCode() instanceof PseudoOpCode) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }
            processedInstructions.add(rawInstructions.get(i));
            if (isPotentiallyUselessShiftBlock(rawInstructions.get(i + 1), rawInstructions.get(i + 2))) {
                if (wasRegisterLoadedBeforeShifting(rawInstructions.get(i), rawInstructions.get(i+1).getInstruction().getFirstOperand())) {
                    patternUsages.add(rawInstructions.get(i).getId());
                    if (optimizationDecider.test(rawInstructions.get(i).getId())) {
                        log.debug("Useless shifting: {}, {}, {}", rawInstructions.get(i), rawInstructions.get(i + 1), rawInstructions.get(i + 2));
                        i += 2;
                        wasPatternUsed = true;
                        continue;
                    }
                }
            }

            if (isPotentiallyUnusedRegisterShiftBlock(rawInstructions.get(i), rawInstructions.get(i + 1), rawInstructions.get(i + 2))) {
                patternUsages.add(rawInstructions.get(i).getId());
                if (optimizationDecider.test(rawInstructions.get(i).getId())) {
                    log.debug("Useless shifting with unused registers: {}, {}, {}", rawInstructions.get(i), rawInstructions.get(i + 1), rawInstructions.get(i + 2));
                    rawInstructions.get(i).setInstruction(rawInstructions.get(i).getInstruction().toBuilder()
                            .firstOperand(rawInstructions.get(i + 1).getInstruction().getFirstOperand())
                            .build());
                    i += 2;
                    wasPatternUsed = true;
                }
            }

        }
        // Do not forget to include last two instructions!
        processedInstructions.addAll(rawInstructions.subList(rawInstructions.size() - 2, rawInstructions.size()));
        return processedInstructions;
    }

    @Override
    public List<List<RawInstruction>> branchPattern(List<RawInstruction> rawInstructions) {
        patternUsages.clear();
        rawInstructions = usePattern(rawInstructions, SCOUT);
        log.debug("Pattern {} is used in {}", this.getClass(), patternUsages);
        List<List<Integer>> combinations = BranchingUtil.sampleBranchingOptions(patternUsages, BRANCHING_FACTOR);
        List<List<RawInstruction>> possibilities = new ArrayList<>();
        for (List<Integer> combination : combinations) {
            possibilities.add(usePattern(CopyUtil.copyRawInstructions(rawInstructions), combination::contains));
        }
        return possibilities;
    }

    private boolean isPotentiallyUselessShiftBlock(RawInstruction firstInstruction, RawInstruction secondInstruction) {
        return firstInstruction.getInstruction().getOpCode() == InstructionOpCode.SLU
                && secondInstruction.getInstruction().getOpCode() == InstructionOpCode.SR
                && firstInstruction.getInstruction().getFirstOperand().equals(firstInstruction.getInstruction().getSecondOperand())
                && secondInstruction.getInstruction().getFirstOperand().equals(secondInstruction.getInstruction().getSecondOperand())
                && firstInstruction.getInstruction().getFirstOperand().equals(secondInstruction.getInstruction().getFirstOperand())
                && Objects.equals(firstInstruction.getInstruction().getThirdOperand(), secondInstruction.getInstruction().getThirdOperand());

    }

    /**
     *  This pattern substitutes this block of code
     * 	LDT $0,$0,0
     * 	SLU $2,$0,32
     * 	SR $2,$2,32
     * 	with
     *  LDT $2,$0,0
     */
    private boolean isPotentiallyUnusedRegisterShiftBlock(RawInstruction zerothInstruction, RawInstruction firstInstruction, RawInstruction secondInstruction) {
        return InstructionOpCode.isSignedLoadInstructionOpCode((InstructionOpCode) zerothInstruction.getInstruction().getOpCode())
                && Objects.equals(zerothInstruction.getInstruction().getFirstOperand(), firstInstruction.getInstruction().getSecondOperand())
                && firstInstruction.getInstruction().getOpCode() == InstructionOpCode.SLU
                && secondInstruction.getInstruction().getOpCode() == InstructionOpCode.SR
                && Objects.equals(firstInstruction.getInstruction().getFirstOperand(), secondInstruction.getInstruction().getFirstOperand())
                && Objects.equals(secondInstruction.getInstruction().getFirstOperand(), secondInstruction.getInstruction().getSecondOperand())
                && secondInstruction.getUnusedRegisters().contains(firstInstruction.getInstruction().getSecondOperand())
                && Objects.equals(firstInstruction.getInstruction().getThirdOperand(), secondInstruction.getInstruction().getThirdOperand());
    }

    public boolean wasRegisterLoadedBeforeShifting(RawInstruction rawInstruction, String register) {
        if (rawInstruction.isPseudoInstruction()) {
            return false;
        }
        if (rawInstruction.getInstruction().getOpCode() instanceof PseudoOpCode) {
            boolean wasLoaded = true;
            for (RawInstruction instruction : rawInstruction.getPossiblePrecedingInstruction()) {
                wasLoaded &= wasRegisterLoadedBeforeShifting(instruction, register);
            }
            return wasLoaded;
        }
        if (InstructionOpCode.isSignedLoadInstructionOpCode((InstructionOpCode) rawInstruction.getInstruction().getOpCode()) && Objects.equals(rawInstruction.getInstruction().getFirstOperand(), register)) {
            return true;
        }
        if (rawInstruction.getInstruction().isWrittenToRegister(register)) {
            return false;
        }
        boolean wasLoaded = true;
        for (RawInstruction instruction : rawInstruction.getPossiblePrecedingInstruction()) {
            wasLoaded &= wasRegisterLoadedBeforeShifting(instruction, register);
        }
        return wasLoaded;
    }

}
