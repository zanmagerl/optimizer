package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.phases.impl.GraphConstructionPhaseImpl;
import si.fri.mag.magerl.phases.impl.RegisterUsagesPhaseImpl;
import si.fri.mag.magerl.phases.impl.SubroutineLabelingPhaseImpl;
import si.fri.mag.magerl.utils.RegisterUtil;
import si.fri.mag.magerl.utils.RoutineUtil;

import java.util.*;
import java.util.function.Predicate;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class UnusedStoreInstructionPattern implements Pattern {

    public final Map<String, Integer> patternsUsed = new HashMap<>();

    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        //if (1==1) return rawInstructions;
        rawInstructions = new SubroutineLabelingPhaseImpl().visit(rawInstructions);
        rawInstructions = new RegisterUsagesPhaseImpl().visit(rawInstructions);
        rawInstructions = new GraphConstructionPhaseImpl().visit(rawInstructions);
        if (cannotBeUsed(rawInstructions)) return rawInstructions;



        for (String routine : RoutineUtil.routineMapping.keySet()) {
            patternsUsed.put(routine, patternsUsed.getOrDefault(routine, 0) + 1);
            offsetToRegisterMapping.clear();
            int numberOfNeededRegisters = -1;
            String pushXRegister = "$0";
            String oldMax = "$0";
            for (int step = 0; step < 2; step++) {

                int i = rawInstructions.indexOf(RoutineUtil.routineMapping.get(routine));
                for (; !rawInstructions.get(i).isPseudoInstruction(); i++) {

                    RawInstruction currentInstruction = rawInstructions.get(i);
                    // We need to reset stack pointer mappings to global registers
                    if (Objects.equals(RoutineUtil.routineMapping.get(currentInstruction.getSubroutine()), currentInstruction)) {
                        numberOfNeededRegisters = findNumberOfNeededRegisters(rawInstructions, currentInstruction.getSubroutine());
                        pushXRegister = findPushXRegister(rawInstructions, currentInstruction.getSubroutine());
                        if (step == 0) oldMax = pushXRegister;
                    }
                    if (numberOfNeededRegisters == -1) continue;
                    if (step == 1 && currentInstruction.getInstruction().getOpCode() == SUBU && Objects.equals(currentInstruction.getInstruction().getSecondOperand(), "$253")) {

                        RawInstruction saveInstruction = extractStoreBlock(rawInstructions, i);
                        if (saveInstruction != null) {
                            String offset = currentInstruction.getInstruction().getThirdOperand();
                            List<RawInstruction> extractedLoadBlock = extractLoadBlock(saveInstruction, rawInstructions, offset, new ArrayList<>(), currentInstruction);
                            rawInstructions.remove(currentInstruction);
                            String substituteRegister;
                            if (offsetToRegisterMapping.containsKey(offset)) {
                                substituteRegister = offsetToRegisterMapping.get(offset);
                            } else {
                                substituteRegister = "$" + (offsetToRegisterMapping.isEmpty() ? (RegisterUtil.extractRegister(oldMax) + 1) : offsetToRegisterMapping.values().stream().map(RegisterUtil::extractRegister).toList().stream().max(Integer::compareTo).get() + 1);
                                offsetToRegisterMapping.put(offset, substituteRegister);
                            }
                            RawInstruction substituteInstruction = useRegisterToRegisterInsteadOfStack(substituteRegister, saveInstruction.getInstruction().getFirstOperand());
                            rawInstructions.add(rawInstructions.indexOf(saveInstruction), substituteInstruction);
                            log.debug("Combine {} {} -> to {}", currentInstruction.getRawInstruction(), saveInstruction.getRawInstruction(), substituteInstruction.getRawInstruction());
                            rawInstructions.remove(saveInstruction);
                            for (int n = 0; n < extractedLoadBlock.size(); n++) {
                                if (extractedLoadBlock.get(n).getInstruction().getOpCode() == SUBU) {
                                    rawInstructions.remove(extractedLoadBlock.get(n));
                                    log.debug("Remove instruction {}", extractedLoadBlock.get(n).getRawInstruction());
                                } else {
                                    log.debug("{}", extractedLoadBlock.get(n).printInstructionInfo());
                                    int blockStart = rawInstructions.indexOf(extractedLoadBlock.get(n));
                                    rawInstructions.remove(rawInstructions.get(blockStart));
                                    RawInstruction loadRegister = useRegisterToRegisterInsteadOfStack(extractedLoadBlock.get(n).getInstruction().getFirstOperand(), substituteRegister);
                                    rawInstructions.add(blockStart, loadRegister);
                                    log.debug("Change instruction {} -> {}", extractedLoadBlock.get(n).getRawInstruction(), rawInstructions.get(blockStart).getRawInstruction());
                                }
                            }
                            log.debug("\n");
                            rawInstructions = new GraphConstructionPhaseImpl().visit(rawInstructions);

                        }
                    } else if (step == 0 && currentInstruction.getInstruction().getOpCode() instanceof InstructionOpCode) {
                        if (RegisterUtil.isFirstLocalRegisterBiggerThanSecond(currentInstruction.getInstruction().getFirstOperand(), pushXRegister)) {
                            currentInstruction.getInstruction().setFirstOperand(increaseRegisterByValue(currentInstruction.getInstruction().getFirstOperand(), numberOfNeededRegisters));
                        }
                        if (RegisterUtil.isFirstLocalRegisterBiggerThanSecond(currentInstruction.getInstruction().getSecondOperand(), pushXRegister)) {
                            currentInstruction.getInstruction().setSecondOperand(increaseRegisterByValue(currentInstruction.getInstruction().getSecondOperand(), numberOfNeededRegisters));
                        }
                        if (RegisterUtil.isFirstLocalRegisterBiggerThanSecond(currentInstruction.getInstruction().getThirdOperand(), pushXRegister)) {
                            currentInstruction.getInstruction().setThirdOperand(increaseRegisterByValue(currentInstruction.getInstruction().getThirdOperand(), numberOfNeededRegisters));
                        }
                        if (InstructionOpCode.isSubroutineInstructionOpCode((InstructionOpCode) currentInstruction.getInstruction().getOpCode())) {
                            currentInstruction.getInstruction().setFirstOperand(increaseRegisterByValue(currentInstruction.getInstruction().getFirstOperand(), numberOfNeededRegisters));
                        }
                    }
                }
            }
        }
        List<RawInstruction> proccessedInstruction = new ArrayList<>();
        for (RawInstruction rawInstruction : rawInstructions) {
            if (rawInstruction.isPseudoInstruction()) {
                proccessedInstruction.add(rawInstruction);
                continue;
            }
            // WATCH OUT
            if (rawInstruction.getInstruction().usesRegister("$253") || rawInstruction.getInstruction().usesRegister("$254")) {
                continue;
            }
            proccessedInstruction.add(rawInstruction);
        }
        log.info("Stack pointers pattern: {}", patternsUsed.keySet().stream().filter(key -> RoutineUtil.routineMapping.containsKey(key)).map(patternsUsed::get).mapToInt(Integer::intValue).sum());

        return proccessedInstruction;
    }

    private String increaseRegisterByValue(String register, int value) {
        int originalValue = RegisterUtil.extractRegister(register);
        return "$" + (originalValue + value);
    }

    private int findNumberOfNeededRegisters(List<RawInstruction> rawInstructions, String routine) {
        RawInstruction startInstruction = RoutineUtil.routineMapping.get(routine);
        Set<String> offsets = new HashSet<>();
        for (int i = rawInstructions.indexOf(startInstruction); !rawInstructions.get(i).isPseudoInstruction() ; i++) {
            RawInstruction currentInstruction = rawInstructions.get(i);
            if (currentInstruction.getInstruction().getOpCode() == SUBU && Objects.equals(currentInstruction.getInstruction().getSecondOperand(), "$253")) {
                offsets.add(currentInstruction.getInstruction().getThirdOperand());
            }
        }
        return offsets.size()+1;
    }

    private String findPushXRegister(List<RawInstruction> rawInstructions, String routine) {
        RawInstruction startInstruction = RoutineUtil.routineMapping.get(routine);
        for (int i = rawInstructions.indexOf(startInstruction); i >= 0 && !rawInstructions.get(i).isPseudoInstruction() ; i++) {
            if (rawInstructions.get(i).getInstruction().getOpCode() instanceof InstructionOpCode && InstructionOpCode.isSubroutineInstructionOpCode((InstructionOpCode) rawInstructions.get(i).getInstruction().getOpCode())) {
                return rawInstructions.get(i).getInstruction().getFirstOperand();
            }
        }
        return "$0";
    }

    private RawInstruction useRegisterToRegisterInsteadOfStack(String destinationRegister, String sourceRegister) {
        return RawInstruction.builder()
                .instruction(Instruction.builder()
                        .opCode(SET)
                        .firstOperand(destinationRegister)
                        .secondOperand(sourceRegister)
                        .build())
                .build();
    }

    @Override
    public List<List<RawInstruction>> branchPattern(List<RawInstruction> rawInstructions) {
        return null;
    }

    private RawInstruction extractStoreBlock(List<RawInstruction> rawInstructions, Integer blockStart) {
        for (int j = blockStart + 1; !rawInstructions.get(j).isPseudoInstruction(); j++) {
            if (rawInstructions.get(j).getInstruction().getOpCode() instanceof  InstructionOpCode && InstructionOpCode.isStoreInstructionOpCode((InstructionOpCode) rawInstructions.get(j).getInstruction().getOpCode())) {
                if (Objects.equals(rawInstructions.get(j).getInstruction().getSecondOperand(), rawInstructions.get(blockStart).getInstruction().getFirstOperand())) {
                    return rawInstructions.get(j);
                }
            }
            if (rawInstructions.get(j).getInstruction().getOpCode() instanceof InstructionOpCode && rawInstructions.get(j).getInstruction().isWrittenToRegister(rawInstructions.get(blockStart).getInstruction().getFirstOperand())) {
                return null;
            }
        }
        return null;
    }

    private static final Map<String, String> offsetToRegisterMapping = new HashMap<>();

    private List<RawInstruction> extractLoadBlock(RawInstruction iterInstruction, List<RawInstruction> rawInstructions, String offset, List<RawInstruction> lastAddressInstructions, RawInstruction originInstruction) {

        List<RawInstruction> result = new ArrayList<>();

        for (int i = rawInstructions.indexOf(iterInstruction); !rawInstructions.get(i).isPseudoInstruction(); i++) {
            iterInstruction = rawInstructions.get(i);


            if (iterInstruction.isPseudoInstruction()) return Collections.emptyList();


            if (iterInstruction.getInstruction().getOpCode() instanceof InstructionOpCode
                    && InstructionOpCode.isLoadInstructionOpCode((InstructionOpCode) iterInstruction.getInstruction().getOpCode())
                    && lastAddressInstructions.stream().map(in -> in.getInstruction().getFirstOperand()).toList().contains(iterInstruction.getInstruction().getSecondOperand())
            ) {
                RawInstruction lastSubuInstruction = lastAddressInstructions.get(lastAddressInstructions.stream().map(in -> in.getInstruction().getFirstOperand()).toList().indexOf(iterInstruction.getInstruction().getSecondOperand()));
                if (lastSubuInstruction != null) result.add(lastSubuInstruction);
                result.add(iterInstruction);
            }
            if (iterInstruction.getInstruction().getOpCode() == SUBU
                    && Objects.equals(iterInstruction.getInstruction().getSecondOperand(), "$253")
                    && Objects.equals(iterInstruction.getInstruction().getThirdOperand(), offset)) {
                lastAddressInstructions.add(iterInstruction);
            }
            // We must stop when there is another write instruction to that register
            else {
                RawInstruction finalIterInstruction = iterInstruction;
                if (iterInstruction.getInstruction().getOpCode() instanceof InstructionOpCode
                        && !lastAddressInstructions.isEmpty()
                        && lastAddressInstructions.stream().map(in -> finalIterInstruction.getInstruction().isWrittenToRegister(in.getInstruction().getFirstOperand())).reduce(Boolean::logicalOr).get()
                ) {
                    RawInstruction finalIterInstruction1 = iterInstruction;
                    lastAddressInstructions.remove(lastAddressInstructions.get(lastAddressInstructions.stream().map(in -> finalIterInstruction1.getInstruction().isWrittenToRegister(in.getInstruction().getFirstOperand())).toList().lastIndexOf(true)));
                }
            }
            // This makes sure that if we store another stuff on that Stack address, that it is used from the last write, not the first
            if (iterInstruction.getInstruction().getOpCode() instanceof InstructionOpCode
                    && InstructionOpCode.isStoreInstructionOpCode((InstructionOpCode) iterInstruction.getInstruction().getOpCode())) {
                RawInstruction origin = findOriginInstruction(rawInstructions, iterInstruction, iterInstruction.getInstruction().getSecondOperand());
                if (origin != null && Objects.equals(origin.getInstruction().getSecondOperand(), "$253") && Objects.equals(origin.getInstruction().getThirdOperand(), offset) &&
                        !Objects.equals(originInstruction, origin)) {
                    return result;
                }
            }
        }

        return result;
    }

    private RawInstruction findOriginInstruction(List<RawInstruction> rawInstructions, RawInstruction startInstruction, String register) {
        for (int i = rawInstructions.indexOf(startInstruction); i > 0; i--) {
            if (rawInstructions.get(i).getInstruction().getOpCode() instanceof InstructionOpCode && rawInstructions.get(i).getInstruction().isWrittenToRegister(register)) {
                return rawInstructions.get(i);
            }
        }
        return null;
    }
    private static boolean patternUsed = false;
    /**
     * We can only use this pattern where there are no loading from memory in different subroutines.
     * We can use it in hanoi but not with quicksort, where elements are in memory.
     */
    private boolean cannotBeUsed(List<RawInstruction> rawInstructions) {
        if (patternUsed) return true;
        patternUsed = true;
        for (RawInstruction rawInstruction : rawInstructions) {
            if (rawInstruction.isPseudoInstruction()) {
                continue;
            }
            // Something is weird check this condition
            if (rawInstruction.getInstruction().getOpCode() instanceof InstructionOpCode
                    && !Objects.equals(rawInstruction.getInstruction().getSecondOperand(), "$254")
                    && rawInstruction.getInstruction().getThirdOperand() != null
                    && rawInstruction.getInstruction().getThirdOperand().contains("$")) {
                return true;
            }
            if (rawInstruction.getInstruction().getOpCode() instanceof InstructionOpCode
                    && Objects.equals(rawInstruction.getInstruction().getSecondOperand(), "$253")
                    && !rawInstruction.getPossibleNextInstructions().isEmpty()
                    && rawInstruction.getPossibleNextInstructions().get(0).getUnusedRegisters().contains(rawInstruction.getInstruction().getFirstOperand())
            ) {
                return true;
            }
            if (rawInstruction.getInstruction().getOpCode() instanceof InstructionOpCode
                    && Objects.equals(rawInstruction.getInstruction().getSecondOperand(), "$253")
                    && RegisterUtil.extractRegister(findPushXRegister(rawInstructions, rawInstruction.getSubroutine())) != 0
                    && RegisterUtil.extractRegister(findPushXRegister(rawInstructions, rawInstruction.getSubroutine())) <= RegisterUtil.extractRegister(rawInstruction.getInstruction().getFirstOperand())
            ) {
                return true;
            }
        }
        return false;
    }
}
