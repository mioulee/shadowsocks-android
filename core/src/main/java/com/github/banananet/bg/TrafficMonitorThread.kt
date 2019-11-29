/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2017 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2017 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                             *
 *  This program is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by       *
 *  the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                        *
 *                                                                             *
 *  This program is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 *  GNU General Public License for more details.                               *
 *                                                                             *
 *  You should have received a copy of the GNU General Public License          *
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                             *
 *******************************************************************************/

package com.github.banananet.bg

import android.net.LocalSocket
import android.text.TextUtils
import android.widget.Toast
import com.github.banananet.Core
import com.github.banananet.preference.DataStore
import com.github.banananet.utils.Key
import com.github.banananet.utils.printLog
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

class TrafficMonitorThread : LocalSocketListener("TrafficMonitorThread") {
    override val socketFile = File(Core.deviceStorage.filesDir, "stat_path")

    override fun accept(socket: LocalSocket) {
        try {
            val buffer = ByteArray(16)
            if (socket.inputStream.read(buffer) != 16) throw IOException("Unexpected traffic stat length")
            val stat = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
            TrafficMonitor.update(stat.getLong(0), stat.getLong(8))

            //检查是否到过期时间，如是,停掉服务
            if(checkIsExpire()) {
                Core.stopService()
                Toast.makeText(Core.app, "使用时长已用完，BananaNet已自动断开", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            printLog(e)
        }
    }

    fun checkIsExpire(): Boolean {
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val curTime = dateFormat.format(date)
        val accountStr = DataStore.privateStore.getString(Key.expiredTime)?:""
        return !(!TextUtils.isEmpty(accountStr) && curTime <= accountStr)
    }

    fun generateUserName(): String? {
        return DataStore.privateStore.getString(Key.userName)
    }
}
