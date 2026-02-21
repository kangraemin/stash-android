package com.kangraemin.stash.ml.embedding

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnnxModelService @Inject constructor(
    @ApplicationContext private val context: Context,
) : Closeable {

    private val environment: OrtEnvironment = OrtEnvironment.getEnvironment()

    private var session: OrtSession? = null

    @Synchronized
    fun getSession(): OrtSession {
        return session ?: loadSession().also { session = it }
    }

    private fun loadSession(): OrtSession {
        val modelBytes = context.assets.open(MODEL_FILE_NAME).use { it.readBytes() }
        return environment.createSession(modelBytes)
    }

    fun createTensor(data: Array<LongArray>): OnnxTensor {
        return OnnxTensor.createTensor(environment, data)
    }

    @Synchronized
    override fun close() {
        session?.close()
        session = null
    }

    companion object {
        const val MODEL_FILE_NAME = "all-MiniLM-L6-v2.onnx"
    }
}
