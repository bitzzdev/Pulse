package io.github.pulse.util;

public final class TimeUtil {
    public static final long NANOS_PER_MILLI = 1_000_000L;
    public static final long NANOS_PER_SECOND = 1_000_000_000L;
    public static final long MILLIS_PER_SECOND = 1_000L;
    public static final int TICKS_PER_SECOND = 20;

    private TimeUtil() {
    }

    public static long ticksToNanos(int ticks) {
        return (long) ticks * Constants.NANOS_PER_TICK;
    }

    public static long ticksToMillis(int ticks) {
        return (long) ticks * 50L;
    }

    public static int millisToTicks(long millis) {
        return (int) (millis / 50L);
    }

    public static int nanosToTicks(long nanos) {
        return (int) (nanos / Constants.NANOS_PER_TICK);
    }

    public static long nanosToMillis(long nanos) {
        return nanos / NANOS_PER_MILLI;
    }

    public static long millisToNanos(long millis) {
        return millis * NANOS_PER_MILLI;
    }

    public static double ticksToSeconds(int ticks) {
        return (double) ticks / (double) TICKS_PER_SECOND;
    }

    public static int secondsToTicks(double seconds) {
        return (int) Math.round(seconds * TICKS_PER_SECOND);
    }

    public static double nanosToSeconds(long nanos) {
        return (double) nanos / (double) NANOS_PER_SECOND;
    }

    public static int frameTimeToFps(long frameTimeNanos) {
        if (frameTimeNanos <= 0) {
            return 0;
        }
        return (int) Math.round((double) NANOS_PER_SECOND / (double) frameTimeNanos);
    }

    public static long fpsToFrameTime(int fps) {
        if (fps <= 0) {
            return 0;
        }
        return NANOS_PER_SECOND / fps;
    }
}
