package controllers

import baseSpec.BaseSpecWithApplication
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.api.mvc.Results

class ApplicationControllerSpec extends BaseSpecWithApplication{

  val TestApplicationController = new ApplicationController(
    component,
    repository
  )

  "ApplicationController .index" should {

    val result = TestApplicationController.index()(FakeRequest())

    "return OK" in {
      status(result) shouldBe Status.OK
    }
  }

  "ApplicationController .create()" should {

  }

  "ApplicationController .read(id:String)" should {

  }

  "ApplicationController .update(id:String)" should {

  }

  "ApplicationController .delete(id:String)" should {

  }



}
