package com.pokeskies.skiesskins.storage.file

import com.pokeskies.skiesskins.data.UserData
import java.util.*

class FileData {
    var userdata: HashMap<UUID, UserData> = HashMap()
    override fun toString(): String {
        return "FileData(userdata=$userdata)"
    }
}