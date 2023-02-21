package dk.scuffed.opengltest.pipeline.stages

import android.content.Context
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.opengltest.R
import dk.scuffed.opengltest.gl.*
import dk.scuffed.opengltest.pipeline.*
import dk.scuffed.opengltest.pipeline.FramebufferInfo
import dk.scuffed.opengltest.pipeline.GLOutputStage
import dk.scuffed.opengltest.pipeline.TextureUnitPair

internal class DrawFramebufferStage(context: Context, private val inputFrameBufferInfo: FramebufferInfo, pipeline: Pipeline): GLOutputStage(context, R.raw.vertex_shader, R.raw.passthough_shader, pipeline) {

    // TODO Get this from the view.
    private val resolution: Size = Size(1080, 1920)

    init {
        setup()
    }

    override fun setupFramebufferInfo() {
        // Set framebuffer to screen
        frameBufferInfo = FramebufferInfo(0, 0, TextureUnitPair(0, 0), 0, resolution)
    }

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
    }
}