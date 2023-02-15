package dk.scuffed.opengltest.gl

import android.opengl.GLES20
import android.util.Log
import java.nio.Buffer


fun loadShader(type: Int, shaderCode: String): Int {
    val shader = glCreateShader(type)
    glShaderSource(shader, shaderCode)
    glCompileShader(shader)
    return shader
}

fun glEnable(i: Int) {
    GLES20.glEnable(i)
    logErrorIfAny("glEnable")
}

fun glDisable(i: Int) {
    GLES20.glDisable(i)
    logErrorIfAny("glDisable")
}

fun glClear(flag: Int) {
    GLES20.glClear(flag)
    logErrorIfAny("glClear")
}

fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
    GLES20.glClearColor(red, green, blue, alpha)
    logErrorIfAny("glClearColor")
}

fun glViewport(x: Int, y: Int, width: Int, height: Int) {
    GLES20.glViewport(x, y, width, height)
    logErrorIfAny("glViewport")
}

fun glCreateShader(i: Int): Int {
    val shader = GLES20.glCreateShader(i)
    logErrorIfAny("glCreateShader")
    return shader
}

fun glShaderSource(shader: Int, shaderCode: String) {
    GLES20.glShaderSource(shader, shaderCode)
    logErrorIfAny("glShaderSource")
}

fun glCompileShader(shader: Int) {
    GLES20.glCompileShader(shader)
    logErrorIfAny("glCompileShader")

    val compiled = IntArray(1)
    GLES20.glGetShaderiv(
        shader,
        GLES20.GL_COMPILE_STATUS,
        compiled,
        0
    )

    if (compiled[0] == GLES20.GL_FALSE) {
        throw Exception(GLES20.glGetShaderInfoLog(shader))
    }
}

fun glCreateProgram(): Int {
    val program = GLES20.glCreateProgram()
    logErrorIfAny("glCreateProgram")
    return program
}

fun glAttachShader(program: Int, shader: Int) {
    GLES20.glAttachShader(program, shader)
    logErrorIfAny("glAttachShader")
}

fun glLinkProgram(program: Int) {
    GLES20.glLinkProgram(program)
    logErrorIfAny("glLinkProgram")

    val linkStatus = IntArray(1)
    GLES20.glGetProgramiv(
        program,
        GLES20.GL_LINK_STATUS,
        linkStatus,
        0
    )

    if (linkStatus[0] == GLES20.GL_FALSE) {
        throw Exception(GLES20.glGetProgramInfoLog(program))
    }
}

fun glUseProgram(program: Int) {
    GLES20.glUseProgram(program)
    logErrorIfAny("glUseProgram")
}

fun glGetAttribLocation(program: Int, attribName: String): Int {
    val location = GLES20.glGetAttribLocation(program, attribName)
    logErrorIfAny("glGetAttribLocation")
    return location
}

// https://docs.gl/es2/glEnableVertexAttribArray
fun glEnableVertexAttribArray(index: Int) {
    GLES20.glEnableVertexAttribArray(index)
    logErrorIfAny("glEnableVertexAttribArray")
}

// https://docs.gl/es2/glEnableVertexAttribArray
fun glDisableVertexAttribArray(index: Int) {
    GLES20.glDisableVertexAttribArray(index)
    logErrorIfAny("glDisableVertexAttribArray")
}

fun glVertexAttribPointer(
    index: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    ptr: Buffer
) {
    GLES20.glVertexAttribPointer(index, size, type, normalized, stride, ptr)
    logErrorIfAny("glVertexAttribPointer")
}

fun glGetUniformLocation(program: Int, uniformName: String): Int {
    val location = GLES20.glGetUniformLocation(program, uniformName)
    logErrorIfAny("glGetUniformLocation")
    return location
}

fun glUniform1f(location: Int, value: Float) {
    GLES20.glUniform1f(location, value)
    logErrorIfAny("glUniform1f")
}

fun glUniform2f(location: Int, x: Float, y: Float) {
    GLES20.glUniform2f(location, x, y)
    logErrorIfAny("glUniform2f")
}

fun glUniform4fv(location: Int, count: Int, buffer: FloatArray, offset: Int) {
    GLES20.glUniform4fv(location, count, buffer, offset)
    logErrorIfAny("glUniform4fv")
}

fun glDrawArrays(mode: Int, first: Int, count: Int) {
    GLES20.glDrawArrays(mode, first, count)
    logErrorIfAny("glDrawArrays")
}

fun glDrawElements(mode: Int, count: Int, type: Int, buffer: Buffer) {
    GLES20.glDrawElements(mode, count, type, buffer)
    logErrorIfAny("glDrawElements")
}

fun glGenTextures(n: Int, textures: IntArray, offset: Int) {
    GLES20.glGenTextures(n, textures, offset)
    logErrorIfAny("glGenTextures")
}

fun glBindTexture(target: Int, texture: Int)  {
    GLES20.glBindTexture(target, texture)
    logErrorIfAny("glBindTexture")
}

fun glTexParameteri(target: Int, pname: Int, param: Int) {
    GLES20.glTexParameteri(target, pname, param)
    logErrorIfAny("glTexParameteri")
}

fun glUniform1i(location: Int, target: Int) {
    GLES20.glUniform1i(location, target)
    logErrorIfAny("glUniform1i")
}

fun glActiveTexture(texture: Int) {
    GLES20.glActiveTexture(texture)
    logErrorIfAny("glActiveTexture")
}

private fun logErrorIfAny(funcname: String) {
    var error = GLES20.glGetError()
    while (error != 0) {
        Log.e("OpenGL", funcname + ": " + error + ": " + errorToString(error))
        error = GLES20.glGetError()
    }
}

private fun errorToString(error: Int): String {
    return when (error) {
        GLES20.GL_INVALID_ENUM -> "GL_INVALID_ENUM"
        GLES20.GL_INVALID_VALUE -> "GL_INVALID_VALUE"
        GLES20.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
        GLES20.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY"
        GLES20.GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION"
        else -> "UNKNOWN ERROR"
    }
}
