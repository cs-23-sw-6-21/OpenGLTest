package dk.scuffed.opengltest

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(OpenGLView(this))
    }
}