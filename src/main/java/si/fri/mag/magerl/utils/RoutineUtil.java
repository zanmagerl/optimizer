package si.fri.mag.magerl.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.GETA;

@Slf4j
@UtilityClass
public class RoutineUtil {

    public Map<String, RawInstruction> routineMapping = new HashMap<>();

    public String findRoutineNameForPushGo(List<RawInstruction> rawInstructions, RawInstruction rawInstruction) {
        int index = rawInstructions.indexOf(rawInstruction);
        for (int i = index - 1; i >= 0; i--) {
            if (rawInstructions.get(i).isPseudoInstruction()) {
                continue;
            }
            Instruction instruction = rawInstructions.get(i).getInstruction();
            if (instruction.getOpCode() == GETA) {
                if (Objects.equals(rawInstruction.getInstruction().getSecondOperand(), instruction.getFirstOperand())) {
                    return instruction.getSecondOperand();
                }
            }
        }
        throw new RuntimeException("Could not find routine name in previous instructions");
    }
}
