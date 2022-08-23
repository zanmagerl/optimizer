package si.fri.mag.magerl.utils;

import lombok.experimental.UtilityClass;
import si.fri.mag.magerl.models.RawInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class RegisterUtil {

    public Map<String, Map<RawInstruction, List<String>>> availableRegisters = new HashMap<>();

    public List<Integer> usedGlobalRegisters = new ArrayList<>(List.of(
            255, 254, 253
    ));

    public static boolean isRegister(String register) {
        return register != null && register.startsWith("$") && extractRegister(register) >= 0 && extractRegister(register) < 256;
    }

    public static boolean isLocalRegister(String register) {
        if (!isRegister(register)) {
            return false;
        }
        return extractRegister(register) >= 0 && extractRegister(register) < 64;
    }

    public static boolean isGlobalRegister(String register) {
        if (!isRegister(register)) {
            return false;
        }
        return extractRegister(register) >= 64 && extractRegister(register) < 256;
    }

    public static Integer extractRegister(String register) {
        return Integer.parseInt(register.substring(1));
    }

    public static boolean isUnusedAfterInstruction(String register, RawInstruction rawInstruction) {
        for (RawInstruction nextInstruction : rawInstruction.getPossibleNextInstructions()) {
            if (!nextInstruction.getUnusedRegisters().contains(register)) {
                return false;
            }
        }
        return true;
    }

    public String getAvailableGlobalRegister() {
        Integer availableRegister = usedGlobalRegisters.stream().min(Integer::compareTo).get() - 1;
        usedGlobalRegisters.add(availableRegister);
        return "$" + availableRegister;
    }
}
