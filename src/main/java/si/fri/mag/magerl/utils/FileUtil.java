package si.fri.mag.magerl.utils;

import si.fri.mag.magerl.models.RawInstruction;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {

    public static List<RawInstruction> readProgram(InputStream inputStream){
            return new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .filter(line -> line.length() > 0)
                    .map(RawInstruction::new)
                    .collect(Collectors.toList());
    }

    public static void writeProgram(List<String> instructions, String path) {
        File file = new File(path);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (String instruction : instructions) {
                bufferedWriter.write(instruction);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to write program to file", e);
        }
    }

    public static String getTargetDirectory(){
        return new File("target").getAbsolutePath();
    }

    public static String extendPathWithFile(String path, String file) {
        return Paths.get(path, file).toString();
    }
}
