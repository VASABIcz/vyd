import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class Responses {
    companion object {
        val success = HttpStatusCode(200, "success")
        val notFound = HttpStatusCode(404, "not found")
        val serverFailure = HttpStatusCode(500, "server failure")

        private fun unspecifiedParameter(parameter: String): HttpStatusCode {
            return HttpStatusCode(400, "unspecified parameter: $parameter")
        }

        private fun invalidParameter(parameter: String): HttpStatusCode {
            return HttpStatusCode(400, "invalid parameter: $parameter")
        }

        suspend fun ApplicationCall.unspecifiedPar(parameter: String) {
            this.respond(unspecifiedParameter(parameter))
        }

        suspend fun ApplicationCall.invalidPar(parameter: String) {
            this.respond(invalidParameter(parameter))
        }

        suspend fun ApplicationCall.usernameTaken(username: String) {
            this.respond(HttpStatusCode(400, "username $username already taken"))
        }

        suspend fun ApplicationCall.success() {
            this.respond(HttpStatusCode(200, "success UWU"))
        }

        suspend fun ApplicationCall.badRequest(value: String) {
            this.respond(HttpStatusCode(400, value))
        }

        suspend fun ApplicationCall.serverIssue() {
            this.respond(HttpStatusCode(500, "server failure"))
        }

        suspend fun ApplicationCall.notFound() {
            this.respond(HttpStatusCode(404, "not found"))
        }
    }
}