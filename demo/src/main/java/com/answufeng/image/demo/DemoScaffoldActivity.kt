package com.answufeng.image.demo

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * 子演示页基类：统一 Toolbar + 分节卡片。
 */
abstract class DemoScaffoldActivity : AppCompatActivity() {

    abstract fun demoTitle(): String

    /** 在 [activity_demo_scaffold] 的 content 容器内构建 UI。 */
    protected abstract fun LinearLayout.buildDemo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDemoScaffold(demoTitle()) {
            buildDemo()
        }
    }
}
