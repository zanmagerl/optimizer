package si.fri.mag.magerl.phases.impl;

import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.utils.RoutineUtil;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.OpCode;
import si.fri.mag.magerl.phases.Phase;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.GETA;
import static si.fri.mag.magerl.models.opcode.InstructionOpCode.POP;

public class SubroutineLabelingPhaseImpl implements Phase {

    private final Set<String> subroutineNames = new HashSet<>();

    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        findSubroutineNames(rawInstructions);
        String currentRoutine = null;
        for (int i = 0; i < rawInstructions.size()-1; i++) {
            RawInstruction rawInstruction = rawInstructions.get(i);
            if (rawInstruction.isPseudoInstruction()) {
                continue;
            }
            if (subroutineNames.contains(rawInstruction.getInstruction().getLabel())) {
                currentRoutine = rawInstruction.getInstruction().getLabel();
                RoutineUtil.routineMapping.put(currentRoutine, rawInstruction);
            }
            rawInstruction.setSubroutine(currentRoutine);
            // After POP instruction we go out of the subroutine
            if (rawInstructions.get(i+1).isPseudoInstruction()) {
                currentRoutine = null;
            }
        }
        rawInstructions.get(rawInstructions.size()-1).setSubroutine(currentRoutine);
        return rawInstructions;
    }

    private void findSubroutineNames(List<RawInstruction> rawInstructions) {
        for (int i = 0; i < rawInstructions.size(); i++) {
            RawInstruction rawInstruction = rawInstructions.get(i);
            OpCode opCode = Optional.ofNullable(rawInstruction).map(RawInstruction::getInstruction).map(Instruction::getOpCode).orElse(null);
            if (opCode instanceof InstructionOpCode) {
                switch ((InstructionOpCode) opCode) {
                    case PUSHJ -> subroutineNames.add(rawInstruction.getInstruction().getSecondOperand());
                    case PUSHGO -> subroutineNames.add(findLabelOfAddress(rawInstructions, i));
                }
            }
        }
    }

    private String findLabelOfAddress(List<RawInstruction> rawInstructions, Integer indexOfPushGo) {
        String registerName = rawInstructions.get(indexOfPushGo).getInstruction().getSecondOperand();
        for (int i = indexOfPushGo - 1; i >= 0; i--) {
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if (instruction != null) {
                if (registerName.equals(instruction.getFirstOperand())) {
                    if (instruction.getOpCode() == GETA) {
                        return instruction.getSecondOperand();
                    }
                    throw new RuntimeException("Unexpected write operation: " + rawInstructions.get(i));
                }
            }
        }
        throw new RuntimeException("Was not able to found label of the routine");
    }
}
