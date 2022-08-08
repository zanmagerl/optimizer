package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.patterns.Pattern;

import java.util.*;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class UnusedRegisterPattern implements Pattern {

    @Override
    public List<RawInstruction> usePattern(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>(rawInstructions);
        processedInstructions = removeSwappingInstructions(processedInstructions);
        return processedInstructions;
    }

    /**
     * Removes stuff like that:
     * SET $1,#a
     * ...
     * SET $3,$1
     * ...
     * ...
     * SET $1,#sth
     */
    private List<RawInstruction> unusedSetInstruction(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        for (int i = 0; i < rawInstructions.size(); i++) {
            if (rawInstructions.get(i).isPseudoInstruction()) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if ((instruction.getOpCode() == SET || instruction.getOpCode() == SETL)) {
                List<RawInstruction> readInstructions = readBeforeWrittenAgain(rawInstructions, i + 1, instruction.getFirstOperand(), new HashMap<>());
                if (readInstructions.size() == 1 && readInstructions.get(0).getInstruction().getOpCode() == SET) {
                    if (readBeforeWrittenAgain(rawInstructions, i+1, readInstructions.get(0).getInstruction().getFirstOperand(), new HashMap<>()).isEmpty()) {
                        log.info("Remove unused register: {}, {}", rawInstructions.get(i).getRawInstruction(), readInstructions.get(0).getRawInstruction());
                        /*
                        rawInstructions.get(i).getInstruction().setFirstOperand(readInstructions.get(0).getInstruction().getFirstOperand());

                        processedInstructions.add(rawInstructions.get(i));
                        rawInstructions.remove(readInstructions.get(0));
                        continue;
                        */
                    }
                }
            }
            processedInstructions.add(rawInstructions.get(i));
        }
        return processedInstructions;
    }

    /**
     * This works well, but we need to fix problems with:
     * SET $1,$0
     * L:26 IS @ <- here something can jump
     * SET $0,$1
     */
    private List<RawInstruction> removeSwappingInstructions(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();

        for (int i = 0; i < rawInstructions.size(); i++) {
            if (rawInstructions.get(i).isPseudoInstruction()) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if (instruction.getOpCode() == SET) {
                if (rawInstructions.get(i+1).getUnusedRegisters().contains(instruction.getSecondOperand()) && !instruction.containsGlobalRegisters() && isSwapBackAndUnused(rawInstructions, i+1, instruction)) {

                    if (!isCallInstructionNear(rawInstructions, i)) {
                        log.info("{}: Remove swapping instruction: {}", rawInstructions.get(i).getSubroutine(), rawInstructions.get(i).getRawInstruction());
                        log.info("Unused registers: {}", rawInstructions.get(i+1).getUnusedRegisters());
                        rawInstructions.get(i+1).getInstruction().changeOperand(instruction.getFirstOperand(), instruction.getSecondOperand());
                        String substituteRegister = null;
                        int j;
                        for (j = i+2; !rawInstructions.get(j).getInstruction().isTwinSwapInstruction(instruction); j++) {
                            if (rawInstructions.get(j).getInstruction().usesRegister(instruction.getFirstOperand())) {
                                log.info("Modify instruction: {} {} -> {}", rawInstructions.get(j).getRawInstruction(), instruction.getFirstOperand(), instruction.getSecondOperand());
                                rawInstructions.get(j).getInstruction().changeOperand(instruction.getFirstOperand(), instruction.getSecondOperand());
                            }
                            if (rawInstructions.get(j).getInstruction().usesRegister(instruction.getSecondOperand())) {
                                log.info("Modify instruction: {} {} -> {}", rawInstructions.get(j).getRawInstruction(), instruction.getFirstOperand(),
                                        instruction.getSecondOperand());
                                if (substituteRegister == null) {
                                    substituteRegister = rawInstructions.get(j).getUnusedRegisters().get(rawInstructions.get(j).getUnusedRegisters().size()-1);
                                }
                                rawInstructions.get(j).getInstruction().changeOperand(instruction.getSecondOperand(), substituteRegister);
                            }
                        }
                        rawInstructions.remove(rawInstructions.get(j));
                        continue;
                    }
                }
            }
            processedInstructions.add(rawInstructions.get(i));
        }

        return processedInstructions;
    }

    private boolean isSwapBackAndUnused(List<RawInstruction> rawInstructions, Integer index, Instruction firstInstruction) {
        for (int j = index+1 ; rawInstructions.get(j).getInstruction().getOpCode() != POP; j++) {
            Instruction instruction = rawInstructions.get(j).getInstruction();
            if (instruction.getOpCode() instanceof PseudoOpCode) continue;
            if (instruction.isTwinSwapInstruction(firstInstruction)) {
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

    private List<RawInstruction> readBeforeWrittenAgain(List<RawInstruction> rawInstructions, int index, String register, HashMap<String, Boolean> visitedLabels) {
        if (visitedLabels.get(rawInstructions.get(index).getInstruction().getLabel()) != null) {
            return Collections.emptyList();
        }
        visitedLabels.put(rawInstructions.get(index).getInstruction().getLabel(), true);
        List<RawInstruction> readInstructions = new ArrayList<>();
        for (int i = index; i < rawInstructions.size(); i++) {
            if (rawInstructions.get(i).isPseudoInstruction()) {
                continue;
            }
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if (instruction.getOpCode() instanceof InstructionOpCode) {
                if (InstructionOpCode.isBranchInstructionOpCode((InstructionOpCode) instruction.getOpCode())) {
                    readInstructions.addAll(readBeforeWrittenAgain(
                            rawInstructions,
                            rawInstructions.indexOf(rawInstructions.stream().filter(rawInstruction -> {
                                if (rawInstruction.getInstruction() != null) {
                                    return Objects.equals(rawInstruction.getInstruction().getLabel(), instruction.getSecondOperand());
                                }
                                return false;
                            }).findFirst().get()),
                            register,
                            visitedLabels));
                }
                if (!InstructionOpCode.isStoreInstructionOpCode((InstructionOpCode) instruction.getOpCode()) && Objects.equals(register, instruction.getSecondOperand()) || Objects.equals(register, instruction.getThirdOperand())) {
                    readInstructions.add(rawInstructions.get(i));
                }
            }
            if (!readInstructions.isEmpty()) {
                return readInstructions;
            }
            if (instruction.getOpCode() == POP || Objects.equals(instruction.getFirstOperand(), register)) {
                break;
            }
        }
        return readInstructions;
    }
}
