package database.servicies.guildChannels

import data.responses.GuildsCategory
import data.responses.GuildsChannels
import database.servicies.guilds.GuildChannel
import database.servicies.guilds.GuildChannelService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext


data class Category(val channel: GuildChannel, val channels: MutableList<GuildChannel>) {
    fun toGuildsCategory(): GuildsCategory {
        return GuildsCategory(channel.toGuildsChannel(), channels.map {
            it.toGuildsChannel()
        })
    }
}

data class MetaCategory(val channel: Int, val channels: MutableList<Int>)

data class Chans(val categories: MutableList<Category>, val noCategory: MutableList<GuildChannel>) {
    fun toGuildsChannels(): GuildsChannels {
        return GuildsChannels(categories.map {
            it.toGuildsCategory()
        }, noCategory.map {
            it.toGuildsChannel()
        })
    }

    companion object {
        suspend fun fromString(str: String, channelService: GuildChannelService, guild: Int): Chans =
            withContext(Dispatchers.IO) {
                var buf = ""
                val noCategory = emptyList<GuildChannel>().toMutableList()
                val categories = emptyList<Category>().toMutableList()

                val metaNoCategory = emptyList<Int>().toMutableList()
                val metaCategories = emptyList<MetaCategory>().toMutableList()

                var isCategory = false
                var currentCategoryIndex = 0

                for (c in str) {
                    when (c) {
                        ',' -> {
                            if (buf.isNotBlank()) {
                                if (!isCategory) {
                                    metaNoCategory.add(buf.toInt())
                                } else {
                                    metaCategories[currentCategoryIndex].channels.add(buf.toInt())
                                }
                            }
                        buf = ""
                    }

                    '(' -> {
                        isCategory = true
                        metaCategories.add(MetaCategory(buf.toInt(), mutableListOf()))
                        currentCategoryIndex++
                        buf = ""
                    }

                    ')' -> {
                        if (buf.isNotBlank()) {
                            if (!isCategory) {
                                metaNoCategory.add(buf.toInt())
                            } else {
                                metaCategories[currentCategoryIndex].channels.add(buf.toInt())
                            }
                        }

                        isCategory = false
                        buf = ""
                    }

                        else -> {
                            buf += c
                        }
                    }
                }
                val toAwait = async {
                    channelService.getChannels(guild, *metaNoCategory.toIntArray())
                }
                val toAwait2 = mutableListOf<Deferred<List<GuildChannel>>>()
                for (mc in metaCategories) {
                    toAwait2.add(async {
                        channelService.getChannels(guild, mc.channel, *mc.channels.toIntArray())
                    })
                }

                // FIXME this will crash with big guilds :)
                // TODO or just dont do this :D
                noCategory.addAll(toAwait.await())
                categories.addAll(
                    toAwait2.map {
                        val cs = it.await()
                        Category(
                            cs[0],
                            cs.drop(1) as MutableList<GuildChannel>
                        )
                    }
                )

                return@withContext Chans(categories, noCategory)
        }
    }

    fun moveChannel(id: Int, category: Int?, position: Int): Boolean {
        var chan: GuildChannel?

        if (category != null) {
            categories.find {
                it.channel.channel.id == category
            }
        }

        chan = noCategory.find {
            it.channel.id == id
        }
        if (chan == null) {
            for (c in categories) {
                val cc = c.channels.find {
                    it.channel.id == id
                }
                if (cc != null) {
                    c.channels.remove(cc)
                    chan = cc
                }
            }
        } else {
            noCategory.remove(chan)
        }

        chan ?: return false

        if (category == null) {
            noCategory.add(position, chan)
        } else {
            categories.find {
                it.channel.channel.id == category
            }?.channels?.add(position, chan)
        }

        return true
    }

    fun moveCategory(id: Int, position: Int): Boolean {
        val cat = categories.find {
            it.channel.channel.id == id
        } ?: return false
        categories.remove(cat)

        categories.add(position, cat)

        return true
    }

    fun removeChannel(id: Int): Boolean {
        noCategory.find {
            it.channel.id == id
        }?.also { noCategory.remove(it) } ?: return false

        return true
    }

    fun removeCategory(id: Int): Boolean {
        categories.find {
            it.channel.channel.id == id
        }?.also { categories.remove(it) } ?: return false

        return true
    }

    suspend fun addChannel(id: Int, channelService: GuildChannelService, guild: Int, category: Int?): Boolean {
        if (category == null) {
            channelService.getChannel(id, guild)?.also {
                noCategory.add(it)
            } ?: return false

            return true
        } else {
            channelService.getChannel(id, guild)?.also {
                categories.find { cat ->
                    cat.channel.channel.id == category
                }?.also { cat ->
                    cat.channels.add(it)
                }
            } ?: return false

            return true
        }
    }

    suspend fun addCategory(id: Int, channelService: GuildChannelService, guild: Int): Boolean {
        channelService.getChannel(id, guild)?.also {
            categories.add(Category(it, mutableListOf()))
        } ?: return false

        return true
    }

    override fun toString(): String {
        var str = ""
        for (ch in noCategory) {
            str += ch.channel.id
            str += ','
        }
        for (cat in categories) {
            str + cat.channel.channel.id
            str += '('
            for (ch in cat.channels) {
                str += ch.channel.id
                str += ','
            }
            str += ')'
            str += ','
        }
        return str
    }
}

interface GuildChannelOrderingService {
    suspend fun getChannels(guild: Int): Chans?

    suspend fun moveCategory(channel: Int, guild: Int, position: Int, a: GuildChannelOrderingService): Boolean

    suspend fun deleteCategory(channel: Int, guild: Int, a: GuildChannelOrderingService): Boolean

    suspend fun createCategory(channel: Int, guild: Int, a: GuildChannelOrderingService): Boolean

    suspend fun createChannel(channel: Int, guild: Int, category: Int?, a: GuildChannelOrderingService): Boolean

    suspend fun deleteChannel(channel: Int, guild: Int, a: GuildChannelOrderingService): Boolean

    suspend fun moveChannel(
        channel: Int,
        guild: Int,
        category: Int?,
        position: Int,
        a: GuildChannelOrderingService
    ): Boolean

    suspend fun update(guild: Int, channels: Chans): Boolean

    suspend fun createRecord(guild: Int): Boolean
}