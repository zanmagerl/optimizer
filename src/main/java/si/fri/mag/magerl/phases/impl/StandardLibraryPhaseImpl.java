package si.fri.mag.magerl.phases.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.phases.Phase;
import si.fri.mag.magerl.utils.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class StandardLibraryPhaseImpl implements Phase {

    private static boolean wasAlreadyRun = false;
    private static final String FILE_EXTENSION = ".mms";
    @AllArgsConstructor
    enum Subprogram {
        MAIN("main"),
        PRINT_CHAR("print_char")
        ;
        private final String name;
    }

    @Override
    public List<RawInstruction> visit(List<RawInstruction> rawInstructions) {
        if (wasAlreadyRun) {
            return rawInstructions;
        }
        wasAlreadyRun = true;
        List<RawInstruction> withPrintChar = replacePrintChar(rawInstructions);
        return addMain(withPrintChar);
    }

    private List<RawInstruction> replacePrintChar(List<RawInstruction> rawInstructions) {
        List<RawInstruction> replacedRawInstructions = new ArrayList<>();
        boolean isPrintChar = false;
        for (RawInstruction rawInstruction : rawInstructions) {
            if (rawInstruction.getRawInstruction().contains("print_char\tIS @")) {
                isPrintChar = true;
                replacedRawInstructions.addAll(loadSubprogram(Subprogram.PRINT_CHAR));
            } else if (!isPrintChar){
                //O2 optimizations use this
                if (rawInstruction.getRawInstruction().contains("putchar")) {
                    if (!Objects.equals(rawInstruction.getInstruction().getSecondOperand(), "putchar")) {
                        throw new RuntimeException("Instruction contains putchar, but is not as second operand" + rawInstruction.getRawInstruction());
                    }
                    rawInstruction.getInstruction().setSecondOperand("print_char");
                }
                replacedRawInstructions.add(rawInstruction);
            }

            if (isPrintChar && rawInstruction.getRawInstruction().contains("POP")) {
                isPrintChar = false;
            }
        }
        return replacedRawInstructions;
    }

    private List<RawInstruction> addMain(List<RawInstruction> rawInstructions) {
        rawInstructions.addAll(loadSubprogram(Subprogram.MAIN));
        return rawInstructions;
    }

    private List<RawInstruction> loadSubprogram(Subprogram subprogram){
        log.info(subprogram.name + FILE_EXTENSION);
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(subprogram.name + FILE_EXTENSION)) {
            return FileUtil.readProgram(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read subprogram " + subprogram, e);
        }
    }
}
