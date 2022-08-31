package database.servicies.settings

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.support.mysql.bulkInsertOrUpdate
import org.ktorm.support.mysql.insertOrUpdate

class DatabaseUserSettingsService(private val database: Database) : UserSettingsService {
    private val parameters = database.sequenceOf(DatabaseUserSettings)

    override suspend fun getValue(user: Int, key: String): UserSetting? {
        return parameters.find {
            (it.user eq user) and (it.key eq key)
        }
    }

    override suspend fun setValue(user: Int, key: String, value: String): Boolean {
        return database.insertOrUpdate(DatabaseUserSettings) {
            set(it.user, user)
            set(it.key, key)
            set(it.value, value)
            onDuplicateKey {
                set(it.value, value)
            }
        } != 0
    }

    override suspend fun getValueBulk(user: Int, vararg keys: String): List<UserSetting> {
        return parameters.filter {
            (it.user eq user) and (it.key inList keys.toList())
        }.toList()
    }

    override suspend fun setValuesBulk(user: Int, vararg values: Pair<String, String>): Boolean {
        return database.bulkInsertOrUpdate(DatabaseUserSettings) {
            values.forEach { pair ->
                item {
                    set(it.user, user)
                    set(it.key, pair.first)
                    set(it.value, pair.second)
                }
            }
        } != 0
    }
}