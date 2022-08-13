package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.patterns.Pattern;

import java.util.ArrayList;
import java.util.List;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class SwapPattern implements Pattern {
    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>(rawInstructions);
        processedInstructions = removeSwappingInstructions(processedInstructions);
        return processedInstructions;
    }

    @Override
    public List<RawInstruction> branchPattern(List<RawInstruction> rawInstructions) {
        return null;
    }


    private List<RawInstruction> removeUnusedRegisters(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();



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
                        wasPatternUsed = true;
                        continue;
                    }
                }
            }
            processedInstructions.add(rawInstructions.get(i));
        }

        return processedInstructions;
    }

    private boolean isSwapBackAndUnused(List<RawInstruction> rawInstructions, Integer index, Instruction firstInstruction) {
        for (int j = index ; rawInstructions.get(j).getInstruction().getOpCode() != POP; j++) {
            Instruction instruction = rawInstructions.get(j).getInstruction();
            if (instruction.hasLabel()) {
                return false;
            }
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
}
