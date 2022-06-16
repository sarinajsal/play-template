package services

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import baseSpec.BaseSpec
import cats.data.EitherT
import connectors.LibraryConnector
import models.{APIError, DataModel}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Futures.whenReady
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

  "getGoogleBook ss" should {
    val url: String = "testUrl"

    "return a book" in {
      (mockConnector.get[DataModel](_: String)(_: OFormat[DataModel], _: ExecutionContext))
        .expects(url, *, *)
        .returning(EitherT.fromEither(Right(gameOfThrones.as[DataModel]))) //fromeither wraps it in the first param(in this case a future)
//        .returning(Future(gameOfThrones.as[DataModel])).once()
//        .returning(EitherT.right(Future(gameOfThrones.as[DataModel])) //whats the difference here?, does the same thing as fromEither

      whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "").value) {result =>
        result shouldBe Right(DataModel("someId","A Game of Thrones", "The best book!!!", 100))
      }
    }
  }


  "return an error ss" in {
    val url: String = "testUrl"
//    val apiErrorReturned: APIError = APIError.BadAPIResponse(Status.BAD_REQUEST)
    (mockConnector.get[DataModel](_: String)(_: OFormat[DataModel], _: ExecutionContext))
      .expects(url, *, *)
//      .returning(Future(Status.BAD_REQUEST.asInstanceOf[DataModel]))// How do we return an error? check this
//      .once()
      .returning(EitherT.fromEither(Left(APIError.BadAPIResponse(400, "no")))) //need to return an instance of future APIError

    whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "").value) { result => //value retrieves the instance inside whatever wrapper you have
      result shouldBe Left(APIError.BadAPIResponse(Status.BAD_REQUEST, "no"))
    }
  }
//ie value here retrieves what is in the either, whenReady handles the future?
}
