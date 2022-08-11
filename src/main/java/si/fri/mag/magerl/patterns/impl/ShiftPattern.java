package si.fri.mag.magerl.patterns.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.patterns.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ShiftPattern implements Pattern {
    @Override
    public List<RawInstruction> usePattern(List<RawInstruction> rawInstructions) {
        List<RawInstruction> processedInstructions = new ArrayList<>();
        for (int i = 0; i < rawInstructions.size() - 2; i++) {
            if (rawInstructions.get(i).isPseudoInstruction() || rawInstructions.get(i + 1).isPseudoInstruction() || rawInstructions.get(i + 2).isPseudoInstruction() || rawInstructions.get(i).getInstruction().getOpCode() instanceof PseudoOpCode) {
                processedInstructions.add(rawInstructions.get(i));
                continue;
            }
            processedInstructions.add(rawInstructions.get(i));
            if (isPotentiallyUselessShiftBlock(rawInstructions.get(i + 1), rawInstructions.get(i + 2))) {
                if (InstructionOpCode.isSignedLoadInstructionOpCode((InstructionOpCode) rawInstructions.get(i).getInstruction().getOpCode())
                        && Objects.equals(rawInstructions.get(i).getInstruction().getFirstOperand(), rawInstructions.get(i+1).getInstruction().getFirstOperand())) {
                    log.info("Useless shifting: {}, {}, {}", rawInstructions.get(i), rawInstructions.get(i + 1), rawInstructions.get(i + 2));
                    i += 2;
                    continue;
                }
            }

            if (isPotentiallyUnusedRegisterShiftBlock(rawInstructions.get(i), rawInstructions.get(i + 1), rawInstructions.get(i + 2))) {
                log.info("Useless shifting with unused registers: {}, {}, {}", rawInstructions.get(i), rawInstructions.get(i + 1), rawInstructions.get(i + 2));
                rawInstructions.get(i).setInstruction(rawInstructions.get(i).getInstruction().toBuilder()
                        .firstOperand(rawInstructions.get(i+1).getInstruction().getFirstOperand())
                        .build());
                i += 2;
            }

        }
        // Do not forget to include last two instructions!
        processedInstructions.addAll(rawInstructions.subList(rawInstructions.size() - 2, rawInstructions.size()));
        return processedInstructions;
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

    private boolean couldReplaceShiftBlockWithSet(RawInstruction firstInstruction, RawInstruction secondInstruction) {

        return false;
    }
}
