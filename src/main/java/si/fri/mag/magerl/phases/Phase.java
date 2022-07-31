package si.fri.mag.magerl.phases;

import si.fri.mag.magerl.models.RawInstruction;

import java.util.List;

public interface Phase {

    List<RawInstruction> visit(List<RawInstruction> rawInstructions);
}
