package controllers

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import baseSpec.BaseSpecWithApplication
import models.DataModel
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.mvc.{Result, Results}

import scala.concurrent.Future

class ApplicationControllerSpec extends BaseSpecWithApplication{

  val TestApplicationController = new ApplicationController( //creating an instance of the controller
    component,
    repository,
    service
  )

  private val dataModel: DataModel = DataModel( //a test data model
    "abcd",
    "test name",
    "test description",
    100
  )

  private val updatedDataModel: DataModel = DataModel( //a test data model
    "abcd",
    "test name",
    "test description",
    100
  )

  "ApplicationController .create " should {

    "create a book in the database" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api/create").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)

      status(createdResult) shouldBe Status.CREATED
      afterEach()
    }

    "not take an empty json string" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson("{}")) //empty json body, json body constructed w curly braces
      val createdResult: Future[Result] = TestApplicationController.create()(request)

      status(createdResult) shouldBe Status.BAD_REQUEST
    }

  }



  "ApplicationController .read ss" should {
  beforeEach()
    "find a book in the database by id" in {

      val request: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel)) //why is jsval in square brakcets
      val createdResult: Future[Result] = TestApplicationController.create()(request)

      //Hint: You could use status(createdResult) shouldBe Status.CREATED to check this has worked again

      val readResult: Future[Result] = TestApplicationController.read("abcd")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe DataModel("abcd", "test name", "test description", 100)
      afterEach()
    }
    "not take an empty id string" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildGet("/api/{}").withBody[JsValue](Json.toJson("{}"))
      val createdResult: Future[Result] = TestApplicationController.create()(request)

      status(createdResult) shouldBe Status.BAD_REQUEST

    }
  }

  "ApplicationController .delete" should {
    beforeEach()
    "delete a book in the database by id" in {

      val request: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.CREATED

      val deletedResult: Future[Result] = TestApplicationController.delete("abcd")(FakeRequest())

      status(deletedResult) shouldBe Status.ACCEPTED
    }
    afterEach()
  }

//  "ApplicationController .update" should{
//    beforeEach()
//    "update a books fields in the database by id" in {
//      val request: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
//      val createdResult: Future[Result] = TestApplicationController.create()(request)
//      status(createdResult) shouldBe Status.CREATED
//
//      val newCreatedResult:
//
//      val updatedResult: Future[Result] = TestApplicationController.update("abcd")
//
//      status(updatedResult) shouldBe Status.ACCEPTED
//
//    }
//  }

  "ApplicationController .update()" should {

    "find a book in the database by id and update the fields" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      val updateRequest: FakeRequest[JsValue] = buildPut("/update/abcd").withBody[JsValue](Json.toJson(updatedDataModel))

      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.CREATED
      val updateResult: Future[Result] = TestApplicationController.update("abcd")(updateRequest)


      status(updateResult) shouldBe Status.ACCEPTED
      //contentAsJson(updateResult).as[DataModel] shouldBe DataModel("abcd", "updated test name", "test description", 100) //this will not work as we are not returning the updates result in the body of accepted action
      afterEach()
    }

    "not update if all fields are not entered" in {
      //pattern match?
    }

  }






  "ApplicationController .index" should {

    val result = TestApplicationController.index()(FakeRequest())

    "return OK" in {
      status(result) shouldBe Status.OK
    }
  }

  "ApplicationController .create()" should {

  }

  "ApplicationController .read(id:String)" should {

    val result = TestApplicationController.read("id")(FakeRequest())

    "return OK" in {
      status(result) shouldBe Status.OK
    }

  }

  "ApplicationController .update(id:String)" should {

  }

  "ApplicationController .delete(id:String)" should {

  }

  override def beforeEach(): Unit = repository.deleteAll()
  override def afterEach(): Unit = repository.deleteAll()

}
