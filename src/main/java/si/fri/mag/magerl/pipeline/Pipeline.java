package si.fri.mag.magerl.pipeline;

import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.Phase;
import si.fri.mag.magerl.phases.impl.*;

import java.util.List;

public interface Pipeline {

    List<Phase> phases = List.of(
            new StandardLibraryPhaseImpl(),
            new CleaningPhaseImpl(),
            new SubroutineLabelingPhaseImpl(),
            new GraphConstructionPhaseImpl(),
            new RegisterUsagesPhaseImpl(),
            new PatternPhaseImpl(),
            new SwapPhaseImpl()
    );

    List<RawInstruction> run(List<RawInstruction> rawInstructions);
}
