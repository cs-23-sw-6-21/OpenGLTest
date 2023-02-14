package dk.scuffed.opengltest

import android.content.Context
import android.opengl.GLSurfaceView

class OpenGLView(context: Context) : GLSurfaceView(context) {

    private val renderer: OpenGLRenderer

    init {
        setEGLContextClientVersion(2)

        renderer = OpenGLRenderer(context)

        setRenderer(renderer)
    }
}