package com.androidvip.sysctlgui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.Prefs
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.RootUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.StringBuilder
import java.lang.reflect.Type
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            this
        )
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
            Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
//                putExtra(Intent.EXTRA_TITLE, "params.json")
            }
            startActivityForResult(intent, 1)
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
            1 -> {
                Toast.makeText(applicationContext, "got", Toast.LENGTH_LONG).show()
                println(data.toString())
                val stringBuilder = StringBuilder()
                data?.data.also { uri ->
                    contentResolver.openInputStream(uri).use { inputStream: InputStream? ->
                        BufferedReader(InputStreamReader(inputStream)).use { bufferedReader: BufferedReader ->
                            var content: String? = bufferedReader.readLine()
                            while (content != null) {
                                stringBuilder.append(content)
                                content = bufferedReader.readLine()
                            }
                        }
                    }
                    val type: Type = object : TypeToken<List<KernelParameter>>() {}.type
                    val list: MutableList<KernelParameter> =
                        Gson().fromJson(stringBuilder.toString(), type)
                    var noErro: MutableList<KernelParameter> = mutableListOf()

                    GlobalScope.launch(Dispatchers.IO) {
                        list.forEach { kernelParameter: KernelParameter ->
                            val result = commitChanges(kernelParameter)
                            if (result == "error" || !result.contains(kernelParameter.name)) {

                            } else {
                                noErro.add(kernelParameter)
                            }
                        }
                        println(noErro.toString())
                        println("finished")
                        val oldParams = Prefs.removeAllParams(this@MainActivity)
                        if (Prefs.putParams(noErro, this@MainActivity)) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "succes", Toast.LENGTH_LONG)
                                    .show()
                            }
                        } else {
                            Prefs.putParams(oldParams, this@MainActivity)
                        }
                    }
                    println(stringBuilder.toString())
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private suspend fun commitChanges(kernelParam: KernelParameter) =
        withContext(Dispatchers.Default) {
            val commandPrefix = if (prefs.getBoolean(Prefs.USE_BUSYBOX, false)) "busybox " else ""
            val command = when (prefs.getString(Prefs.COMMIT_MODE, "sysctl")) {
                "sysctl" -> "${commandPrefix}sysctl -w ${kernelParam.name}=${kernelParam.value}"
                "echo" -> "echo '${kernelParam.value}' > ${kernelParam.path}"
                else -> "busybox sysctl -w ${kernelParam.name}=${kernelParam.value}"
            }

            RootUtils.executeWithOutput(command, "error")
        }
}
