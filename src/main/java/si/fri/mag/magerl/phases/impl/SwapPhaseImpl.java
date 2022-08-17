package si.fri.mag.magerl.phases.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.phases.Phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
public class SwapPhaseImpl implements Phase {
    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        Random random = new Random();
        boolean isMain = false;
        for (int i = 0; i < rawInstructions.size() - 1; i++) {
            if (rawInstructions.get(i).isPseudoInstruction() || isMain) {
                continue;
            }
            if (Objects.equals(rawInstructions.get(i).getInstruction().getLabel(), "Main")) {
                isMain = true;
                continue;
            }
            List<RawInstruction> possibleSwaps = possibleSwaps(rawInstructions, rawInstructions.get(i));
            if (!possibleSwaps.isEmpty()) {
                RawInstruction swapper = possibleSwaps.get(random.nextInt(0, possibleSwaps.size()));
                List<RawInstruction> pred = swapper.getPossiblePrecedingInstruction();
                List<RawInstruction> next = swapper.getPossibleNextInstructions();
                swapper.setPossiblePrecedingInstruction(rawInstructions.get(i).getPossiblePrecedingInstruction());
                swapper.setPossibleNextInstructions(rawInstructions.get(i).getPossibleNextInstructions());
                rawInstructions.set(rawInstructions.indexOf(swapper), rawInstructions.get(i).toBuilder()
                        .possiblePrecedingInstruction(pred)
                        .possibleNextInstructions(next)
                        .build());
                rawInstructions.set(i, swapper);
            }
        }
        return rawInstructions;
    }

    private boolean shouldSafeSwapThem(RawInstruction firstInstruction, RawInstruction secondInstruction) {
        if (firstInstruction.isPseudoInstruction() || secondInstruction.isPseudoInstruction() || firstInstruction.getInstruction().getOpCode() instanceof PseudoOpCode || secondInstruction.getInstruction().getOpCode() instanceof PseudoOpCode) {
            return false;
        }
        if (Objects.equals(firstInstruction.getSubroutine(), "print_char") || Objects.equals(firstInstruction.getSubroutine(), "Main")) {
            return false;
        }
        if (firstInstruction.getInstruction().getOpCode() == TRAP || secondInstruction.getInstruction().getOpCode() == TRAP) {
            return false;
        }
        if (firstInstruction.getInstruction().hasLabel() || secondInstruction.getInstruction().hasLabel()) {
            return false;
        }
        if (InstructionOpCode.isBranchOrJumpInstructionOpCode((InstructionOpCode) firstInstruction.getInstruction().getOpCode()) || InstructionOpCode.isBranchOrJumpInstructionOpCode((InstructionOpCode) secondInstruction.getInstruction().getOpCode())) {
            return false;
        }
        if (firstInstruction.getInstruction().getOpCode() == POP || secondInstruction.getInstruction().getOpCode() == POP) {
            return false;
        }
        if (InstructionOpCode.isSubroutineInstructionOpCode((InstructionOpCode) firstInstruction.getInstruction().getOpCode()) || InstructionOpCode.isSubroutineInstructionOpCode((InstructionOpCode) secondInstruction.getInstruction().getOpCode())) {
            return false;
        }
        /*
        Special case when we read remainder when doing division. We must not read the reminder before actually doing division.
         */
        if (firstInstruction.getInstruction().getOpCode() == DIVU || (secondInstruction.getInstruction().getOpCode() == GET && secondInstruction.getInstruction().getSecondOperand().contains("rR"))) {
            return false;
        }
        return !secondInstruction.getInstruction().usesRegister(firstInstruction.getInstruction().getFirstOperand())
                && !firstInstruction.getInstruction().usesRegister(secondInstruction.getInstruction().getFirstOperand());
    }

    private List<RawInstruction> possibleSwaps(List<RawInstruction> rawInstructions, RawInstruction rawInstruction) {
        List<RawInstruction> possibilities = new ArrayList<>();
        for (int j = rawInstructions.indexOf(rawInstruction) + 1; ; j++) {
            if (!shouldSafeSwapThem(rawInstruction, rawInstructions.get(j))) {
                break;
            } else {
                boolean canBeSwapped = true;
                for (int n = rawInstructions.indexOf(rawInstruction) + 1; n < j; n++) {
                    canBeSwapped &= shouldSafeSwapThem(rawInstructions.get(n), rawInstructions.get(j));
                }
                if (canBeSwapped) {
                    possibilities.add(rawInstructions.get(j));
                }
            }
        }
        return possibilities;
    }
}
