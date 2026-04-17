package com.answufeng.image.demo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.image.loadCircle
import com.answufeng.image.loadImage

class ErrorHandlingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_handling)
        title = "Error Handling & Fallback"

        val validUrl = "https://picsum.photos/200/200"
        val invalidUrl = "https://invalid.example.com/not_found.jpg"

        val tvValidStatus = findViewById<TextView>(R.id.tvValidStatus)
        val ivValid = findViewById<ImageView>(R.id.ivValid)
        ivValid.loadImage(validUrl) {
            crossfade(300)
            listener(
                onStart = { tvValidStatus.text = "Status: Loading..." },
                onSuccess = { tvValidStatus.text = "Status: Success!" },
                onError = { tvValidStatus.text = "Status: Failed - ${it.throwable.message}" }
            )
        }

        val tvErrorStatus = findViewById<TextView>(R.id.tvErrorStatus)
        val ivError = findViewById<ImageView>(R.id.ivError)
        ivError.loadImage(invalidUrl) {
            error(ColorDrawable(Color.parseColor("#FFFFCDD2")))
            listener(
                onStart = { tvErrorStatus.text = "Status: Loading..." },
                onSuccess = { tvErrorStatus.text = "Status: Success!" },
                onError = { tvErrorStatus.text = "Status: Failed (expected)" }
            )
        }

        val ivFallback = findViewById<ImageView>(R.id.ivFallback)
        ivFallback.loadImage(null) {
            fallback(ColorDrawable(Color.parseColor("#FFBDBDBD")))
        }

        val ivCircleError = findViewById<ImageView>(R.id.ivCircleError)
        ivCircleError.loadImage(invalidUrl) {
            circle()
            transform(com.answufeng.image.BorderTransformation(4f, Color.RED, circle = true))
        }
    }
}
