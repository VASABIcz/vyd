package wrapers

import database.servicies.avatars.AvatarOwner
import database.servicies.avatars.AvatarService
import database.servicies.avatars.DefaultAvatarService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


class AvatarWrapper(
    private val userAvatarService: AvatarService,
    private val guildAvatarService: AvatarService,
    private val dmAvatarService: AvatarService,
    private val defaultAvatarService: DefaultAvatarService,
) {
    // TODO rework
    suspend fun createAvatar(id: Int, type: AvatarOwner, image: ByteArray): Boolean = withContext(Dispatchers.IO) {
        val img = ImageIO.read(image.inputStream())

        val scaled = img.getScaledInstance(256, 256, Image.SCALE_DEFAULT)

        val outputImage = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)
        outputImage.graphics.drawImage(scaled, 0, 0, null)

        val finalStream = ByteArrayOutputStream()
        ImageIO.write(outputImage, "png", finalStream)

        when (type) {
            AvatarOwner.user -> userAvatarService.setAvatar(id, finalStream.toByteArray())
            AvatarOwner.guild -> guildAvatarService.setAvatar(id, finalStream.toByteArray())
            AvatarOwner.group -> dmAvatarService.setAvatar(id, finalStream.toByteArray())
        }
    }

    suspend fun getGuildAvatar(id: Int): ByteArray = withContext(Dispatchers.IO) {
        return@withContext guildAvatarService.getAvatar(id) ?: defaultAvatarService.getAvatar(1)!!
    }

    suspend fun getUserAvatar(id: Int): ByteArray = withContext(Dispatchers.IO) {
        return@withContext userAvatarService.getAvatar(id) ?: defaultAvatarService.getAvatar(1)!!
    }

    suspend fun getDmAvatar(id: Int): ByteArray = withContext(Dispatchers.IO) {
        return@withContext dmAvatarService.getAvatar(id) ?: defaultAvatarService.getAvatar(1)!!
    }
}