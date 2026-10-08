package com.v2ray.ang.ui.main

import androidx.compose.runtime.saveable.listSaver

data class ServerDeleteTarget(
    val guid: String,
    val profileName: String,
) {
    companion object {
        val Saver = listSaver<ServerDeleteTarget?, String>(
            save = { target -> target?.let { listOf(it.guid, it.profileName) } },
            restore = { values -> if (values.size < 2) null else ServerDeleteTarget(values[0], values[1]) },
        )
    }
}
