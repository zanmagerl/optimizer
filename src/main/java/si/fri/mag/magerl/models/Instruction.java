package si.fri.mag.magerl.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.opcode.InstructionOpCode;
import si.fri.mag.magerl.models.opcode.OpCode;
import si.fri.mag.magerl.models.opcode.PseudoOpCode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Data
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

    private boolean hasLabel(List<String> parts) {
        return InstructionOpCode.from(parts.get(0)).isEmpty() && PseudoOpCode.from(parts.get(0)).isEmpty();
    }
}
