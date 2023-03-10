package dk.scuffed.opengltest

import android.content.Context
import com.google.common.io.ByteStreams

@Suppress("UnstableApiUsage")
fun readRawResource(context: Context, resourceId: Int): String {
    val stream =
        context.resources.openRawResource(resourceId)
    val string = String(ByteStreams.toByteArray(stream))
    stream.close()

    return string
}
