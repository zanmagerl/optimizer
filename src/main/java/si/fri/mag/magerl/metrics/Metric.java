package si.fri.mag.magerl.metrics;

import si.fri.mag.magerl.models.RawInstruction;

import java.util.List;

public interface Metric {

    Integer value(List<RawInstruction> rawInstructions);
}
