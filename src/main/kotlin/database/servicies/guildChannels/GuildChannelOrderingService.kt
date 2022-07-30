package database.servicies.guildChannels

import data.responses.GuildsCategory
import data.responses.GuildsChannels
import database.servicies.guilds.GuildChannel
import database.servicies.guilds.GuildChannelService


data class Category(val channel: GuildChannel, val channels: MutableList<GuildChannel>) {
    fun toGuildsCategory(): GuildsCategory {
        return GuildsCategory(channel.toGuildsChannel(), channels.map {
            it.toGuildsChannel()
        })
    }
}

data class Chans(val categories: MutableList<Category>, val noCategory: MutableList<GuildChannel>) {
    fun toGuildsChannels(): GuildsChannels {
        return GuildsChannels(categories.map {
            it.toGuildsCategory()
        }, noCategory.map {
            it.toGuildsChannel()
        })
    }

    companion object {
        fun fromString(str: String, channelService: GuildChannelService, guild: Int): Chans {
            var buf = ""
            val noCategory = emptyList<GuildChannel>().toMutableList()
            val categories = emptyList<Category>().toMutableList()
            var currentCategory: String? = null

            for (c in str) {
                when (c) {
                    ',' -> {
                        if (buf.isNotBlank()) {
                            if (currentCategory == null) {
                                channelService.getChannel(buf.toInt(), guild)?.also {
                                    noCategory.add(it)
                                }
                            } else {
                                channelService.getChannel(buf.toInt(), guild)?.also {
                                    categories.find {
                                        it.channel.channel.id == currentCategory!!.toInt()
                                    }?.channels?.add(it)
                                }
                            }
                        }
                        buf = ""
                    }

                    '(' -> {
                        currentCategory = buf
                        channelService.getChannel(buf.toInt(), guild)?.also {
                            categories.add(Category(it, mutableListOf()))
                        }
                        buf = ""
                    }

                    ')' -> {
                        if (buf.isNotBlank()) {
                            if (currentCategory == null) {
                                channelService.getChannel(buf.toInt(), guild)?.also {
                                    noCategory.add(it)
                                }
                            } else {
                                channelService.getChannel(buf.toInt(), guild)?.also {
                                    categories.find {
                                        it.channel.channel.id == currentCategory!!.toInt()
                                    }?.channels?.add(it)
                                }
                            }
                        }

                        currentCategory = null
                        buf = ""
                    }

                    else -> {
                        buf += c
                    }
                }
            }
            return Chans(categories, noCategory)
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

    fun addChannel(id: Int, channelService: GuildChannelService, guild: Int, category: Int?): Boolean {
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

    fun addCategory(id: Int, channelService: GuildChannelService, guild: Int): Boolean {
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
    fun getChannels(guild: Int): Chans?

    fun moveCategory(channel: Int, guild: Int, position: Int): Boolean

    fun deleteCategory(channel: Int, guild: Int): Boolean

    fun createCategory(channel: Int, guild: Int): Boolean

    fun createChannel(channel: Int, guild: Int, category: Int?): Boolean

    fun deleteChannel(channel: Int, guild: Int): Boolean

    fun moveChannel(channel: Int, guild: Int, category: Int?, position: Int): Boolean

    fun update(guild: Int, channels: String): Boolean

    fun createRecord(guild: Int): Boolean
}