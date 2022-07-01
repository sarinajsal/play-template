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
import repositories.{DataRepository, MockRepository}


//import repositories.DataRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassManifestFactory.Nothing

class ApplicationServiceSpec extends BaseSpec with MockFactory with ScalaFutures with GuiceOneAppPerSuite{


  val mockRepository = mock[MockRepository]
  val mockDataModel = mock[DataModel]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val testApplicationService = new ApplicationService(mockRepository)

  val gameOfThronesExample: JsValue = Json.obj(
    "_id" -> "someId",
    "name" -> "A Game of Thrones",
    "description" -> "The best book!!!",
    "numSales" -> 100
  )

  val datamodel2: DataModel = DataModel(
    "anid",
    "aname",
  "adescription",
  100
  )

  "ApplicationService.read ss" should{

    val id = "a string?"

    "return a right of a book/datamodel" in{
      (mockRepository.read(_: String))
        .expects(id)
        .returning (Future(Right(gameOfThronesExample.as[DataModel])))
      //trying to return a Future Right datamodel
      //the data repository has returned a Right data model, should now go to Applicationservice and return aRight datamodel

      whenReady(testApplicationService.read(id)) {result =>
        result shouldBe Right(DataModel("someId", "A Game of Thrones", "The best book!!!", 100))
      }
    }

    "return a left of an APIerror" in{
      (mockRepository.read(_: String))
        .expects(id)
        .returning(Future(Left(APIError.BadAPIResponse(404, "NO BOOK?"))))

      whenReady(testApplicationService.read(id)) {result =>
        result shouldBe Left(APIError.BadAPIResponse(404,"cannot find book"))
      }
    }
  }

//  "ApplicationService .index should" {
//    "return a Right[Seq[Datamodel]]" in {
//      (() => (mockRepository.index()).expects
//
//      whenReady(testApplicationService.index) { result =>
//        result shouldBe Right(Seq(DataModel))
//      }
//
//    }
//  }

  "applicationService. delete" should{
    val id = "abcd"
    "return a right boolean when given an existing id" in{
      (mockRepository.delete(_: String))
        .expects(id)
        .returning(Future(Right(true))).once()

      whenReady(testApplicationService.delete(id)) { result =>
        result shouldBe Right(true)
      }
    }

    "return an error sjs" in{
      (mockRepository.delete(_:String))
        .expects(*)
        .returning(Future(Left(APIError.BadAPIResponse(404, ""))))

      whenReady(testApplicationService.delete(id)){result=>
        result shouldBe Left(APIError.BadAPIResponse(404, "COULD NOT DELETE BOOK SERVICE"))
      }
    }

  }

  "applicationService .create" should{
    "return an error if the data model is not valid" in{
      (mockRepository.create(datamodel2))
    }
  }

}
