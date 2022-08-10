package si.fri.mag.magerl.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.utils.RoutineUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.PUSHJ;

@Data
public class RawInstruction implements Comparable{

    private static Integer counter = 0;

    final Integer id;
    Instruction instruction;
    String rawInstruction;
    boolean inLoop;
    boolean isPseudoInstruction;
    String subroutine;
    ArrayList<String> unusedRegisters = new ArrayList<>();

    public void addUnusedRegisters(ArrayList<String> unusedRegisters) {
        if (this.unusedRegisters.isEmpty()) {
            this.unusedRegisters = unusedRegisters;
        } else {
          this.unusedRegisters.retainAll(new ArrayList<>(unusedRegisters));
        }
    }

    // This is for graph purposes
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<RawInstruction> possibleNextInstructions = new ArrayList<>();
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<RawInstruction> possiblePrecedingInstruction = new ArrayList<>();
    public void addNextInstruction(RawInstruction rawInstruction) {
        this.possibleNextInstructions.add(rawInstruction);
    }
    public void addPredecessor(RawInstruction rawInstruction) {
        this.possiblePrecedingInstruction.add(rawInstruction);
    }
    //

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

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * This is actually not always correct, but it works for debugging and visualizing purposes.
     */
    @Override
    public int compareTo(Object o) {
        return this.id.compareTo(((RawInstruction)o).getId());
    }


    public String extractSubroutineCallLabel(List<RawInstruction> rawInstructions) {
        if (!InstructionOpCode.isSubroutineInstructionOpCode((InstructionOpCode) this.instruction.getOpCode())) {
            throw new RuntimeException("Expected PUSHJ or PUSHGO instruction but this was not the case: " + this);
        }
        if (this.instruction.getOpCode() == PUSHJ) {
            return this.instruction.getSecondOperand();
        }
        return RoutineUtil.findRoutineNameForPushGo(rawInstructions, this);
    }
}
