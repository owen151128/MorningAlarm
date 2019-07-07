package kr.owens.alarm.util;

import java.io.IOException;

public class CommandUtil {

    private static final String COMMAND_START = "cmd.exe /c cd \"";
    private static final String COMMAND_END = "\" & start cmd.exe";

    public static boolean startCommandExe(String srcDir) {
        try {
            Runtime.getRuntime().exec(COMMAND_START + srcDir + COMMAND_END);
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }
}
