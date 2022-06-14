package connectors

import play.api.libs.json.OFormat
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LibraryConnector @Inject()(ws: WSClient) {
  def get[Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Response] = { //(url: string) is given by LibraryService
    val request = ws.url(url)
    val response = request.get()
    response.map {
      result =>
        result.json.as[Response]
    }
  }
}
