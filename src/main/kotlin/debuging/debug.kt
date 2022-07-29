package debuging

import database.servicies.avatars.DatabaseDefaultAvatarService
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO


fun insertDefaultAvatar(database: Database, bytes: ByteArray): Boolean {
    val avs = DatabaseDefaultAvatarService(database)
    return avs.createAvatar(bytes)
}

fun crop(av: InputStream): ByteArray {
    val img = ImageIO.read(av)

    val scaled = img.getScaledInstance(256, 256, Image.SCALE_DEFAULT)

    val outputImage = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)
    outputImage.graphics.drawImage(scaled, 0, 0, null)

    val finalStream = ByteArrayOutputStream()
    ImageIO.write(outputImage, "png", finalStream)

    return finalStream.toByteArray()
}

fun main() {
    val database = Database.connect(
        System.getenv("database_uri"),
        user = System.getenv("database_username"),
        password = System.getenv("database_password"),
        dialect = PostgreSqlDialect()
    )

    val img = File("").inputStream()

    val croped = crop(img)

    if (insertDefaultAvatar(database, croped)) {
        println("SUCCESS")
    } else {
        println("ERROR")
    }
}