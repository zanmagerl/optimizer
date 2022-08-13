package si.fri.mag.magerl.pipeline.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.Phase;
import si.fri.mag.magerl.pipeline.Pipeline;

import java.util.List;

@Slf4j
public class LinearPipelineImpl implements Pipeline {

    private static final Integer NUMBER_OF_RUNS = 1;

    @Override
    public List<RawInstruction> run(List<RawInstruction> rawInstructions) {
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            log.info("Run number: {}", i+1);
            for (Phase phase : phases) {
                rawInstructions = phase.visit(rawInstructions);
            }
        }
        return rawInstructions;
    }

}
