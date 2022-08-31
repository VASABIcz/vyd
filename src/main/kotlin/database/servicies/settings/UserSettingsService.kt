package database.servicies.settings

interface UserSettingsService {
    suspend fun getValue(user: Int, key: String): UserSetting?

    suspend fun setValue(user: Int, key: String, value: String): Boolean

    suspend fun getValueBulk(user: Int, vararg keys: String): List<UserSetting>

    suspend fun setValuesBulk(user: Int, vararg values: Pair<String, String>): Boolean
}