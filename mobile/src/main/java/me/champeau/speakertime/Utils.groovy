package me.champeau.speakertime

import groovy.transform.CompileStatic

@CompileStatic
class Utils {
    public static String convertToDuration(long millis) {
        long seconds = (long) millis / 1000L
        long s = seconds % 60
        long m = ((long) (seconds / 60)) % 60
        long h = ((long) (seconds / 3600L)) % 24
        "$h:$m:$s"
    }
}