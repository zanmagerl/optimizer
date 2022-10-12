package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.utils.BranchingUtil;
import si.fri.mag.magerl.utils.CopyUtil;
import si.fri.mag.magerl.utils.RoutineUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static si.fri.mag.magerl.config.BranchingConfig.BRANCHING_FACTOR;
import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class SwapPattern implements Pattern {

    public final List<Integer> patternUsages = new ArrayList<>();
    public final Map<String, Integer> patternsUsed = new HashMap<>();

    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> processedInstructions = new ArrayList<>(rawInstructions);
        processedInstructions = removeSwappingInstructions(processedInstructions, optimizationDecider);
        return processedInstructions;
    }

    @Override
    public List<List<RawInstruction>> branchPattern(List<RawInstruction> rawInstructions) {
        patternUsages.clear();
        rawInstructions = usePattern(rawInstructions, x -> false);
        log.debug("Pattern {} is used in {}", this.getClass(), patternUsages);
        List<List<Integer>> combinations = BranchingUtil.sampleBranchingOptions(patternUsages, BRANCHING_FACTOR);
        List<List<RawInstruction>> possibilities = new ArrayList<>();
        for (List<Integer> combination : combinations) {
            possibilities.add(usePattern(CopyUtil.copyRawInstructions(rawInstructions), combination::contains));
        }
        return possibilities;
    }

    private List<RawInstruction> removeSwappingInstructions(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        boolean wasPatternUsed = false;
        for (int i = 0; i < rawInstructions.size(); i++) {
            if (rawInstructions.get(i).isPseudoInstruction() || wasPatternUsed) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if (instruction.getOpCode() == SET) {
                if (rawInstructions.get(i+1).getUnusedRegisters().contains(instruction.getSecondOperand()) && !instruction.containsGlobalRegisters() && isSwapBackAndUnused(rawInstructions, i+1, instruction)) {

                    if (!isCallInstructionNear(rawInstructions, i)) {
                        patternUsages.add(rawInstructions.get(i).getId());
                        if (optimizationDecider.test(rawInstructions.get(i).getId())) {
                            log.debug("{}: Remove swapping instruction: {}", rawInstructions.get(i).getSubroutine(), rawInstructions.get(i).getRawInstruction());
                            log.debug("Unused registers: {}", rawInstructions.get(i + 1).getUnusedRegisters());
                            rawInstructions.get(i + 1).getInstruction().changeOperand(instruction.getFirstOperand(), instruction.getSecondOperand());
                            patternsUsed.put(rawInstructions.get(i).getSubroutine(), patternsUsed.getOrDefault(rawInstructions.get(i).getSubroutine(), 0) + 1);

                            String substituteRegister = null;
                            int j;
                            for (j = i + 2; !rawInstructions.get(j).isPseudoInstruction() && !rawInstructions.get(j).getInstruction().isTwinSwapInstruction(instruction); j++) {
                                if (rawInstructions.get(j).getInstruction().usesRegister(instruction.getFirstOperand())) {
                                    log.debug("Modify instruction: {} {} -> {}", rawInstructions.get(j).getRawInstruction(), instruction.getFirstOperand(), instruction.getSecondOperand());
                                    rawInstructions.get(j).getInstruction().changeOperand(instruction.getFirstOperand(), instruction.getSecondOperand());
                                }
                                if (rawInstructions.get(j).getInstruction().usesRegister(instruction.getSecondOperand())) {
                                    log.debug("Modify instruction: {} {} -> {}", rawInstructions.get(j).getRawInstruction(), instruction.getFirstOperand(),
                                            instruction.getSecondOperand());
                                    if (substituteRegister == null) {
                                        substituteRegister = rawInstructions.get(j).getUnusedRegisters().get(rawInstructions.get(j).getUnusedRegisters().size() - 1);
                                    }
                                    rawInstructions.get(j).getInstruction().changeOperand(instruction.getSecondOperand(), substituteRegister);
                                }
                            }
                            rawInstructions.remove(rawInstructions.get(j));
                            wasPatternUsed = true;
                            continue;
                        }
                    }
                }
            }
            processedInstructions.add(rawInstructions.get(i));
        }
        log.info("Swap pattern: {}", patternsUsed.keySet().stream().filter(key -> RoutineUtil.routineMapping.containsKey(key)).map(patternsUsed::get).mapToInt(Integer::intValue).sum());

        return processedInstructions;
    }

    private boolean isSwapBackAndUnused(List<RawInstruction> rawInstructions, Integer index, Instruction firstInstruction) {
        for (int j = index ; !rawInstructions.get(j).isPseudoInstruction(); j++) {
            Instruction instruction = rawInstructions.get(j).getInstruction();
            if (instruction.hasLabel()) {
                return false;
            }
            if (instruction.getOpCode() instanceof PseudoOpCode) continue;

            if (instruction.isTwinSwapInstruction(firstInstruction)) {
                if (j == index) return false;
                return true;
            }
            if (instruction.isWrittenToRegister(firstInstruction.getFirstOperand())) {
                return false;
            }
        }
        return false;
    }

    private boolean isCallInstructionNear(List<RawInstruction> rawInstructions, int index) {
        for (int i = index; i < index + 10; i++) {
            if (rawInstructions.get(i).getInstruction() == null) {
                break;
            }
            if (rawInstructions.get(i).getInstruction().isSubroutineCall()) {
                return true;
            }
        }
        return false;
    }
}
