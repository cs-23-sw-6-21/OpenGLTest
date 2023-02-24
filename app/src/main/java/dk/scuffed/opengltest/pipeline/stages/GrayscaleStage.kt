package dk.scuffed.opengltest.pipeline.stages

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.opengltest.R
import dk.scuffed.opengltest.gl.glActiveTexture
import dk.scuffed.opengltest.gl.glBindTexture
import dk.scuffed.opengltest.gl.glGetUniformLocation
import dk.scuffed.opengltest.gl.glUniform1i
import dk.scuffed.opengltest.pipeline.FramebufferInfo
import dk.scuffed.opengltest.pipeline.GLOutputStage
import dk.scuffed.opengltest.pipeline.Pipeline

internal class GrayscaleStage(context: Context, private val inputFramebufferInfo: FramebufferInfo, pipeline: Pipeline) : GLOutputStage(context, R.raw.vertex_shader, R.raw.grayscale_shader, pipeline) {
    init {
        setup()
    }

    override fun setupFramebufferInfo() {
        allocateFramebuffer(GLES20.GL_RGBA, inputFramebufferInfo.textureSize)
    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        // We don't need the framebuffer resolution as it is the same as resolution :^)

        // Input framebuffer
        val framebufferTextureHandle = glGetUniformLocation(program, "framebuffer")
        glUniform1i(framebufferTextureHandle, inputFramebufferInfo.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFramebufferInfo.textureHandle)
    }
}