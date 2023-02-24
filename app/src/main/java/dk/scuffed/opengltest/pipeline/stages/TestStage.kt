package dk.scuffed.opengltest.pipeline.stages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.opengltest.R
import dk.scuffed.opengltest.gl.*
import dk.scuffed.opengltest.pipeline.*
import java.nio.ByteBuffer
import java.nio.IntBuffer


internal class TestStage(private val context: Context, private val inputFrameBufferInfo: FramebufferInfo, private val pipeline: Pipeline): GLOutputStage(context, R.raw.vertex_shader, R.raw.shaderfun, pipeline) {
    // TODO Get this from the view.
    private val resolution: Size = Size(1080, 1920)
    lateinit var bitmap: Bitmap
    lateinit var buffer : ByteBuffer
    private lateinit var textureUnitPair: TextureUnitPair

    private var time = System.currentTimeMillis();
    init {
        setup()
        loadTexture()

    }
    override fun setupFramebufferInfo() {
        val resolution = Size(resolution.width, resolution.height)
        allocateFramebuffer(GLES20.GL_RGBA, resolution)

    }

    fun loadTexture(){

        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.noisetexture)
        val width = bitmap.getWidth()
        val height = bitmap.getHeight()

        val size: Int = bitmap.getRowBytes() * bitmap.getHeight()
        buffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(buffer)
        buffer.position(0)


        textureUnitPair = pipeline.allocateTextureUnit(this)
        glActiveTexture(textureUnitPair.textureUnit)

        textureHandle = glGenTexture()

        glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width,
            height,
            GLES20.GL_UNSIGNED_BYTE,
            buffer
        )
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        glBindTexture(GLES20.GL_TEXTURE_2D, 0)


    }
    var textureHandle : Int = 0

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)
        // Input framebuffer resolution
        val framebufferResolutionHandle = glGetUniformLocation(program, "framebuffer_resolution")
        glUniform2f(framebufferResolutionHandle, inputFrameBufferInfo.textureSize.width.toFloat(), inputFrameBufferInfo.textureSize.height.toFloat())
        // Input framebuffer
        val framebufferTextureHandle = glGetUniformLocation(program, "framebuffer")
        glUniform1i(framebufferTextureHandle, inputFrameBufferInfo.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFrameBufferInfo.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFrameBufferInfo.textureHandle)

        val noiseTextureHandle = glGetUniformLocation(program, "noise")
        glUniform1i(noiseTextureHandle, textureUnitPair.textureUnitIndex)
        glActiveTexture(textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        val timeHandle = glGetUniformLocation(program, "time")
        glUniform1f(timeHandle, (System.currentTimeMillis() - time)/1000f)
    }
}