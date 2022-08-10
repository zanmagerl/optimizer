package si.fri.mag.magerl.phases.impl;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.JMP;
import static si.fri.mag.magerl.models.opcode.InstructionOpCode.POP;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.phases.Phase;
import si.fri.mag.magerl.utils.RoutineUtil;

/**
 * @author zan.magerl
 */
@Slf4j
public class GraphConstructionPhase implements Phase {

    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {

        // To clean up any stuff from previous optimizations
        for (RawInstruction rawInstruction : rawInstructions) {
            rawInstruction.setPossibleNextInstructions(new ArrayList<>());
            rawInstruction.setPossiblePrecedingInstruction(new ArrayList<>());
        }

        for (String routine : RoutineUtil.routineMapping.keySet()) {
            RawInstruction firstRoutineInstruction = RoutineUtil.routineMapping.get(routine);
            /**
             * Let's cover all instructions inside specific routine -> jumps are "probably" always contained inside one routine
             */
            int i;
            for (i = rawInstructions.indexOf(firstRoutineInstruction)+1; rawInstructions.get(i).getInstruction().getOpCode() != POP ; i++) {
                if (rawInstructions.get(i-1).getInstruction() != null && rawInstructions.get(i-1).getInstruction().getOpCode() != JMP) {
                    rawInstructions.get(i).addPredecessor(rawInstructions.get(i-1));
                    rawInstructions.get(i-1).addNextInstruction(rawInstructions.get(i));
                }
                Instruction instruction = rawInstructions.get(i).getInstruction();
                if (instruction.getOpCode() instanceof PseudoOpCode) {
                    continue;
                }
                if (InstructionOpCode.isBranchOrJumpInstructionOpCode((InstructionOpCode) instruction.getOpCode())) {
                    RawInstruction instructionAfterBranch = findInstructionWithLabel(rawInstructions, instruction.extractBranchLabel());
                    rawInstructions.get(i).addNextInstruction(instructionAfterBranch);
                    instructionAfterBranch.addPredecessor(rawInstructions.get(i));
                } else if (instruction.isSubroutineCall()) {
                    RawInstruction instructionAfterSubroutineCall = RoutineUtil.routineMapping.get(rawInstructions.get(i).extractSubroutineCallLabel(rawInstructions));
                    rawInstructions.get(i).addNextInstruction(instructionAfterSubroutineCall);
                    instructionAfterSubroutineCall.addPredecessor(rawInstructions.get(i));
                }
            }
            // POP instruction - we will need to do another round after all routines are done

        }

        for (String routine : RoutineUtil.routineMapping.keySet()) {
            // main is called from special initialized code Main
            //if (routine.equals("main")) continue;
            RawInstruction firstRoutineInstruction = RoutineUtil.routineMapping.get(routine);
            int i = rawInstructions.indexOf(firstRoutineInstruction);
            for (; rawInstructions.get(i).getInstruction().getOpCode() != POP; i++) { }
            rawInstructions.get(i).addPredecessor(rawInstructions.get(i-1));
            rawInstructions.get(i-1).addNextInstruction(rawInstructions.get(i));
            // We use every call of the subroutine and add them as possible next instructions for POP
            List<RawInstruction> callInstructions = firstRoutineInstruction.getPossiblePrecedingInstruction();
            List<RawInstruction> instructionsAfterCall = callInstructions.stream()
                    .map(callInstruction -> callInstruction.getPossibleNextInstructions().stream().filter(instr -> instr != firstRoutineInstruction).findFirst().get())
                    .toList();
            for (RawInstruction instructionAfterCall : instructionsAfterCall) {
                rawInstructions.get(i).addNextInstruction(instructionAfterCall);
                // Instruction after subroutine call is always after POP and not after PUSHJ/PUSHGO
                //instructionAfterCall.getPossiblePrecedingInstruction().clear();
                instructionAfterCall.addPredecessor(rawInstructions.get(i));
            }
        }
        return rawInstructions;
    }

    private RawInstruction findInstructionWithLabel(List<RawInstruction> rawInstructions, String label){
        for (RawInstruction rawInstruction : rawInstructions) {
            if (rawInstruction.getInstruction() != null && Objects.equals(rawInstruction.getInstruction().getLabel(), label)) {
                return rawInstruction;
            }
        }
        throw new RuntimeException("There are no instruction with label " + label);
    }

}
