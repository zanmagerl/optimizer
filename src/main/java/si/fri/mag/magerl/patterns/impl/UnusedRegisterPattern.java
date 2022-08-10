package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.utils.RegisterUtil;

import java.util.*;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class UnusedRegisterPattern implements Pattern {

    @Override
    public List<RawInstruction> usePattern(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>(rawInstructions);
        processedInstructions = removeSetAfterArithmeticOperation(processedInstructions);
        return processedInstructions;
    }

    /***
     * Let's resolve pattern:
     * NEGU $0,0,$1
     * SET $1,$0
     * where $0 is not used anymore after that
     */

    private List<RawInstruction> removeSetAfterArithmeticOperation(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        for (int i = 0; i < rawInstructions.size()-1; i++) {
            if (rawInstructions.get(i).isPseudoInstruction() || rawInstructions.get(i).getInstruction().getOpCode() instanceof PseudoOpCode) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }

            Instruction firstInstruction = rawInstructions.get(i).getInstruction();
            Instruction secondInstruction = rawInstructions.get(i+1).getInstruction();

            if (firstInstruction.getOpCode() == NEG && secondInstruction.getOpCode() == SET) {
                if (Objects.equals(firstInstruction.getFirstOperand(), secondInstruction.getSecondOperand()) && RegisterUtil.isUnusedAfterInstruction(firstInstruction.getFirstOperand(), rawInstructions.get(i+1))) {
                    log.info("Combining instructions {} and {}", rawInstructions.get(i).getRawInstruction(), rawInstructions.get(i+1).getRawInstruction());
                    rawInstructions.get(i).setInstruction(firstInstruction.toBuilder()
                            .firstOperand(secondInstruction.getFirstOperand())
                            .build());
                    processedInstructions.add(rawInstructions.get(i));
                    i++;
                    continue;
                }
            }

            if (InstructionOpCode.isArithmeticInstructionOpCode((InstructionOpCode) firstInstruction.getOpCode()) && secondInstruction.getOpCode() == SET) {
                if (Objects.equals(firstInstruction.getFirstOperand(), secondInstruction.getSecondOperand()) && RegisterUtil.isUnusedAfterInstruction(firstInstruction.getFirstOperand(), rawInstructions.get(i+1))) {
                    log.info("Combining instructions {} and {}", rawInstructions.get(i).getRawInstruction(), rawInstructions.get(i+1).getRawInstruction());
                    rawInstructions.get(i).setInstruction(firstInstruction.toBuilder()
                            .firstOperand(secondInstruction.getFirstOperand())
                            .build());
                    processedInstructions.add(rawInstructions.get(i));
                    i++;
                    continue;
                }
            }

            processedInstructions.add(rawInstructions.get(i));
        }
        processedInstructions.add(rawInstructions.get(rawInstructions.size()-1));
        return processedInstructions;
    }


    private List<RawInstruction> removeUnusedSetInstructions(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();

        for (int i = 0; i < rawInstructions.size(); i++) {
            if (rawInstructions.get(i).isPseudoInstruction()) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if (instruction.getOpCode() == SET || instruction.getOpCode() == SETL) {
                String potentiallyUnusedRegister = instruction.getFirstOperand();

                if (rawInstructions.get(i+2).getUnusedRegisters().contains(potentiallyUnusedRegister)) {

                }
            }
        }

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

                        rawInstructions.get(i).getInstruction().setFirstOperand(readInstructions.get(0).getInstruction().getFirstOperand());

                        processedInstructions.add(rawInstructions.get(i));
                        rawInstructions.remove(readInstructions.get(0));
                        continue;

                    }
                }
            }
            processedInstructions.add(rawInstructions.get(i));
        }
        return processedInstructions;
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
