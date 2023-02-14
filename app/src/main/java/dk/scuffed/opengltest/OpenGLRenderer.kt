package dk.scuffed.opengltest

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import dk.scuffed.opengltest.gl.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import com.google.common.io.ByteStreams;


class OpenGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val COORDS_PER_VERTEX = 3

    private var program = 0
    private val resolution = floatArrayOf(0.0f, 0.0f)
    private val squareCoords = floatArrayOf(
        -1.0f, 1.0f, 0.0f,
        -1.0f, -1.0f, 0.0f,
         1.0f, -1.0f, 0.0f,
         1.0f, 1.0f,0.0f
    )
    private var startTime: Long = 0
    private val vertexCount: Int = squareCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex


    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

    // initialize vertex byte buffer for shape coordinates
    private val vertexBuffer: FloatBuffer =
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(squareCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(squareCoords)
                position(0)
            }
        }

    // initialize byte buffer for the draw list
    private val drawListBuffer: ShortBuffer =
        // (# of coordinate values * 2 bytes per short)
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }


    private lateinit var vertexShaderCode: String
    private lateinit var fragmentShaderCode: String


    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        val vertexShaderStream =
            context.resources.openRawResource(R.raw.vertex_shader)
        vertexShaderCode = String(ByteStreams.toByteArray(vertexShaderStream))
        vertexShaderStream.close()

        val fragmentShaderStream =
            context.resources.openRawResource(R.raw.fragment_shader)
        fragmentShaderCode = String(ByteStreams.toByteArray(fragmentShaderStream))
        fragmentShaderStream.close()

        glDisable(GLES20.GL_BLEND)
        glDisable(GLES20.GL_CULL_FACE)
        glDisable(GLES20.GL_DEPTH_TEST)
        glClearColor(1.0f, 0.0f, 0.0f, 1.0f)

        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        val glCreateProgram: Int = glCreateProgram()
        glAttachShader(glCreateProgram, vertexShader)
        glAttachShader(glCreateProgram, fragmentShader)
        glLinkProgram(glCreateProgram)
        program = glCreateProgram
        startTime = System.nanoTime()
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        resolution[0] = width.toFloat()
        resolution[1] = height.toFloat()
    }

    override fun onDrawFrame(p0: GL10?) {
        glClear(GLES20.GL_COLOR_BUFFER_BIT)
        glUseProgram(program)

        val positionHandle = glGetAttribLocation(program, "position")
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)

        val delta = (System.nanoTime() - startTime).toFloat() / 1000000000.0f
        glUniform1f(
            glGetUniformLocation(program, "time"),
            delta
        )

        val resolutionHandle = glGetUniformLocation(program, "resolution")
        glUniform2f(resolutionHandle, resolution[0], resolution[1])
        glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
        glDisableVertexAttribArray(positionHandle)
    }
}
