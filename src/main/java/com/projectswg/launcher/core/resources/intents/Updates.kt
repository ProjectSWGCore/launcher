package com.projectswg.launcher.core.resources.intents

import com.projectswg.launcher.core.resources.data.update.UpdateServer
import me.joshlarson.jlcommon.control.Intent

data class CancelDownloadIntent(val server: UpdateServer): Intent()
data class DownloadPatchIntent(val server: UpdateServer): Intent()
data class RequestScanIntent(val server: UpdateServer): Intent()
