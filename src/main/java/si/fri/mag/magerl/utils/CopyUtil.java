package si.fri.mag.magerl.utils;

import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;

import java.util.ArrayList;
import java.util.List;

public class CopyUtil {


    public List<RawInstruction> copyRawInstructions(List<RawInstruction> rawInstructions) {
        List<RawInstruction> rawInstructionsCopy = new ArrayList<>();
        for (RawInstruction rawInstruction : rawInstructions) {
            rawInstructionsCopy.add(copyRawInstruction(rawInstruction));
        }
        return rawInstructionsCopy;
    }

    public RawInstruction copyRawInstruction(RawInstruction rawInstruction) {
        return rawInstruction.toBuilder()
                .possibleNextInstructions(new ArrayList<>())
                .possiblePrecedingInstruction(new ArrayList<>())
                .instruction(copyInstruction(rawInstruction.getInstruction()))
                .build();
    }

    public Instruction copyInstruction(Instruction instruction) {
        return instruction.toBuilder().build();
    }
}
