package si.fri.mag.magerl.pipeline.impl;

import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.patterns.impl.UnusedStoreInstructionPattern;
import si.fri.mag.magerl.phases.Phase;
import si.fri.mag.magerl.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.List;

import static si.fri.mag.magerl.config.BranchingConfig.NUMBER_OF_RUNS;

@Slf4j
public class LinearPipelineImpl implements Pipeline {
    @Override
    public List<List<RawInstruction>> run(List<RawInstruction> rawInstructions) {
        List<List<RawInstruction>> programs = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            log.info("Run number: {}", i+1);
            for (Phase phase : phases) {
                if (phase instanceof UnusedStoreInstructionPattern && i == 0) {
                    continue;
                }
                rawInstructions = phase.visit(rawInstructions);
            }
            programs.add(rawInstructions);
        }
        return programs;
    }

}
