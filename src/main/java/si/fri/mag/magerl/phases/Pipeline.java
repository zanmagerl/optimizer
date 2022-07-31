package si.fri.mag.magerl.phases;

import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.impl.CleaningPhaseImpl;
import si.fri.mag.magerl.phases.impl.PatternPhaseImpl;
import si.fri.mag.magerl.phases.impl.StandardLibraryPhaseImpl;
import si.fri.mag.magerl.phases.impl.SubroutineLabelingPhaseImpl;

import java.util.List;

public class Pipeline {

    private final List<Phase> phases = List.of(
            new StandardLibraryPhaseImpl(),
            new CleaningPhaseImpl(),
            new SubroutineLabelingPhaseImpl(),
            new PatternPhaseImpl()
    );

    public List<RawInstruction> run(List<RawInstruction> rawInstructions) {
        for (Phase phase : phases) {
            rawInstructions = phase.visit(rawInstructions);
        }
        return rawInstructions;
    }
}
