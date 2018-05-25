package com.darkzek.ChickenBot;

public class ExceptionTracker implements Thread.UncaughtExceptionHandler {

    public void uncaughtException(Thread t, Throwable e) {
        handle(e);
    }

    public void handle(Throwable throwable) {
        try {
            // insert your e-mail code here
            System.out.println("Error!\n" + throwable.fillInStackTrace());
            ChickenBot.TellMe("Hey man, we just blew a fuse!\n\nStacktrace:```" + throwable.fillInStackTrace() + "```");
        } catch (Throwable t) {
            // don't let the exception get thrown out, will cause infinite looping!
        }
    }

    public static void registerExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionTracker());
        System.setProperty("sun.awt.exception.handler", ExceptionTracker.class.getName());
    }
}
