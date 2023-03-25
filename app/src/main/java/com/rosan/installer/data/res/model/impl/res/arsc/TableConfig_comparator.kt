//package com.rosan.installer.data.res.model.impl.res.arsc
//
//import com.rosan.installer.data.res.model.impl.res.arsc.config.ScreenLong
//import com.rosan.installer.data.res.model.impl.res.arsc.config.ScreenSize
//import com.rosan.installer.data.res.model.impl.res.arsc.config.UIModeNight
//import com.rosan.installer.data.res.model.impl.res.arsc.config.UIModeType
//
//{ o1, o2 ->
//    o2.config.let { o ->
//        o1.config.run {
//            // 越相似，数值越小
//            if (mcc.toInt() != 0
//                || o.mcc.toInt() != 0
//                || mnc.toInt() != 0
//                || o.mnc.toInt() != 0
//            ) {
//                if (mcc != o.mcc
//                    && requested.mcc != 0
//                ) {
//                    return@sortedWith if (mcc.toInt() != 0) -1 else 1
//                }
//                if (mnc != o.mnc
//                    && requested.mnc != 0
//                ) {
//                    return@sortedWith if (mnc.toInt() != 0) -1 else 1
//                }
//            }
//            if (screenLayout.toInt() != 0
//                || o.screenLayout.toInt() != 0
//            ) {
//                if ((screenLayout xor o.screenLayout).toInt() != 0
//                    && requested.screenLayout != 0
//                ) {
//                    return@sortedWith if (layoutDir.value > o.layoutDir.value) -1 else 1
//                }
//            }
//            if (smallestScreenWidthDp.toInt() != 0
//                || o.smallestScreenWidthDp.toInt() != 0
//            ) {
//                if (smallestScreenWidthDp != o.smallestScreenWidthDp) {
//                    return@sortedWith if (smallestScreenWidthDp > o.smallestScreenWidthDp) -1 else 1
//                }
//            }
//            if (screenWidthDp.toInt() != 0
//                || screenHeightDp.toInt() != 0
//                || o.screenWidthDp.toInt() != 0
//                || o.screenHeightDp.toInt() != 0
//            ) {
//                var myDelta = 0
//                var otherDelta = 0
//                if (requested.screenWidthDp != 0) {
//                    myDelta += requested.screenWidthDp - screenWidthDp.toInt()
//                    otherDelta += requested.screenWidthDp - o.screenWidthDp.toInt()
//                }
//                if (requested.screenHeightDp != 0) {
//                    myDelta += requested.screenHeightDp - screenHeightDp.toInt()
//                    otherDelta += requested.screenHeightDp - o.screenHeightDp.toInt()
//                }
//                if (myDelta != otherDelta) {
//                    return@sortedWith if (myDelta < otherDelta) -1 else 1
//                }
//            }
//            if (screenLayout.toInt() != 0
//                || o.screenLayout.toInt() != 0
//            ) {
//                val requestedScreenSize =
//                    ScreenSize.build(requested.screenLayout.toUByte()).value.toInt()
//                val requestedScreenLong =
//                    ScreenLong.build(requested.screenLayout.toUByte()).value.toInt()
//                if (((screenSize.value xor o.screenSize.value).toInt() != 0)
//                    && requestedScreenSize != 0
//                ) {
//                    val mySL = screenSize.value.toInt()
//                    val oSL = o.screenSize.value.toInt()
//                    var fixedMySL = mySL
//                    var fixedOSL = oSL
//                    if (requestedScreenSize >= ScreenSize.Normal.value.toInt()) {
//                        if (fixedMySL == 0) fixedMySL = ScreenSize.Normal.value.toInt()
//                        if (fixedOSL == 0) fixedOSL = ScreenSize.Normal.value.toInt()
//                    }
//                    if (fixedMySL == fixedOSL) {
//                        if (mySL == 0) return@sortedWith 1
//                        return@sortedWith -1
//                    } else {
//                        return@sortedWith if (fixedMySL > fixedOSL) -1 else 1
//                    }
//                }
//
//                if ((screenLong.value xor o.screenLong.value).toInt() != 0
//                    && requestedScreenLong != 0
//                ) {
//                    return@sortedWith if (screenLong.value.toInt() != 0) -1 else 1
//                }
//            }
//            if (screenLayout2.toInt() != 0
//                || o.screenLayout2.toInt() != 0
//            ) {
//                if ((screenRound.value xor o.screenRound.value).toInt() != 0
//                    && requested.isScreenRound
//                ) {
//                    return@sortedWith if (screenRound.value.toInt() != 0) -1 else 1
//                }
//            }
//            if (colorMode.toInt() != 0
//                || o.colorMode.toInt() != 0
//            ) {
//                if ((wideColorGamut.value xor o.wideColorGamut.value).toInt() != 0
//                    && requested.isScreenWideColorGamut
//                ) {
//                    return@sortedWith if (wideColorGamut.value.toInt() != 0) -1 else 1
//                }
//                if ((hdr.value xor o.hdr.value).toInt() != 0
//                    && requested.isScreenHdr
//                ) {
//                    return@sortedWith if (hdr.value.toInt() != 0) -1 else 1
//                }
//            }
//            if (orientation != o.orientation
//                && requested.orientation != 0
//            ) {
//                return@sortedWith if (orientation.value.toInt() != 0) -1 else 1
//            }
//            if (uiMode.toInt() != 0
//                || o.uiMode.toInt() != 0
//            ) {
//                if ((uiModeType.value xor o.uiModeType.value).toInt() != 0
//                    && UIModeType.build(requested.uiMode.toUByte()).value.toInt() != 0
//                ) {
//                    return@sortedWith if (uiModeType.value.toInt() != 0) -1 else 1
//                }
//                if ((uiModeNight.value xor o.uiModeNight.value).toInt() != 0
//                    && UIModeNight.build(requested.uiMode.toUByte()).value.toInt() != 0
//                ) {
//                    return@sortedWith if (uiModeNight.value.toInt() != 0) -1 else 1
//                }
//            }
//        }
//    }
//    return@sortedWith 0
//}