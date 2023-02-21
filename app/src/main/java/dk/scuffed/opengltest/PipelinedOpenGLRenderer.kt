package dk.scuffed.opengltest

import android.content.Context
import android.opengl.GLSurfaceView
import dk.scuffed.opengltest.pipeline.Pipeline
import java.nio.channels.Pipe
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PipelinedOpenGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var pipeline: Pipeline

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        pipeline = Pipeline(context)
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
    }

    override fun onDrawFrame(p0: GL10?) {
        pipeline.draw()
    }
}