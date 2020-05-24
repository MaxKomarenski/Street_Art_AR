package com.nta.streetartar

import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3

class RotatingNode : Node(), Node.OnTapListener {

    private var rotationAnimation: ObjectAnimator? = null
    private var degreesPerSecond = 90.0f

    private var lastSpeedMultiplier = 1.0f

    private val animationDuration: Long
        get() = (1000 * 360 / (degreesPerSecond * speedMultiplier)).toLong()

    private val speedMultiplier: Float
        get() = 1.0f

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)

        if (rotationAnimation == null) {
            return
        }

        val speedMultiplier = speedMultiplier

        if (lastSpeedMultiplier == speedMultiplier) {
            return
        }

        if (speedMultiplier == 0.0f) {
            rotationAnimation!!.pause()
        } else {
            rotationAnimation!!.resume()

            val animatedFraction = rotationAnimation!!.animatedFraction
            rotationAnimation!!.duration = animationDuration
            rotationAnimation!!.setCurrentFraction(animatedFraction)
        }
        lastSpeedMultiplier = speedMultiplier
    }

    override fun onActivate() {
        startAnimation()
    }

    override fun onDeactivate() {
        stopAnimation()
    }

    private fun startAnimation() {
        if (rotationAnimation != null) {
            return
        }
        rotationAnimation = createAnimator()
        rotationAnimation!!.target = this
        rotationAnimation!!.duration = animationDuration
        rotationAnimation!!.start()
    }

    private fun stopAnimation() {
        if (rotationAnimation == null) {
            return
        }
        rotationAnimation!!.cancel()
        rotationAnimation = null
    }

    private fun createAnimator(): ObjectAnimator {
        val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0f)
        val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 120f)
        val orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 240f)
        val orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)

        val rotationAnimation = ObjectAnimator()
        rotationAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4)

        rotationAnimation.setPropertyName("localRotation")

        rotationAnimation.setEvaluator(QuaternionEvaluator())

        rotationAnimation.repeatCount = ObjectAnimator.INFINITE
        rotationAnimation.repeatMode = ObjectAnimator.RESTART
        rotationAnimation.interpolator = LinearInterpolator()
        rotationAnimation.setAutoCancel(true)

        return rotationAnimation
    }

    override fun onTap(p0: HitTestResult?, p1: MotionEvent?) {

    }


}