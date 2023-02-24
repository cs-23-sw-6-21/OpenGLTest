package dk.scuffed.opengltest.pipeline

import android.content.Context
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.opengltest.gl.*
import dk.scuffed.opengltest.pipeline.stages.*
import dk.scuffed.opengltest.pipeline.stages.CameraXStage
import dk.scuffed.opengltest.pipeline.stages.DrawFramebufferStage
import dk.scuffed.opengltest.pipeline.stages.TestStage
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import dk.scuffed.opengltest.pipeline.stages.GaussianBlurStage
import dk.scuffed.opengltest.pipeline.stages.SobelStage

class Pipeline(context: Context) {

    private val cameraXStage: CameraXStage
    private val gaussianBlurXStage: GaussianBlurStage
    private val gaussianBlurYStage: GaussianBlurStage
    private val grayscaleStage: GrayscaleStage
    private val sobelStage: SobelStage
    private val drawFramebufferInfo: DrawFramebufferStage
    private val testStage: TestStage

    private var nextTextureUnit: Int = 0

    private val indexToTextureUnit = intArrayOf(
        GLES20.GL_TEXTURE0,
        GLES20.GL_TEXTURE1,
        GLES20.GL_TEXTURE2,
        GLES20.GL_TEXTURE3,
        GLES20.GL_TEXTURE4,
        GLES20.GL_TEXTURE5,
        GLES20.GL_TEXTURE6,
        GLES20.GL_TEXTURE7,
        GLES20.GL_TEXTURE8,
        GLES20.GL_TEXTURE9,
        GLES20.GL_TEXTURE10,
        GLES20.GL_TEXTURE11,
        GLES20.GL_TEXTURE12,
        GLES20.GL_TEXTURE13,
        GLES20.GL_TEXTURE14,
        GLES20.GL_TEXTURE15,
        GLES20.GL_TEXTURE16,
        GLES20.GL_TEXTURE17,
        GLES20.GL_TEXTURE18,
        GLES20.GL_TEXTURE19,
    )

    init {
        glDisable(GLES20.GL_BLEND)
        glDisable(GLES20.GL_CULL_FACE)
        glDisable(GLES20.GL_DEPTH_TEST)
        glClearColor(1.0f, 0.0f, 1.0f, 1.0f)

        cameraXStage = CameraXStage(
            context,
            this
        )

        gaussianBlurXStage = GaussianBlurStage(
            context,
            cameraXStage.frameBufferInfo,
            true,
            this
        )

        gaussianBlurYStage = GaussianBlurStage(
            context,
            gaussianBlurXStage.frameBufferInfo,
            false,
            this
        )

        grayscaleStage = GrayscaleStage(
            context,
            gaussianBlurYStage.frameBufferInfo,
            this
        )

        sobelStage = SobelStage(
            context,
            grayscaleStage.frameBufferInfo,
            this
        )

        drawFramebufferInfo = DrawFramebufferStage(
            context,
            sobelStage.frameBufferInfo,
            this
        )

        drawFramebufferInfo = DrawFramebufferStage(
            context,
            testStage.frameBufferInfo,
            this
        )
    }

    fun draw() {
        cameraXStage.draw()
        gaussianBlurXStage.draw()
        gaussianBlurYStage.draw()
        grayscaleStage.draw()
        sobelStage.draw()
        drawFramebufferInfo.draw()
    }

    internal fun allocateFramebuffer(stage: GLOutputStage, textureFormat: Int, width: Int, height: Int): FramebufferInfo {
        val fboHandle = glGenFramebuffer()

        val textureHandle = glGenTexture()

        val textureUnitPair = allocateTextureUnit(stage)
        glActiveTexture(textureUnitPair.textureUnit)

        glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, GLES20.GL_UNSIGNED_BYTE, null)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        glBindFramebuffer(fboHandle)
        glFramebufferTexture2D(GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureHandle)

        return FramebufferInfo(fboHandle, textureHandle, textureUnitPair, GLES20.GL_RGBA, Size(width, height))
    }

    internal fun allocateTextureUnit(stage: GLOutputStage): TextureUnitPair {
        val textureUnitIndex = nextTextureUnit++;
        val textureUnit = indexToTextureUnit[textureUnitIndex]
        return TextureUnitPair(textureUnit, textureUnitIndex)
    }
}