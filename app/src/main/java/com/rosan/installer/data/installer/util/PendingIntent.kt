package com.rosan.installer.data.installer.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

private val defaultFlags =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    else PendingIntent.FLAG_UPDATE_CURRENT

fun Intent.pendingBroadcast(
    context: Context,
    requestCode: Int,
    flags: Int = defaultFlags
): PendingIntent = PendingIntent.getBroadcast(context, requestCode, this, defaultFlags)

fun Intent.pendingActivity(
    context: Context,
    requestCode: Int,
    flags: Int = defaultFlags
): PendingIntent = PendingIntent.getActivity(context, requestCode, this, defaultFlags)
