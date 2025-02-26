package org.ogzkesk.marvel.test.app.controller

import org.ogzkesk.marvel.test.app.model.Distance

class TargetPredictor(
    private val sampleSize: Int = 5,
) {
    private val samples = ArrayDeque<Distance>(sampleSize)

    fun addSample(distance: Distance) {
        if (samples.size >= sampleSize) {
            samples.removeFirst()
        }
        samples.addLast(distance)
    }

    fun predict(distance: Distance): Distance {
        for(sample in samples){
            sample.dx
            sample.dy
        }

        return Distance(0,0,0,0,0,0)
    }

    fun reset(){
        samples.clear()
    }
}