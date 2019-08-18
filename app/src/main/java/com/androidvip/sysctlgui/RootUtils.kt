package com.androidvip.sysctlgui

import com.stericson.RootShell.execution.Command
import com.stericson.RootTools.RootTools
import java.util.concurrent.atomic.AtomicBoolean

object RootUtils {
    private var commandId: Int = 0

    @Synchronized
    fun executeWithOutput(command: String, defaultOutput: String, multiline: Boolean = false, forEachLine: ((String?) -> Unit)? = null) : String {
        val sb = StringBuilder()
        val completed = AtomicBoolean(false)
        try {
            RootTools.getShell(true).add(object : Command(++commandId, false, command) {
                override fun commandOutput(id: Int, line: String?) {
                    super.commandOutput(id, line)
                    if (multiline) {
                        if (forEachLine != null) {
                            forEachLine(line)
                        }
                        sb.append(line).append("\n")
                    } else {
                        sb.append(line)
                        completed.set(true)
                    }
                }

                override fun commandCompleted(id: Int, exitcode: Int) {
                    completed.set(true)
                }

                override fun commandTerminated(id: Int, reason: String?) {
                    completed.set(true)
                }
            })
        } catch (e: Exception) {
            return defaultOutput
        }

        while (true) {
            if (completed.get())
                return sb.toString()
        }
    }
}