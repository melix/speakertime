package me.champeau.speakertime

import android.os.CountDownTimer
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

@CompileStatic
class PresenterTimer extends CountDownTimer {

    private final List<Closure> onTickListeners = []
    private final List<Closure> onFinishListeners = []

    PresenterTimer(long duration) {
        super(duration, 1000)
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