package si.fri.mag.magerl.patterns;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.RawInstruction;

import java.util.List;

public interface Pattern {

    List<RawInstruction> usePattern(List<RawInstruction> rawInstructions);
}
