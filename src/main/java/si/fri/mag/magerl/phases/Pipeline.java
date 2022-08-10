package si.fri.mag.magerl.phases;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.impl.*;

import java.util.List;

@Slf4j
public class Pipeline {

    private static final Integer NUMBER_OF_RUNS = 2;

    private final List<Phase> phases = List.of(
            new StandardLibraryPhaseImpl(),
            new CleaningPhaseImpl(),
            new SubroutineLabelingPhaseImpl(),
            new GraphConstructionPhase(),
            new RegisterUsagesPhaseImpl(),
            new PatternPhaseImpl(),
            new SwapPhaseImpl()
    );

    public List<RawInstruction> run(List<RawInstruction> rawInstructions) {
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            log.info("Run number: {}", i+1);
            for (Phase phase : phases) {
                rawInstructions = phase.visit(rawInstructions);
            }
        }
        return rawInstructions;
    }

    public Pipeline(List<RawInstruction> rawInstructions) {

    }
}
