package dk.scuffed.opengltest

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.core.AspectRatio.RATIO_16_9
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.io.ByteStreams
import dk.scuffed.opengltest.gl.*
import java.nio.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


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

    private val gaussianKernelSize: Int = 5

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


    // https://www.rastergrid.com/blog/2010/09/efficient-gaussian-blur-with-linear-sampling/
    private val gaussianOffsets = floatArrayOf(0.0f, 1.3846153846f, 3.2307692308f)
    private val gaussianWeights = floatArrayOf(0.2270270270f, 0.3162162162f / 2.0f, 0.0702702703f / 2.0f)


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
        glClearColor(1.0f, 0.0f, 1.0f, 1.0f)

        glGenTextures(textures.size, textures, 0)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)


        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)
        startTime = System.nanoTime()

        surfaceTexture = SurfaceTexture(textures[0])
        surfaceTexture.setDefaultBufferSize(1920, 1080)

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val handler = android.os.Handler(context.mainLooper)

        handler.post { run {
            val preview : Preview = Preview.Builder()
                .setTargetAspectRatio(RATIO_16_9)
                .build()

            preview.setSurfaceProvider(object : SurfaceProvider {
                override fun onSurfaceRequested(request: SurfaceRequest) {
                    val surfaceNew = Surface(surfaceTexture)
                    request.provideSurface(surfaceNew, ContextCompat.getMainExecutor(context), { result: SurfaceRequest.Result -> SurfaceRequest.Result.RESULT_SURFACE_USED_SUCCESSFULLY})
                }
            })

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = cameraProviderFuture.get()
            camera = cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview)
            cameraResolution[0] = preview.resolutionInfo!!.resolution.width.toFloat()
            cameraResolution[1] = preview.resolutionInfo!!.resolution.height.toFloat()
            Log.i("TEST", "Width: ${cameraResolution[0]}, Height: ${cameraResolution[1]}")
        } }
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        resolution[0] = width.toFloat()
        resolution[1] = height.toFloat()
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


        val gaussianKernelSizeHandle = glGetUniformLocation(program, "gaussianKernelSize")
        glUniform1i(gaussianKernelSizeHandle, 3)

        val gaussianOffsetsHandle = glGetUniformLocation(program, "gaussianOffsets")
        glUniform1fv(gaussianOffsetsHandle, gaussianOffsets.size, gaussianOffsets, 0)

        val gaussianWeightsHandle = glGetUniformLocation(program, "gaussianWeights")
        glUniform1fv(gaussianWeightsHandle, gaussianWeights.size, gaussianWeights, 0)

        glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
        glDisableVertexAttribArray(positionHandle)
    }
}
