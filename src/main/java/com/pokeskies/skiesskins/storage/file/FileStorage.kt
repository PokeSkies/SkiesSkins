package com.pokeskies.skiesskins.storage.file

import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.data.UserData
import com.pokeskies.skiesskins.storage.IStorage
import java.util.*

class FileStorage : IStorage {
    private var fileData: FileData = SkiesSkins.INSTANCE.loadFile(STORAGE_FILENAME, FileData(), true)

    companion object {
        private const val STORAGE_FILENAME = "storage.json"
    }

    override fun getUser(uuid: UUID): UserData? {
        return fileData.userdata[uuid]
    }

    override fun saveUser(uuid: UUID, userData: UserData): Boolean {
        fileData.userdata[uuid] = userData
        return SkiesSkins.INSTANCE.saveFile(STORAGE_FILENAME, fileData)
    }
}