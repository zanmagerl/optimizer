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

    public String getRawInstruction(){
        if (this.isPseudoInstruction) {
            return this.rawInstruction;
        }
        String result = "";
        if (this.instruction.getLabel() != null) {
            result += this.instruction.label;
        }

        result += "\t" + this.instruction.getOpCode() + " " + this.instruction.getFirstOperand();

        if (this.instruction.getSecondOperand() != null) {
            result += "," + this.instruction.getSecondOperand();
        }

        if (this.instruction.getThirdOperand() != null) {
            result += "," + this.instruction.getThirdOperand();
        }

        return result;
    }
}
