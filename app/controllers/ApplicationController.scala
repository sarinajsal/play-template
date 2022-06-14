package controllers

import models.DataModel
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Results}
import repositories.DataRepository
import services.{LibraryService}

import java.awt.print.Book
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents, val dataRepository: DataRepository, val services: LibraryService)(implicit val executionContext: ExecutionContext) extends BaseController{

  def index(): Action[AnyContent] = Action.async { implicit request =>
    val books: Future[Seq[DataModel]] = dataRepository.collection.find().toFuture()
    books.map(items => Json.toJson(items)).map(result => Ok(result))
  }

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        dataRepository.create(dataModel).map(_ => Created)
      case JsError(_) => Future(BadRequest)
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
    val book = dataRepository.read(id)
    book.map (items => Json.toJson(items)).map(result => Ok(result))
  }

  def update(id: String): Action[JsValue]= Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel,_) =>
        dataRepository.update(id, dataModel).map(_ => Accepted) //calling the data access layer, give it necessary arguments and return a status code
      case JsError(_)=> Future(BadRequest)
    }
  }

  def delete(id: String) = Action.async {implicit request =>
    val bookDelete = dataRepository.delete(id)
    Future(Accepted)
    }

  def getGoogleBook(search: String, term: String): Action[AnyContent] = Action.async { implicit request =>
    val bookjson = services.getGoogleBook(search = search, term = term)
      bookjson.map(items => Ok(Json.toJson(items)))
  } //use error handling?

//  def getGoogleBook(search: String, term: String): Action[AnyContent] = Action.async { implicit request =>
//    val bookjson = services.getGoogleBook(search = search, term = term).map {
//      case Left =>
//      case Right =>
//    }
//  }


}
