package si.fri.mag.magerl.phases.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.utils.RegisterUtil;
import si.fri.mag.magerl.utils.RoutineUtil;
import si.fri.mag.magerl.phases.Phase;

import java.util.*;
import java.util.stream.IntStream;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.POP;

@Slf4j
public class RegisterUsagesPhaseImpl implements Phase {

    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        memo = new HashMap<>();
        for (String routine : RoutineUtil.routineMapping.keySet()) {
            RegisterUtil.availableRegisters.put(routine, new HashMap<>());
            List<RawInstruction> routineSourceCode = getRoutineSourceCode(rawInstructions, routine);
            List<RawInstruction> reversedElements = routineSourceCode.subList(0, routineSourceCode.size());
            Collections.reverse(reversedElements);
            Set<String> currentlyUnusedRegisters = new HashSet<>(IntStream.range(0, highestLocalRegister(routineSourceCode) + 1).mapToObj(number -> "$" + number).toList());
            RawInstruction iterInstruction = reversedElements.get(0);
            findUnusedRegisters(routine, iterInstruction, currentlyUnusedRegisters,  highestLocalRegister(routineSourceCode) + 1, null);
        }

        return rawInstructions;
    }

    Map<RawInstruction, List<RawInstruction>> memo = new HashMap<>();

    private void findUnusedRegisters(String routine, RawInstruction iterInstruction, Set<String> currentlyUnusedRegisters, int highestLocalRegister, RawInstruction fromInstruction) {
        // To avoid loops
        if(memo.containsKey(iterInstruction)) {
            if (memo.get(iterInstruction).contains(fromInstruction)) {
                return;
            }
        }

        if (!Objects.equals(routine, iterInstruction.getSubroutine())) {
            return;
        }
        if (Objects.equals(RoutineUtil.routineMapping.get(routine).getId(), iterInstruction.getId())) {
            return;
        }

        /**
         * Here we now go and assume first that all registers and free and then decide depending if it is a write or read instruction
         */
        Instruction instruction = iterInstruction.getInstruction();
        List<String> localRegisters = iterInstruction.getInstruction().getLocalRegisters();
        for (String register : localRegisters) {
            if (instruction.isReadFromRegister(register)) {
                // We remove it from unused registers since we need its content
                currentlyUnusedRegisters.remove(register);
            } else if (instruction.isWrittenToRegister(register)) {
                // We add it to the unused registers since we overwrite its content
                currentlyUnusedRegisters.add(register);
            }
        }

        if (instruction.isSubroutineCall()) {
            log.debug("Before {}: {}", instruction, currentlyUnusedRegisters);
            int pushX = RegisterUtil.extractRegister(instruction.getFirstOperand());
            currentlyUnusedRegisters.removeAll(IntStream.range(pushX+1, highestLocalRegister+1).mapToObj(number -> "$" + number).toList());
            log.debug("After {}: {}", instruction, currentlyUnusedRegisters);

        }
        iterInstruction.addUnusedRegisters(new ArrayList<>(new ArrayList<>(currentlyUnusedRegisters).stream().sorted().toList()));

        if (memo.containsKey(iterInstruction)) {
            memo.get(iterInstruction).add(fromInstruction);
        } else if (fromInstruction != null){
            memo.put(iterInstruction, new ArrayList<>(List.of(fromInstruction)));
        }

        for (RawInstruction previousInstruction : iterInstruction.getPossiblePrecedingInstruction()) {
            findUnusedRegisters(routine, previousInstruction, new HashSet<>(currentlyUnusedRegisters), highestLocalRegister, iterInstruction);
        }
    }



    private List<RawInstruction> getRoutineSourceCode(List<RawInstruction> rawInstructions, String routine) {
        List<RawInstruction> routineSourceCode = new ArrayList<>();
        int startIndex = rawInstructions.indexOf(RoutineUtil.routineMapping.get(routine));
        for (int i = startIndex; ; i++) {
            routineSourceCode.add(rawInstructions.get(i));
            if (rawInstructions.get(i).getInstruction().getOpCode() == POP) {
                break;
            }
        }
        return routineSourceCode;
    }

    private Integer highestLocalRegister(List<RawInstruction> rawInstructions) {
        int highestLocalRegister = 0;
        for (RawInstruction rawInstruction : rawInstructions) {
            Instruction instruction = rawInstruction.getInstruction();
            if (RegisterUtil.isLocalRegister(instruction.getFirstOperand())) {
                highestLocalRegister = Math.max(highestLocalRegister, RegisterUtil.extractRegister(instruction.getFirstOperand()));
            }
            if (RegisterUtil.isLocalRegister(instruction.getSecondOperand())) {
                highestLocalRegister = Math.max(highestLocalRegister, RegisterUtil.extractRegister(instruction.getSecondOperand()));
            }
            if (RegisterUtil.isLocalRegister(instruction.getThirdOperand())) {
                highestLocalRegister = Math.max(highestLocalRegister, RegisterUtil.extractRegister(instruction.getThirdOperand()));
            }
        }
        return highestLocalRegister;
    }

    /**
     * Register is unused:
     * - after it was written to and it was never read
     * - after it was read for the last time in routine
     * Be aware of the other subroutine calls. We do not know what is in registers > $X after they come back.
     * You must always use a write instruction before read, which will be done either way, since if the register is unused
     * we do not care what is in it, since we will overwrite it.
     */
}
