package si.fri.mag.magerl.utils;

import lombok.experimental.UtilityClass;
import si.fri.mag.magerl.models.Instruction;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.impl.GraphConstructionPhaseImpl;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CopyUtil {


    public List<RawInstruction> copyRawInstructions(List<RawInstruction> rawInstructions) {
        List<RawInstruction> rawInstructionsCopy = new ArrayList<>();
        for (RawInstruction rawInstruction : rawInstructions) {
            rawInstructionsCopy.add(copyRawInstruction(rawInstruction));
        }
        return new GraphConstructionPhaseImpl().visit(rawInstructionsCopy);
    }

    public RawInstruction copyRawInstruction(RawInstruction rawInstruction) {
        return rawInstruction.toBuilder()
                .possibleNextInstructions(new ArrayList<>())
                .possiblePrecedingInstruction(new ArrayList<>())
                .instruction(copyInstruction(rawInstruction.getInstruction()))
                .unusedRegisters(new ArrayList<>(rawInstruction.getUnusedRegisters()))
                .build();
    }



    public Instruction copyInstruction(Instruction instruction) {
        if (instruction == null) {
            return null;
        }
        return instruction.toBuilder().build();
    }
}
