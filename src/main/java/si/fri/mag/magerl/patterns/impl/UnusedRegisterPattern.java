package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.patterns.Pattern;
import si.fri.mag.magerl.utils.BranchingUtil;
import si.fri.mag.magerl.utils.CopyUtil;
import si.fri.mag.magerl.utils.RegisterUtil;

import java.util.*;
import java.util.function.Predicate;

import static si.fri.mag.magerl.config.BranchingConfig.BRANCHING_FACTOR;
import static si.fri.mag.magerl.config.BranchingConfig.NUMBER_OF_PROGRAMS;
import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class UnusedRegisterPattern implements Pattern {

    private final List<Integer> patternUsages = new ArrayList<>();

    @Override
    public List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> processedInstructions = new ArrayList<>(rawInstructions);
        processedInstructions = removeSetAfterArithmeticOperation(processedInstructions, optimizationDecider);
        return processedInstructions;
    }

    @Override
    public List<List<RawInstruction>> branchPattern(List<RawInstruction> rawInstructions) {
        patternUsages.clear();
        rawInstructions = usePattern(rawInstructions, x -> false);
        log.debug("Pattern {} is used in {}", this.getClass(), patternUsages);
        List<List<Integer>> combinations = BranchingUtil.sampleBranchingOptions(patternUsages, NUMBER_OF_PROGRAMS);
        List<List<RawInstruction>> possibilities = new ArrayList<>();
        for (List<Integer> combination : combinations) {
            possibilities.add(usePattern(CopyUtil.copyRawInstructions(rawInstructions), combination::contains));
        }
        return possibilities;
    }

    /***
     * Let's resolve pattern:
     * NEGU $0,0,$1
     * SET $1,$0
     * where $0 is not used anymore after that
     */

    private List<RawInstruction> removeSetAfterArithmeticOperation(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        boolean wasPatternUsed = false;
        for (int i = 0; i < rawInstructions.size()-1; i++) {
            if (rawInstructions.get(i).isPseudoInstruction() || rawInstructions.get(i).getInstruction().getOpCode() instanceof PseudoOpCode || wasPatternUsed) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }

            Instruction firstInstruction = rawInstructions.get(i).getInstruction();
            Instruction secondInstruction = rawInstructions.get(i+1).getInstruction();

            if (firstInstruction.getOpCode() == NEG && secondInstruction.getOpCode() == SET) {
                if (Objects.equals(firstInstruction.getFirstOperand(), secondInstruction.getSecondOperand()) && RegisterUtil.isUnusedAfterInstruction(firstInstruction.getFirstOperand(), rawInstructions.get(i+1))) {
                    patternUsages.add(rawInstructions.get(i).getId());
                    if (optimizationDecider.test(rawInstructions.get(i).getId())) {
                        log.debug("Combining instructions {} and {}", rawInstructions.get(i).getRawInstruction(), rawInstructions.get(i + 1).getRawInstruction());
                        rawInstructions.get(i).setInstruction(firstInstruction.toBuilder()
                                .firstOperand(secondInstruction.getFirstOperand())
                                .build());
                        processedInstructions.add(rawInstructions.get(i));
                        i++;
                        wasPatternUsed = true;
                        continue;
                    }
                }
            }

            if (InstructionOpCode.isArithmeticInstructionOpCode((InstructionOpCode) firstInstruction.getOpCode()) && secondInstruction.getOpCode() == SET) {
                if (Objects.equals(firstInstruction.getFirstOperand(), secondInstruction.getSecondOperand()) && RegisterUtil.isUnusedAfterInstruction(firstInstruction.getFirstOperand(), rawInstructions.get(i+1))) {
                    patternUsages.add(rawInstructions.get(i).getId());
                    if (optimizationDecider.test(rawInstructions.get(i).getId())) {
                        log.debug("Combining instructions {} and {}", rawInstructions.get(i).getRawInstruction(), rawInstructions.get(i + 1).getRawInstruction());
                        rawInstructions.get(i).setInstruction(firstInstruction.toBuilder()
                                .firstOperand(secondInstruction.getFirstOperand())
                                .build());
                        processedInstructions.add(rawInstructions.get(i));
                        i++;
                        wasPatternUsed = true;
                        continue;
                    }
                }
            }

            if (InstructionOpCode.isSignedLoadInstructionOpCode((InstructionOpCode) firstInstruction.getOpCode()) && secondInstruction.getOpCode() == SET) {
                if (Objects.equals(firstInstruction.getFirstOperand(), secondInstruction.getSecondOperand()) && RegisterUtil.isUnusedAfterInstruction(firstInstruction.getFirstOperand(), rawInstructions.get(i+1))) {
                    patternUsages.add(rawInstructions.get(i).getId());
                    if (optimizationDecider.test(rawInstructions.get(i).getId())) {
                        log.debug("Combining instructions {} and {}", rawInstructions.get(i).getRawInstruction(), rawInstructions.get(i + 1).getRawInstruction());
                        rawInstructions.get(i).setInstruction(firstInstruction.toBuilder()
                                .firstOperand(secondInstruction.getFirstOperand())
                                .build());
                        processedInstructions.add(rawInstructions.get(i));
                        i++;
                        wasPatternUsed = true;
                        continue;
                    }
                }
            }

            if (firstInstruction.getOpCode() == SETL && secondInstruction.getOpCode() == SET) {
                if (Objects.equals(firstInstruction.getFirstOperand(), secondInstruction.getSecondOperand()) && RegisterUtil.isUnusedAfterInstruction(firstInstruction.getFirstOperand(), rawInstructions.get(i+2))) {
                    patternUsages.add(rawInstructions.get(i).getId());
                    if (optimizationDecider.test(rawInstructions.get(i).getId())) {
                        log.debug("Combining instructions {} and {}", rawInstructions.get(i).getRawInstruction(), rawInstructions.get(i + 1).getRawInstruction());
                        rawInstructions.get(i).setInstruction(firstInstruction.toBuilder()
                                .firstOperand(secondInstruction.getFirstOperand())
                                .build());
                        processedInstructions.add(rawInstructions.get(i));
                        i++;
                        wasPatternUsed = true;
                        continue;
                    }
                }
            }
            /**
             * Removes stuff like that:
             * SET $1,#a
             * ...
             * SET $3,$1
             * ...
             * ...
             * SET $1,#sth
             *
             * AND
             *
             * 	SET $5,$0
             *  .... ($5 unused)
             * 	SET $7,$5
             */

            if (InstructionOpCode.isSignedLoadInstructionOpCode((InstructionOpCode) firstInstruction.getOpCode()) || firstInstruction.getOpCode() == SETL || firstInstruction.getOpCode() == SET || InstructionOpCode.isArithmeticInstructionOpCode((InstructionOpCode) firstInstruction.getOpCode())) {
                String potentiallyUnusedRegister = firstInstruction.getFirstOperand();
                RawInstruction returnedInstruction = instructionToCombineWith(rawInstructions.get(i+1), potentiallyUnusedRegister);
                if (returnedInstruction != null && returnedInstruction.getPossibleNextInstructions().get(0).getUnusedRegisters().contains(potentiallyUnusedRegister) && isUnusedBetween(rawInstructions.get(i), returnedInstruction, returnedInstruction.getInstruction().getFirstOperand())) {
                    patternUsages.add(rawInstructions.get(i).getId());
                    if (optimizationDecider.test(rawInstructions.get(i).getId())) {
                        log.debug("{}, Combining instructions more far away {} and {}", rawInstructions.get(i).getSubroutine(), rawInstructions.get(i).getRawInstruction(), returnedInstruction.getRawInstruction());
                        firstInstruction.setFirstOperand(returnedInstruction.getInstruction().getFirstOperand());
                        rawInstructions.remove(returnedInstruction);
                        wasPatternUsed = true;
                    }
                }

            }

            processedInstructions.add(rawInstructions.get(i));
        }
        processedInstructions.add(rawInstructions.get(rawInstructions.size()-1));
        return processedInstructions;
    }


    @Deprecated
    private List<RawInstruction> removeUnusedSetInstructions(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();

        for (int i = 0; i < rawInstructions.size(); i++) {
            if (rawInstructions.get(i).isPseudoInstruction() || rawInstructions.get(i).getInstruction().getOpCode() instanceof PseudoOpCode) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if (InstructionOpCode.isSignedLoadInstructionOpCode((InstructionOpCode) instruction.getOpCode()) || instruction.getOpCode() == SETL || instruction.getOpCode() == SET || InstructionOpCode.isArithmeticInstructionOpCode((InstructionOpCode) instruction.getOpCode())) {
                String potentiallyUnusedRegister = instruction.getFirstOperand();
                RawInstruction returnedInstruction = instructionToCombineWith(rawInstructions.get(i+1), potentiallyUnusedRegister);
                if (returnedInstruction != null && returnedInstruction.getPossibleNextInstructions().get(0).getUnusedRegisters().contains(potentiallyUnusedRegister) && isUnusedBetween(rawInstructions.get(i), returnedInstruction, returnedInstruction.getInstruction().getFirstOperand())) {
                    log.debug("{}, Combining instructions more far away {} and {}", rawInstructions.get(i).getSubroutine(), rawInstructions.get(i).getRawInstruction(), returnedInstruction.getRawInstruction());
                    instruction.setFirstOperand(returnedInstruction.getInstruction().getFirstOperand());
                    rawInstructions.remove(returnedInstruction);
                }
            }
            processedInstructions.add(rawInstructions.get(i));
        }

        return processedInstructions;
    }

    /**
     * Find SET that can be removed if we combine two instructions
     */
    private RawInstruction instructionToCombineWith(RawInstruction iterInstruction, String register) {

        if (iterInstruction.isPseudoInstruction()) {
            return null;
        }
        if (iterInstruction.getInstruction().getOpCode() instanceof PseudoOpCode) {
            if (iterInstruction.getPossibleNextInstructions().isEmpty()) return null;
            return instructionToCombineWith(iterInstruction.getPossibleNextInstructions().get(0), register);
        }

        if (iterInstruction.getInstruction().getOpCode() == POP) {
            return null;
        }

        if (iterInstruction.getInstruction().getOpCode() == SET && Objects.equals(iterInstruction.getInstruction().getSecondOperand(), register)) {
            if (iterInstruction.getPossibleNextInstructions().get(0).getUnusedRegisters().contains(register)) {
                return iterInstruction;
            } else {
                return null;
            }
        }

        if (iterInstruction.getInstruction().isReadFromRegister(register)) {
            return null;
        }

        if (iterInstruction.getPossibleNextInstructions().size() != 1) return null;
        return instructionToCombineWith(iterInstruction.getPossibleNextInstructions().get(0), register);
    }

    /**
     * Recursively check if is register in all instructions unused
     */
    private boolean isUnusedBetween(RawInstruction start, RawInstruction finish, String register) {
        if (start == finish) {
            return true;
        }
        if (start.getUnusedRegisters().contains(register)) {
            return isUnusedBetween(start.getPossibleNextInstructions().get(0), finish, register);
        }
        return false;
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
                        log.debug("Remove unused register: {}, {}", rawInstructions.get(i).getRawInstruction(), readInstructions.get(0).getRawInstruction());

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
