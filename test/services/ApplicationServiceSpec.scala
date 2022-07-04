package services


import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import baseSpec.{BaseSpec, BaseSpecWithApplication}
import cats.data.EitherT
import controllers.ApplicationController
import models.{APIError, DataModel, UpdateOneThing}
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneAppPerTest}
import play.api.libs.json.{JsValue, Json, __}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import repositories.{DataRepository, MockRepository}


//import repositories.DataRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassManifestFactory.Nothing

class ApplicationServiceSpec extends BaseSpecWithApplication with MockFactory with ScalaFutures {


  val mockedRepository: MockRepository = mock[MockRepository]
  val mockDataModel: DataModel = mock[DataModel]
  override implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val testApplicationService = new ApplicationService(mockedRepository)

   val gameOfThronesExample: JsValue = Json.obj(
    "_id" -> "someId",
    "name" -> "A Game of Thrones",
    "description" -> "The best book!!!",
    "numSales" -> 100
  )

   val dataModel1: DataModel = DataModel(
    "anid",
    "aname",
  "adescription",
    100
  )

   val dataModelSequence: Seq[DataModel] = Seq(dataModel1)

  val updateOneThingDataModelType: UpdateOneThing = UpdateOneThing(
    "name",
    "newName"
  )

  "ApplicationService.read ss" should{

    val id = "a string?"

    "return a right of a book/datamodel" in{
      (mockedRepository.read(_: String))
        .expects(id)
        .returning (Future(Right(gameOfThronesExample.as[DataModel])))
      //trying to return a Future Right datamodel
      //the data repository has returned a Right data model, should now go to Applicationservice and return aRight datamodel

      whenReady(testApplicationService.read(id)) {result =>
        result shouldBe Right(DataModel("someId", "A Game of Thrones", "The best book!!!", 100))
      }
    }

    "return a left of an APIerror" in{
      (mockedRepository.read(_: String))
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
      (mockedRepository.delete(_: String))
        .expects(id)
        .returning(Future(Right(true))).once()

      whenReady(testApplicationService.delete(id)) { result =>
        result shouldBe Right(true)
      }
    }

    "return an error sjs" in{
      (mockedRepository.delete(_:String))
        .expects(*)
        .returning(Future(Left(APIError.BadAPIResponse(404, ""))))

      whenReady(testApplicationService.delete(id)){result=>
        result shouldBe Left(APIError.BadAPIResponse(404, "COULD NOT DELETE BOOK SERVICE"))
      }
    }

  }

  "applicationService .create SJS" should{

    "validate a data model and give to mock data repository, returns " in{
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel1))
      (mockedRepository.create(_: DataModel))
        .expects(dataModel1)
        .returning(Future(Right())).once() //why is this needed

      whenReady(testApplicationService.create(request)){result =>
        result shouldBe Right(())
      }
      }

    "recieve an invalid data model and return an error" in{
      val invalidDataModelRequest: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.obj())

      whenReady(testApplicationService.create(invalidDataModelRequest)){result =>
        result shouldBe Left(APIError.BadAPIResponse(404, "service checked could not make book"))
      }
    }
    }

  "applicationService .update" should {
    val id = "anid"
    "update a book given the id and datamodel for the book" in {
      val updatedRequest: FakeRequest[JsValue] = buildPut("/update/anid").withBody[JsValue](Json.toJson(dataModel1))

      (mockedRepository.update(_:String, _:DataModel))
        .expects(id, dataModel1)
        .returning(Future(Right(())))

      whenReady(testApplicationService.update(updatedRequest,id)){result =>
        result shouldBe Right(())
      }

    }

    "not validate the datamodel and return an error " in{
      val invalidUpdateRequest: FakeRequest[JsValue] = buildPut("/update/anid").withBody[JsValue](Json.obj())

      whenReady(testApplicationService.update(invalidUpdateRequest, id)){result =>
        result shouldBe Left(APIError.BadAPIResponse(404,"could not update"))
      }
    }

    "validate a datamodel in service, but return an error in mock repository " in{
      val updatedRequest: FakeRequest[JsValue] = buildPut("/update/anid").withBody[JsValue](Json.toJson(dataModel1))

      (mockedRepository.update(_:String, _:DataModel))
        .expects(id, dataModel1)
        .returning(Future(Left(APIError.BadAPIResponse(404, "COULD NOT UPDATE DR"))))

      whenReady(testApplicationService.update(updatedRequest, id)){result =>
        result shouldBe Left(APIError.BadAPIResponse(404,"COULD NOT UPDATE DR")) //shouldnt this be a different message??
      }
    }

  }

  "ApplicationService .index " should{

    "return a seq[dataModel] " in{
      (() => mockedRepository.index()).expects()
        .returning(Future(Right(dataModelSequence)))

      whenReady(testApplicationService.index){result =>
        result shouldBe Right(dataModelSequence)
      }
    }

    "return an error" in {
      (() => mockedRepository.index())
        .expects()
        .returning(Future(Left(APIError.BadAPIResponse(404, "NO BOOKS"))))

      whenReady(testApplicationService.index){result =>
        result shouldBe Left(APIError.BadAPIResponse(404, "cannot find books"))
      }
    }
  }

  "updateOneElement" should {
    val id = "anid"
    "validate the UpdateOneThing datamodel, send to mock repository and return a right Datamodel " in {

      val updateOneThingRequest: FakeRequest[JsValue] = buildPut("/updateOneElement/anid").withBody[JsValue](Json.toJson(updateOneThingDataModelType))

      (mockedRepository.updateOneElement(_: String, _: UpdateOneThing))
        .expects(id, updateOneThingDataModelType)
        .returning(Future(Right(dataModel1)))

      whenReady(testApplicationService.updateOneElement(updateOneThingRequest, id)) { result =>
        result shouldBe Right(dataModel1)
      }
    }

    "not validate the UpdateOneThing datamodel and return a left error " in {
      val updateOneThingRequest: FakeRequest[JsValue] = buildPut("/updateOneElement/anid").withBody[JsValue](Json.toJson(updateOneThingDataModelType))

      (mockedRepository.updateOneElement(_: String, _: UpdateOneThing))
        .expects(id, updateOneThingDataModelType)
        .returning(Future(Left(APIError.BadAPIResponse(404, ""))))

      whenReady(testApplicationService.updateOneElement(updateOneThingRequest, id)) { result =>
        result shouldBe Left(APIError.BadAPIResponse(404, ""))
      }
    }



  }
  "applicationService .getBookByName " should {
    "get a book by name and return a right data model " in{
    val name = "name"
    val bookNameRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/read/name")

    (mockedRepository.getBookByName(_:String)).expects(name).returning(Future(Right(dataModel1)))

    whenReady(testApplicationService.getBookByName(name)) { result =>
      result shouldBe Right(dataModel1)
    }
    }
    "return a left error in service sjs" in{
      val name = "name"
      val bookNameRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/read/name")

      (mockedRepository.getBookByName(_:String)).expects(name).returning(Future(Left(APIError.BadAPIResponse(404, "CANT FIND BY NAME"))))

      whenReady(testApplicationService.getBookByName(name)) { result =>
        result shouldBe Left(APIError.BadAPIResponse(404, "cant find book by name"))
    }

  }
  }
}
