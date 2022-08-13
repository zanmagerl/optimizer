package si.fri.mag.magerl;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import si.fri.mag.magerl.models.RawInstruction;
import si.fri.mag.magerl.pipeline.impl.LinearPipelineImpl;
import si.fri.mag.magerl.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class Main {
    
    public static void main(String[] args){
        log.info("Starting the optimizer!");
        CommandLineProcessor commandLineProcessor = new CommandLineProcessor();
        new CommandLine(commandLineProcessor).execute(args);

        String programName = Paths.get(commandLineProcessor.inputProgram).toString();

        File file = new File(programName);
        try {
            List<RawInstruction> rawInstructions = FileUtil.readProgram(new FileInputStream(file));
            log.info("Number of instruction: {}", rawInstructions.size());
            LinearPipelineImpl linearPipelineImpl = new LinearPipelineImpl();
            List<RawInstruction> optimizedCode = linearPipelineImpl.run(rawInstructions);
            log.info("Number of instructions in the optimized code: {}", optimizedCode.size());
            FileUtil.writeProgram(optimizedCode.stream().map(RawInstruction::getRawInstruction).toList(), FileUtil.extendPathWithFile(FileUtil.getTargetDirectory(), "output.mms"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
