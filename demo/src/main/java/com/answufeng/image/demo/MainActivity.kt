package com.answufeng.image.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))

        findViewById<RecyclerView>(R.id.catalogList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = DemoCatalogAdapter(DemoCatalog.rows()) { entry ->
                startActivity(entry.intent(this@MainActivity))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_demo_playbook -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.demo_playbook_title)
                    .setMessage(R.string.demo_playbook_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                true
            }
            R.id.action_theme -> {
                val current = AppCompatDelegate.getDefaultNightMode()
                val next = if (current == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.MODE_NIGHT_NO
                } else {
                    AppCompatDelegate.MODE_NIGHT_YES
                }
                AppCompatDelegate.setDefaultNightMode(next)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
