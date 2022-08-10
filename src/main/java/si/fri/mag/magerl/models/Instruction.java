package si.fri.mag.magerl.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.OpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;
import si.fri.mag.magerl.utils.RegisterUtil;
import si.fri.mag.magerl.utils.RoutineUtil;

import java.util.*;

import static si.fri.mag.magerl.models.opcode.InstructionOpCode.*;

@Slf4j
@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Instruction {

    String label;
    OpCode opCode;

    String firstOperand;
    String secondOperand;
    String thirdOperand;

    public Instruction(String rawInstruction) {
        List<String> parts = Arrays.stream(rawInstruction.split("\\s+")).map(String::trim).filter(part -> part.length() > 0).toList();
        if (hasLabel(parts)) {
            this.label = parts.get(0);
            processRawInstruction(rawInstruction, parts.subList(1, parts.size()));
        } else {
            processRawInstruction(rawInstruction, parts);
        }
    }

    public void processRawInstruction(String rawInstruction, List<String> parts) {
        Optional<InstructionOpCode> instructionOpCode = InstructionOpCode.from(parts.get(0));
        if (instructionOpCode.isPresent()) {
            opCode = instructionOpCode.get();
            String[] operands = parts.get(1).split(",");
            if (operands.length > 0) {
                this.firstOperand = operands[0];
            }
            if (operands.length > 1) {
                this.secondOperand = operands[1];
            }
            if (operands.length > 2) {
                this.thirdOperand = operands[2];
            }
            if (operands.length > 3) {
                throw new IllegalArgumentException("[Unparsable instruction] Too many operands: " + rawInstruction);
            }
        } else {
            Optional<PseudoOpCode> pseudoOpCode = PseudoOpCode.from(parts.get(0));
            if (pseudoOpCode.isPresent()) {
                opCode = pseudoOpCode.get();
                firstOperand = String.join(" ", parts.subList(1, parts.size()));
            } else {
                throw new IllegalArgumentException("[Unparsable instruction] Not known opcode: " + rawInstruction);
            }
        }
    }

    private static boolean hasLabel(List<String> parts) {
        return InstructionOpCode.from(parts.get(0)).isEmpty() && PseudoOpCode.from(parts.get(0)).isEmpty();
    }

    public boolean isWrittenToRegister(String register) {
        if (InstructionOpCode.isStoreInstructionOpCode((InstructionOpCode) this.opCode)) {
            return false;
        }
        return Objects.equals(this.firstOperand, register);
    }

    public boolean isReadFromRegister(String register) {
        if (InstructionOpCode.isStoreInstructionOpCode((InstructionOpCode) this.opCode)) {
            if (Objects.equals(this.firstOperand, register) || Objects.equals(this.secondOperand, register) || Objects.equals(this.thirdOperand, register)) {
                return true;
            }
        }
        return Objects.equals(this.secondOperand, register) || Objects.equals(this.thirdOperand, register);
    }

    public List<String> getLocalRegisters() {
        List<String> registers = new ArrayList<>();
        if (RegisterUtil.isLocalRegister(this.firstOperand)) {
            registers.add(this.firstOperand);
        }
        if (RegisterUtil.isLocalRegister(this.secondOperand)) {
            registers.add(this.secondOperand);
        }
        if (RegisterUtil.isLocalRegister(this.thirdOperand)) {
            registers.add(this.thirdOperand);
        }
        return registers;
    }

    public boolean usesRegister(String register) {
        return Objects.equals(this.firstOperand, register) || Objects.equals(this.secondOperand, register) || Objects.equals(this.thirdOperand, register);
    }

    public void changeOperand(String register, String replacement) {
        if (Objects.equals(this.firstOperand, register)) {
            this.firstOperand = replacement;
        }
        if (Objects.equals(this.secondOperand, register)) {
            this.secondOperand = replacement;
        }
        if (Objects.equals(this.thirdOperand, register)) {
            this.thirdOperand = replacement;
        }
    }

    public boolean containsGlobalRegisters() {
        return RegisterUtil.isGlobalRegister(this.firstOperand) || RegisterUtil.isGlobalRegister(this.secondOperand) || RegisterUtil.isGlobalRegister(this.thirdOperand);
    }

    public boolean isSubroutineCall() {
        return this.opCode == PUSHJ || this.opCode == PUSHGO;
    }

    public boolean isTwinSwapInstruction(Instruction instruction) {
        return  (this.getOpCode() == SET && instruction.getOpCode() == SET)
                && Objects.equals(this.firstOperand, instruction.getSecondOperand())
                && Objects.equals(this.secondOperand, instruction.getFirstOperand());
    }

    public boolean hasLabel(){
        return this.getLabel() != null;
    }

    public String extractBranchLabel() {
        if (!InstructionOpCode.isBranchOrJumpInstructionOpCode((InstructionOpCode) this.opCode)) {
            throw new RuntimeException("Expected branch or jump instruction but this was not the case: " + this);
        }
        if (this.opCode == JMP) {
            return this.firstOperand;
        }
        return this.secondOperand;
    }
}
