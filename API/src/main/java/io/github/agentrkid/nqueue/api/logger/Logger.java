package io.github.agentrkid.nqueue.api.logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static void log(String message) {
        System.out.println("[nQueue] (" + DATE_FORMAT.format(Calendar.getInstance().getTime()) + ") " + message);
    }

}
