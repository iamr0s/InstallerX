package com.rosan.installer.data.process.repo

import android.content.Context
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.di.init.processModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import kotlin.reflect.KClass
import kotlin.system.exitProcess

abstract class ProcessRepo {
    companion object : KoinComponent {
        fun <T : Any> request(
            clazz: KClass<T>,
            vararg params: Any?
        ): String {
            val command = arrayListOf(
                "app_process",
                "-Djava.class.path='${get<Context>().packageCodePath}'",
                "/system/bin",
                "'${clazz.qualifiedName}'",
            )
            command.addAll(params.map { it.toString() })
            return command.joinToString(" ")
        }
    }

    class NullOutputStream : OutputStream() {
        override fun write(p0: Int) {
        }
    }

    private var isIgnoreWarning = false

    private var defaultOut = System.out

    private var defaultErr = System.err

    /*
    * 为什么我要看似多此一举的自己实现一个标准输入流
    * 而不是直接使用System.in呢
    *
    * 让我们假设一个场景
    * 按照顺序执行两段命令
    * > command1
    * > command2
    * 其中command1需要从标准输入中读取100字节
    * command2需要读取28字节
    * 于是我们启动一个Process，向Process的填入128字节
    *
    * 正常情况应该是，command1读取了100字节
    * 还剩下28字节，command2读取了剩余的28字节
    *
    * 奇怪的事情发生了，程序卡住了，command2并没有被执行，问题出在哪里呢？
    *
    * 因为Java关于System.in实现默认是缓冲的，即是一个BufferedInputStream实现
    * 且缓冲大小为128字节，于是程序就按照这个流程运行
    *
    * command1只需要100字节，但是因为是Buffered的
    * 所以当read的时候，程序直接把标准输入中的128字节都纳入了缓冲区
    * 等到执行command2时，标准输入已经被掏空了，command2还在啥啥等待28字节的输入
    *
    * 所以只需要把command1的标准输入字节流换成not buffered的就行
    * */
    val stdin: InputStream = FileInputStream(FileDescriptor.`in`)

    /*
    * use @JvmStatic
    * */
    @OptIn(ExperimentalSerializationApi::class)
    open fun main(args: Array<String>) = runBlocking<Unit> {
        val throwable = kotlin.runCatching {
            setIgnoreWarning(true)
            startKoin {
                modules(processModules)
            }
            onCreate(args)
        }.exceptionOrNull()
        setIgnoreWarning(false)
        if (throwable == null) return@runBlocking
        if (throwable is ErrorEntity) {
            val serializer = get<ProtoBuf>()
            withContext(Dispatchers.IO) {
                System.out.write(serializer.encodeToByteArray(throwable))
            }
        } else {
            throwable.printStackTrace(System.err)
        }
        exitProcess(126)
    }

    protected abstract suspend fun onCreate(args: Array<String>)

    suspend fun onDestroy() {
    }

    // 忽略非致命错误
    open fun setIgnoreWarning(state: Boolean) {
        isIgnoreWarning = state
        setErr(if (state) null else defaultErr)
    }

    private fun setErr(printStream: PrintStream?) {
        System.setErr(printStream ?: PrintStream(NullOutputStream()))
    }

    fun stdinBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        var offset = 0
        while (offset < size) {
            offset += stdin.read(bytes, offset, size - offset)
        }
        return bytes
    }
}