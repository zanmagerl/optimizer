package si.fri.mag.magerl.patterns;

import si.fri.mag.magerl.models.RawInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface Pattern {

    List<RawInstruction> usePatternOnce(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider);

    default List<RawInstruction> usePattern(List<RawInstruction> rawInstructions) {
        return usePattern(rawInstructions, x -> true);
    }

    default List<RawInstruction> usePattern(List<RawInstruction> rawInstructions, Predicate<Integer> optimizationDecider) {
        List<RawInstruction> iterInstructions = new ArrayList<>(rawInstructions);
        int lastSize;
        do {
            lastSize = iterInstructions.size();
            iterInstructions = usePatternOnce(iterInstructions, optimizationDecider);
        } while(iterInstructions.size() != lastSize);
        return iterInstructions;
    }

    List<List<RawInstruction>> branchPattern(List<RawInstruction> rawInstructions);
}
