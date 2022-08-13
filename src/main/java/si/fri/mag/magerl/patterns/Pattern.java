package si.fri.mag.magerl.patterns;

import si.fri.mag.magerl.models.RawInstruction;

import java.util.ArrayList;
import java.util.List;

public interface Pattern {

    List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions);

    default List<RawInstruction> usePattern(List<RawInstruction> rawInstructions) {
        List<RawInstruction> iterInstructions = new ArrayList<>(rawInstructions);
        int lastSize;
        do {
            lastSize = iterInstructions.size();
            iterInstructions = usePatternOnce(iterInstructions);
        } while(iterInstructions.size() != lastSize);
        return iterInstructions;
    }

    List<RawInstruction> branchPattern(List<RawInstruction> rawInstructions);
}
