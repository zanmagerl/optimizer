package si.fri.mag.magerl.models;

import lombok.Data;

@Data
public class RawInstruction {

    private static Integer counter = 0;

    final Integer id;
    Instruction instruction;
    String rawInstruction;
    boolean inLoop;
    boolean isPseudoInstruction;
    String subroutine;

    public RawInstruction(String rawInstruction){
        this.rawInstruction = rawInstruction;
        this.id = counter++;

        if (this.rawInstruction.contains(".") || this.rawInstruction.contains("!")) {
            this.isPseudoInstruction = true;
        } else {
            instruction = new Instruction(rawInstruction);
        }
    }
}
