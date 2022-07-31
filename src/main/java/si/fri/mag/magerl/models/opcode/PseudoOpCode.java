package si.fri.mag.magerl.models.opcode;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public enum PseudoOpCode implements OpCode {

    BYTE,
    LOC,
    IS
    ;

    public static Optional<PseudoOpCode> from(String opCode) {
        return Arrays.stream(PseudoOpCode.values()).filter(o -> o.name().equals(opCode)).findFirst();
    }
}
