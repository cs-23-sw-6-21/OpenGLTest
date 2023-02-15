package dk.scuffed.opengltest

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.Surface
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

    private lateinit var surfaceTexture: SurfaceTexture

    private lateinit var camera: Camera

    private val cameraResolution = floatArrayOf(0.0f, 0.0f)

    private val textures: IntArray = intArrayOf(0)

    private val uniforms = intArrayOf(
        GLES20.GL_TEXTURE0,
        GLES20.GL_TEXTURE1,
        GLES20.GL_TEXTURE2,
        GLES20.GL_TEXTURE3,
        GLES20.GL_TEXTURE4,
        GLES20.GL_TEXTURE5,
    )


    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        var cameraId = 0
        for (i in 0 until Camera.getNumberOfCameras()) {
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i
                break;
            }
        }

        camera = Camera.open(cameraId)

        camera.setDisplayOrientation(Surface.ROTATION_0)

        val parameters = camera.parameters
        parameters.setRotation(Surface.ROTATION_0)
        camera.parameters = parameters




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
        glClearColor(1.0f, 0.0f, 1.0f, 1.0f)

        glGenTextures(textures.size, textures, 0)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        surfaceTexture = SurfaceTexture(textures[0])
        camera.setPreviewTexture(surfaceTexture)
        camera.startPreview()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)
        startTime = System.nanoTime()
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        resolution[0] = width.toFloat()
        resolution[1] = height.toFloat()

        val previewSize = camera.parameters.previewSize
        cameraResolution[0] = previewSize.width.toFloat()
        cameraResolution[1] = previewSize.height.toFloat()
    }

    override fun onDrawFrame(p0: GL10?) {
        glClear(GLES20.GL_COLOR_BUFFER_BIT)

        surfaceTexture.updateTexImage()

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


        val camTextureHandle = glGetUniformLocation(program, "cam")
        glUniform1i(camTextureHandle, 0)
        glActiveTexture(uniforms[0])
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])

        val camResolutionHandle = glGetUniformLocation(program, "camResolution")
        glUniform2f(camResolutionHandle, cameraResolution[0], cameraResolution[1])

        glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
        glDisableVertexAttribArray(positionHandle)
    }
}
