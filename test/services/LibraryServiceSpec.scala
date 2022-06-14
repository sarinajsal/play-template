package services

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import baseSpec.BaseSpec
import connectors.LibraryConnector
import models.DataModel
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json, OFormat}

import scala.concurrent.{ExecutionContext, Future}
import scala.tools.nsc.interactive.Response

class LibraryServiceSpec extends BaseSpec with MockFactory with ScalaFutures with GuiceOneAppPerSuite{

  val mockConnector = mock[LibraryConnector]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val testService = new LibraryService(mockConnector)

  val gameOfThrones: JsValue = Json.obj(
    "_id" -> "someId",
    "name" -> "A Game of Thrones",
    "description" -> "The best book!!!",
    "numSales" -> 100
  )

  "getGoogleBook" should {
    val url: String = "testUrl"

    "return a book" in {
      (mockConnector.get[DataModel](_: String)(_: OFormat[DataModel], _: ExecutionContext))
        .expects(url, *, *)
        .returning(Future(gameOfThrones.as[DataModel])).once()

      whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "")) { result =>
        result shouldBe DataModel("someId","A Game of Thrones", "The best book!!!", 100)
      }
    }
  }

  "return an error" in {
    val url: String = "testUrl"

    (mockConnector.get[DataModel](_: String)(_: OFormat[DataModel], _: ExecutionContext))
      .expects(url, *, *)
      .returning(Future(Status.BAD_REQUEST.asInstanceOf[DataModel]))// How do we return an error? check this
      .once()

    whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "")) { result =>
      result shouldBe Status.BAD_REQUEST
    }
  }

}
