package me.champeau.speakertime

import android.app.Presentation
import android.os.CountDownTimer
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

@CompileStatic
class PresenterTimer extends CountDownTimer {

    private static final long DEFAULT_TIMER = 90*60*1000

    private final List<Closure> onTickListeners = []
    private final List<Closure> onFinishListeners = []

    PresenterTimer() {
        super(DEFAULT_TIMER, 1000)
    }

    @Override
    void onTick(long millisUntilFinished) {
        onTickListeners*.call(millisUntilFinished)
    }

    @Override
    void onFinish() {
        onFinishListeners*.call()
    }

    PresenterTimer onTick(@ClosureParams(value=SimpleType, options="long")Closure cl) {
        onTickListeners.add(cl)

        this
    }

    PresenterTimer onFinish(Closure cl) {
        onTickListeners.add(cl)

        this
    }

}