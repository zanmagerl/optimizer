package si.fri.mag.magerl;

import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

public class CommandLineProcessor implements Callable<Void> {

    @Option(names = {"-i", "--input-file"}, description = "Provide input file for optimization")
    String inputProgram;

    @Option(names = {"-o", "--output-file"}, description = "Provide target file name after optimization")
    String outputProgram;

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Void call() throws Exception {
        return null;
    }
}
