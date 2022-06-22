package services


import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import baseSpec.BaseSpec
import cats.data.EitherT
import controllers.ApplicationController
import models.{APIError, DataModel}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneAppPerTest}
import play.api.libs.json.{JsValue, Json, __}
//import repositories.DataRepository
//
//import scala.concurrent.{ExecutionContext, Future}
//import scala.reflect.ClassManifestFactory.Nothing
//
//class ApplicationServiceSpec extends BaseSpec with MockFactory with ScalaFutures with GuiceOneAppPerSuite{
//
//  val mockApplicationController = mock[ApplicationController]
//  val mockDataRepository = mock[DataRepository]
//  val mockDataModel = mock[DataModel]
//  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
//  val testApplicationService = new ApplicationService(mockApplicationController, mockDataRepository, mockDataModel)
//
//  val gameOfThronesExample: JsValue = Json.obj(
//    "_id" -> "someId",
//    "name" -> "A Game of Thrones",
//    "description" -> "The best book!!!",
//    "numSales" -> 100
//  )
//
//  "ApplicationService.read ss" should{
//
//    val id = "a string?"
//
//    "return a right of a book/datamodel" in{
//      (mockDataRepository.read(_: String))
//        .expects(id)
//        .returning (Future(Right(gameOfThronesExample.as[DataModel])))
//      //trying to return a Future Right datamodel
//      //the data repository has returned a Right data model, should now go to Applicationservice and return aRight datamodel
//
//      whenReady(testApplicationService.read(id)) {result =>
//        result shouldBe Right(DataModel("someId", "A Game of Thrones", "The best book!!!", 100))
//      }
//    }
//  }
//
//}
