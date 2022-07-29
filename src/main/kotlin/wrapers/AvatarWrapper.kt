package wrapers

import database.servicies.avatars.AvatarOwner
import database.servicies.avatars.AvatarsService
import database.servicies.avatars.DefaultAvatarService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


class AvatarWrapper(
    private val avatarsService: AvatarsService,
    private val defaultAvatarService: DefaultAvatarService
) {
    suspend fun createAvatar(id: Int, type: AvatarOwner, image: ByteArray): Boolean = withContext(Dispatchers.IO) {
        val img = ImageIO.read(image.inputStream())

        val scaled = img.getScaledInstance(256, 256, Image.SCALE_DEFAULT)

        val outputImage = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)
        outputImage.graphics.drawImage(scaled, 0, 0, null)

        val finalStream = ByteArrayOutputStream()
        ImageIO.write(outputImage, "png", finalStream)

        when (type) {
            AvatarOwner.user -> avatarsService.createUserAvatar(id, finalStream.toByteArray())
            AvatarOwner.guild -> avatarsService.createGuildAvatar(id, finalStream.toByteArray())
        }
    }

    fun getGuildAvatar(id: Int): ByteArray {
        return avatarsService.getGuildAvatar(id) ?: defaultAvatarService.getAvatar(1)!!
    }

    fun getUserAvatar(id: Int): ByteArray {
        return avatarsService.getUserAvatar(id) ?: defaultAvatarService.getAvatar(1)!!
    }
}