package me.champeau.speakertime.support

import groovy.transform.CompileStatic

@CompileStatic
abstract class MessageConstants {
    public static final String MSG_TIME_LEFT = "speakertime/timeleft"
    public static final String STOP_WEAR_ACTIVITY = "speakertime/stopWearActivity"
    public static final String START_HANDHELD_ACTIVITY = "speakertime/startHandheldActivity"
    public static final byte[] EMPTY_MESSAGE = new byte[0]
}