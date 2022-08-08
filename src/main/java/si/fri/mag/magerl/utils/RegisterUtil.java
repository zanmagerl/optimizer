package si.fri.mag.magerl.utils;

import lombok.experimental.UtilityClass;
import si.fri.mag.magerl.models.RawInstruction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class RegisterUtil {

    public Map<String, Map<RawInstruction, List<String>>> availableRegisters = new HashMap<>();

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
}
