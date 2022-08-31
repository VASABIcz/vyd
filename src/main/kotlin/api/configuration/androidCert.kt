package api.configuration

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureAndroidCert() {
    routing {
        get("/.well-known/assetlinks.json") {
            call.respondText(
                "[{\n" +
                        "  \"relation\": [\"delegate_permission/common.handle_all_urls\"],\n" +
                        "  \"target\" : { \"namespace\": \"android_app\", \"package_name\": \"com.example.vydtu\",\n" +
                        "               \"sha256_cert_fingerprints\": [\"08:08:8D:A2:8D:2A:1E:C1:C6:EC:38:CE:FE:42:43:54:B0:E8:88:47:A3:33:87:57:E4:23:A6:27:A4:A1:F9:32\"] }\n" +
                        "}]"
            )
        }
    }
}