package database.servicies.guildChannels

import database.servicies.guilds.GuildChannel
import database.servicies.guilds.GuildChannelService


/*
189918,981981,9818919,91919,91981test(78575757),lol(277575575),69nocategory69()
 */

data class Category(val channel: GuildChannel, val channels: MutableList<GuildChannel>)

data class Chans(val categories: MutableList<Category>, val noCategory: MutableList<GuildChannel>) {
    companion object {
        fun fromString(str: String, channelService: GuildChannelService, guild: Int): Chans {
            var buf = ""
            val noCategory = emptyList<GuildChannel>().toMutableList()
            val categories = emptyList<Category>().toMutableList()
            var currentCategory: String? = null

            for (c in str) {
                when (c) {
                    ',' -> {
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
}

interface GuildChannelsService {
    fun getChannels(channels: String): Chans

    fun moveCategory(name: String, position: Int): Boolean

    fun deleteCategory(name: String): Boolean

    fun createCategory(name: String): Boolean

    fun createChannel(name: String, category: String?)

    fun deleteChannel(channel: Int): Boolean

    fun moveChannel(channel: Int, category: String?, position: Int)
}