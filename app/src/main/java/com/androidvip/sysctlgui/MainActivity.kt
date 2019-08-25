package com.androidvip.sysctlgui

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mainParamsList.setOnClickListener {
            Intent(this, KernelParamsListActivity::class.java).apply {
                startActivity(this)
            }
        }

        mainParamBrowser.setOnClickListener {
            Intent(this, KernelParamBrowserActivity::class.java).apply {
                startActivity(this)
            }
        }

        mainReadFromFile.setOnClickListener {
            Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
        }

        mainAppDescription.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.action_settings -> {
                Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
            }

            R.id.action_exit -> {
                Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
            }
        }

        return true
    }
}
