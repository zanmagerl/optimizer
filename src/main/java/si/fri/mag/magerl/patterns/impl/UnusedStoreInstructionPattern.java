package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.phases.impl.GraphConstructionPhaseImpl;
import si.fri.mag.magerl.utils.RegisterUtil;
import si.fri.mag.magerl.utils.RoutineUtil;

import java.util.*;
import java.util.function.Predicate;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class UnusedStoreInstructionPattern implements Pattern {

    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        if (1==1) return rawInstructions;
        rawInstructions = new GraphConstructionPhaseImpl().visit(rawInstructions);
        for (int i = 0; i < rawInstructions.size(); i++) {
            RawInstruction currentInstruction = rawInstructions.get(i);
            // We need to reset stack pointer mappings to global registers
            if (Objects.equals(RoutineUtil.routineMapping.get(currentInstruction.getSubroutine()), currentInstruction)) offsetToRegisterMapping.clear();
            if (currentInstruction.isPseudoInstruction()) continue;
            if (currentInstruction.getInstruction().getOpCode() == SUBU && Objects.equals(currentInstruction.getInstruction().getSecondOperand(), "$253")) {
                RawInstruction saveInstruction = extractStoreBlock(rawInstructions, i);
                if (saveInstruction != null) {
                    String offset = currentInstruction.getInstruction().getThirdOperand();
                    List<RawInstruction> extractedLoadBlock = extractLoadBlock(saveInstruction, rawInstructions, offset, new ArrayList<>(), currentInstruction);
                    rawInstructions.remove(currentInstruction);
                    String globalRegister;
                    if (offsetToRegisterMapping.containsKey(offset)) {
                        globalRegister = offsetToRegisterMapping.get(offset);
                    } else {
                        globalRegister = RegisterUtil.getAvailableGlobalRegister();
                        offsetToRegisterMapping.put(offset, globalRegister);
                    }
                    RawInstruction substituteInstruction = useRegisterToRegisterInsteadOfStack(globalRegister, saveInstruction.getInstruction().getFirstOperand());
                    rawInstructions.add(rawInstructions.indexOf(saveInstruction), substituteInstruction);
                    log.info("Combine {} {} -> to {}", currentInstruction.getRawInstruction(), saveInstruction.getRawInstruction(), substituteInstruction.getRawInstruction());
                    rawInstructions.remove(saveInstruction);
                    for (int n = 0; n < extractedLoadBlock.size(); n++) {
                        if (extractedLoadBlock.get(n).getInstruction().getOpCode() == SUBU) {
                            rawInstructions.remove(extractedLoadBlock.get(n));
                            log.info("Remove instruction {}", extractedLoadBlock.get(n).getRawInstruction());
                        } else {
                            log.info("{}", extractedLoadBlock.get(n).printInstructionInfo());
                            int blockStart = rawInstructions.indexOf(extractedLoadBlock.get(n));
                            rawInstructions.remove(rawInstructions.get(blockStart));
                            RawInstruction loadRegister = useRegisterToRegisterInsteadOfStack(extractedLoadBlock.get(n).getInstruction().getFirstOperand(), globalRegister);
                            rawInstructions.add(blockStart, loadRegister);
                            log.info("Change instruction {} -> {}", extractedLoadBlock.get(n).getRawInstruction(), rawInstructions.get(blockStart).getRawInstruction());
                            if (extractedLoadBlock.get(n).getInstruction().getOpCode() == LDB) {
                                rawInstructions.add(blockStart+1, RawInstruction.builder()
                                        .instruction(Instruction.builder()
                                                .opCode(AND)
                                                .firstOperand(loadRegister.getInstruction().getFirstOperand())
                                                .secondOperand(loadRegister.getInstruction().getFirstOperand())
                                                .thirdOperand("#ff")
                                                .build())
                                        .build());
                            } else if (extractedLoadBlock.get(n).getInstruction().getOpCode() == LDT) {
                                rawInstructions.add(blockStart+1, RawInstruction.builder()
                                        .instruction(Instruction.builder()
                                                .opCode(SLU)
                                                .firstOperand(loadRegister.getInstruction().getFirstOperand())
                                                .secondOperand(loadRegister.getInstruction().getFirstOperand())
                                                .thirdOperand("32")
                                                .build())
                                        .build());
                                rawInstructions.add(blockStart+2, RawInstruction.builder()
                                        .instruction(Instruction.builder()
                                                .opCode(SR)
                                                .firstOperand(loadRegister.getInstruction().getFirstOperand())
                                                .secondOperand(loadRegister.getInstruction().getFirstOperand())
                                                .thirdOperand("32")
                                                .build())
                                        .build());
                            }
                        }
                    }
                    log.info("\n");
                    rawInstructions = new GraphConstructionPhaseImpl().visit(rawInstructions);
                }
            }
        }

        return rawInstructions;
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
}
