package com.androidvip.sysctlgui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidvip.sysctlgui.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        private const val OPEN_FILE_REQUEST_CODE: Int = 1
    }

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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            startActivityForResult(intent, OPEN_FILE_REQUEST_CODE)
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
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            R.id.action_exit -> {
                RootUtils.finishProcess()
                moveTaskToBack(true)
                finish()
            }
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        RootUtils.finishProcess()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            OPEN_FILE_REQUEST_CODE -> {

                if (resultCode != Activity.RESULT_OK) {
                    return
                }

                data?.data?.let { uri ->

                    // check if mime is json
                    if (uri.lastPathSegment?.contains(".json")?.not() ?: run { false }) {
                        Toast.makeText(this, getString(R.string.open_file_failed), Toast.LENGTH_LONG).show()
                        return
                    }

                    val successfulParams: MutableList<KernelParameter> = mutableListOf()

                    GlobalScope.launch(Dispatchers.IO) {
                        KernelParamUtils(this@MainActivity)
                            .paramsFromUri(uri)
                            .forEach { kernelParameter: KernelParameter ->
                                // apply the param to check if valid
                                KernelParamUtils(this@MainActivity).applyParam(
                                    kernelParameter,
                                    object : KernelParamUtils.KernelParamApply {
                                        override fun onEmptyValue() {
                                        }

                                        override fun onFeedBack(feedback: String) {
                                        }

                                        override fun onCustomApply(kernelParam: KernelParameter) {
                                        }

                                        override fun onSuccess() {
                                            successfulParams.add(kernelParameter)
                                        }
                                    }, false
                                )
                            }

                        val oldParams = Prefs.removeAllParams(this@MainActivity)
                        if (Prefs.putParams(successfulParams, this@MainActivity)) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, getString(R.string.done), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Prefs.putParams(oldParams, this@MainActivity)
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, getString(R.string.restore_parameters), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
