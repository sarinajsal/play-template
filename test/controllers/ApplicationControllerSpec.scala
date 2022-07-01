package controllers

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import baseSpec.BaseSpecWithApplication
import models.{DataModel, UpdateOneThing}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.http.{Status, dateFormat}
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.mvc.{AnyContentAsEmpty, Result, Results}
import repositories.DataRepository

import scala.concurrent.Future
import scala.tools.nsc.interpreter.Naming.sessionNames.result

 class ApplicationControllerSpec extends BaseSpecWithApplication with MockFactory with ScalaFutures {

  val TestApplicationController = new ApplicationController( //creating an instance of the controller
    component,
    repository,
    service,
    appservice
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

  private val updateOneNameDM: UpdateOneThing = UpdateOneThing(
    "name",
    "updatedName"
  )

  private val updateOneDescriptionDM: UpdateOneThing = UpdateOneThing(
    "description",
    "updatedDescription"
  )

  private val updateOneNumSalesDM: UpdateOneThing = UpdateOneThing(
    "numSales",
    "200"
  )

  "ApplicationController .create  " should {

    "create a book in the database " in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel))
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



  "ApplicationController .read " should {

    beforeEach()

    "find a book in the database by id and return an Action[AnyContent] ie status result Ok containing book Json" in {

      val buildRequest: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel)) //making a fake request
      val buildResult: Future[Result] = TestApplicationController.create()(buildRequest) //the request is not an argument
      val getRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/read/abcd")
      val getResult: Future[Result] = TestApplicationController.read("abcd")(getRequest)  //ordering of stuff here?

      status(getResult) shouldBe Status.OK
      status(buildResult) shouldBe Status.CREATED
      contentAsJson(getResult).as[DataModel] shouldBe (DataModel("abcd", "test name", "test description", 100))
      afterEach()
    }




//  beforeEach()
//    "find a book in the database by id" in {
//
//      val request: FakeRequest[JsValue] = buildGet(s"/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel)) //why is jsval in square brakcets
//      val createdResult: Future[Result] = TestApplicationController.create()(request)
//
//      //Hint: You could use status(createdResult) shouldBe Status.CREATED to check this has worked again
//
//      val readResult: Future[Result] = TestApplicationController.read("abcd")(FakeRequest())
//
//      status(readResult) shouldBe Status.OK
//      contentAsJson(readResult).as[DataModel] shouldBe Right(DataModel("abcd", "test name", "test description", 100))
//      afterEach()
//    }


    "not take an empty id string" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildGet("/read/{}").withBody[JsValue](Json.toJson("{}"))
      val createdResult: Future[Result] = TestApplicationController.create()(request)

      status(createdResult) shouldBe Status.BAD_REQUEST

    }
  }

//  "ApplicationController .delete" should {
//    beforeEach()
//    "delete a book in the database by id" in {
//
//      val request: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
//      val createdResult: Future[Result] = TestApplicationController.create()(request)
//      status(createdResult) shouldBe Status.CREATED
//
//      val deletedResult: Future[Result] = TestApplicationController.delete("abcd")(FakeRequest())
//
//      status(deletedResult) shouldBe Status.OK
//    }
//    afterEach()
//  }

  "applicationController.delete w error handling" should {
    beforeEach()
    "delete a book in the db by id" in {
      val buildRequest: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel)) //making a fake request
      val buildResult: Future[Result] = TestApplicationController.create()(buildRequest)
      status(buildResult) shouldBe Status.CREATED

      val deleteResult: Future[Result] = TestApplicationController.delete("abcd")(fakeRequest)

      status(deleteResult) shouldBe Status.OK
      afterEach()
    }

    "throw an error if id does not exist sjs" in{
      val buildRequest: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel)) //making a fake request
      val buildResult: Future[Result] = TestApplicationController.create()(buildRequest)
      status(buildResult) shouldBe Status.CREATED

      val deleteResult: Future[Result] = TestApplicationController.delete("abc")(fakeRequest)

      status(deleteResult) shouldBe Status.INTERNAL_SERVER_ERROR
      afterEach()
    }
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

  "ApplicationController .update() with error handling" should {

    "find a book in the database by id and update the fields ss" in {
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


  "ApplicationController .getBookByName" should {

    "find a book by name and return book in Ok status " in{
      beforeEach()
      val buildRequest: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel))
      val builderResult: Future[Result] = TestApplicationController.create()(buildRequest)
      status(builderResult) shouldBe Status.CREATED

      val buildNameGetRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/read/test name")
      val buildNameGetResult: Future[Result] = TestApplicationController.getBookByName("test name")(buildNameGetRequest)

      status(buildNameGetResult) shouldBe Status.OK
      contentAsJson(buildNameGetResult).as[DataModel] shouldBe (DataModel("abcd", "test name", "test description", 100))

      afterEach()
    }
  }


  "applicationController .updateOneElement " should {

    "update the name only" in {
      beforeEach()
      val buildRequest: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel))
      val builderResult: Future[Result] = TestApplicationController.create()(buildRequest)
      status(builderResult) shouldBe Status.CREATED

      val buildUpdate: FakeRequest[JsValue] = buildPut("/updateOneElement/abcd").withBody[JsValue](Json.toJson(updateOneNameDM))
      val buildUpdateResult: Future[Result] = TestApplicationController.updateOneElement("abcd")(buildUpdate)

      status(buildUpdateResult) shouldBe Status.OK
      contentAsJson(buildUpdateResult).as[DataModel] shouldBe DataModel("abcd", "updatedName", "test description", 100) //this will not work as we are not returning the updates result in the body of accepted action
      afterEach()

    }

    "update only the description sjs" in {

      beforeEach()
      val buildRequest: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel))
      val builderResult: Future[Result] = TestApplicationController.create()(buildRequest)
      status(builderResult) shouldBe Status.CREATED

      val buildUpdate: FakeRequest[JsValue] = buildPut("/updateOneElement/abcd").withBody[JsValue](Json.toJson(updateOneDescriptionDM))
      val buildUpdateResult: Future[Result] = TestApplicationController.updateOneElement("abcd")(buildUpdate)

      status(buildUpdateResult) shouldBe Status.OK
      contentAsJson(buildUpdateResult).as[DataModel] shouldBe DataModel("abcd", "test name", "updatedDescription", 100) //this will not work as we are not returning the updates result in the body of accepted action
      afterEach()

    }

    "update on the numSales sjs" in {
      beforeEach()
      val buildRequest: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel))
      val builderResult: Future[Result] = TestApplicationController.create()(buildRequest)
      status(builderResult) shouldBe Status.CREATED

      val buildUpdate: FakeRequest[JsValue] = buildPut("/updateOneElement/abcd").withBody[JsValue](Json.toJson(updateOneNumSalesDM))
      val buildUpdateResult: Future[Result] = TestApplicationController.updateOneElement("abcd")(buildUpdate)

      status(buildUpdateResult) shouldBe Status.OK
      contentAsJson(buildUpdateResult).as[DataModel] shouldBe DataModel("abcd", "test name", "test description", 200) //this will not work as we are not returning the updates result in the body of accepted action
      afterEach()
    }

  }






  "ApplicationController .index" should {

    val result = TestApplicationController.index()(FakeRequest())

    "return OK" in {
      status(result) shouldBe Status.OK
    }
  }



  "ApplicationController .read(id:String)" should {

    val result = TestApplicationController.read("id")(FakeRequest())

    "return OK" in {
      status(result) shouldBe Status.OK
    }

  }



  override def beforeEach(): Unit = repository.deleteAll()
  override def afterEach(): Unit = repository.deleteAll()

}
