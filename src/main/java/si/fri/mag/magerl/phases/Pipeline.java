package si.fri.mag.magerl.phases;

import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.impl.*;

import java.util.List;

public class Pipeline {

    private final List<Phase> phases = List.of(
            new StandardLibraryPhaseImpl(),
            new CleaningPhaseImpl(),
            new SubroutineLabelingPhaseImpl(),
            new GraphConstructionPhase(),
            new RegisterUsagesPhaseImpl(),
            new PatternPhaseImpl()
    );

    public List<RawInstruction> run(List<RawInstruction> rawInstructions) {
        for (Phase phase : phases) {
            rawInstructions = phase.visit(rawInstructions);
        }
        return rawInstructions;
    }

    public Pipeline(List<RawInstruction> rawInstructions) {

    }
}
