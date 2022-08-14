package si.fri.mag.magerl.metrics.impl;

import si.fri.mag.magerl.metrics.Metric;
import si.fri.mag.magerl.models.RawInstruction;

import java.util.List;

public class AbsoluteMetric implements Metric {

    @Override
    public Integer value(List<RawInstruction> rawInstructions) {
        if (rawInstructions.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return rawInstructions.size();
    }
}
