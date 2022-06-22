package services

import controllers.ApplicationController
import models.{APIError, DataModel}
import play.api.http.MediaRange.parse
import play.api.libs.json.{JsBoolean, JsError, JsSuccess, JsValue}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request, Results}
import play.api.mvc.Results.{BadRequest, Created}
import repositories.DataRepository
import views.html.helper.input

import java.awt.Desktop.Action
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.{APIError, DataModel}
import org.mongodb.scala.result.DeleteResult
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsBoolean, JsError, JsSuccess, JsValue, Json}
import repositories.DataRepository
import services.{ApplicationService, LibraryService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationService @Inject()( val dataRepository: DataRepository)(implicit val executionContext: ExecutionContext){

  def read(id: String): Future[Either[APIError, DataModel]] = {
    dataRepository.read(id).map {
      case Right(book: DataModel) => Right(book)
      case Left(e) => Left(APIError.BadAPIResponse(404,"cannot find book"))
    }
  }

  def delete(id: String):Future[Either[APIError, Boolean]]= {
    dataRepository.delete(id).map {
      case Right (deleted: Boolean) => Right(true)
      case Left (e) => Left(APIError.BadAPIResponse(404, "COULD NOT DELETE BOOK SERVICE"))
    }
  }

  def create(inputRequest: Request[JsValue]):Future[Either[APIError, DataModel]] = {
    inputRequest.body.validate[DataModel] match {
      case (JsSuccess(datamodel, _)) =>
        (dataRepository.create(datamodel))
      case JsError(_) => Future(Left(APIError.BadAPIResponse(404, "service checked could not make book")))
    }


  }


}
//book.body.validate[DataModel] match {
//  case JsSuccess(dataModel, _) =>
//  dataRepository.create(dataModel).map(_ => Created)
//  case JsError(_) => Future(BadRequest)
//}