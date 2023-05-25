package com.rosan.installer.data.recycle.model.impl

import android.content.Context
import com.rosan.app_process.AppProcess
import com.rosan.installer.data.recycle.repo.Recycler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.StringTokenizer

class AppProcessRecycler(private val shell: String) : Recycler<AppProcess>(), KoinComponent {
    private class CustomizeAppProcess(private val shell: String) : AppProcess.Terminal() {
        override fun newTerminal(): MutableList<String> {
            val st = StringTokenizer(shell)
            val cmdList = mutableListOf<String>()
            while (st.hasMoreTokens()) {
                cmdList.add(st.nextToken());
            }
            return cmdList;
        }
    }

    private val context by inject<Context>()

    override fun onMake(): AppProcess {
        return CustomizeAppProcess(shell).apply {
            init(context.packageName)
        }
    }
}