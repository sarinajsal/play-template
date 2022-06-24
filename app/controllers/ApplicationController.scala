package controllers

import models.{APIError, DataModel, UpdateOneName}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsBoolean, JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Results}
import repositories.DataRepository
import services.{ApplicationService, LibraryService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents, val dataRepository: DataRepository, val services: LibraryService, val applicationService: ApplicationService)(implicit val executionContext: ExecutionContext) extends BaseController{

  def index(): Action[AnyContent] = Action.async { implicit request =>
    val books: Future[Seq[DataModel]] = dataRepository.collection.find().toFuture()
    books.map(items => Json.toJson(items)).map(result => Ok(result)) //why cant i wrap the whole thing in an Ok?

  }

//  def index2(): Action[AnyContent] = Action.async { implicit request =>
//    val books: Future[Seq[DataModel]] = dataRepository.collection.find().toFuture(x)
//    books.map(items => Json.toJson(items)).map(theJson => Ok(theJson))
//  }


//  def create(): Action[JsValue] = Action.async(parse.json) { implicit request => //calling the parsed json request
//    request.body.validate[DataModel] match {
//      case JsSuccess(dataModel, _) => //dataModel is the body of the json request (rather than request.body)
//        dataRepository.create(dataModel).map( _ => Created)
//      case JsError(_) => Future(BadRequest)
//    }
//  }

  def create():Action[JsValue] = Action.async(parse.json) { implicit request => //getting an action and then parsing json
    applicationService.create(request).map { //implicit request, you will be getting a request
      case (Right(())) => Created
      case Left (e) => Status(e.httpResponseStatus)(e.reason)

    }
  }

//  def create(book: DataModel): Action[AnyContent] =  Action.async(parse.json) { implicit request =>
//    applicationService.create(DataModel.formats.writes(book)).map{
//      case Right (book: JsValue) => Ok(book)
//      case Left (e) => Status(e.httpResponseStatus)(Json.toJson(e.reason))
//    }
//
//  }

//  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
//    val book = dataRepository.read(id)
//    book.map (items => Json.toJson(items)).map(result => Ok(result))
////    book.map(items => Ok(Json.toJson(items))) //an Ok, bad request etc can contain a Json value
//  }

  def read (id: String): Action[AnyContent] = Action.async { implicit request =>
    applicationService.read(id).map{
      case Right(book) => Ok(DataModel.formats.writes(book))
      case Left(e) => Status(e.httpResponseStatus)(Json.toJson(e.reason))
    }
  }

  def update(id: String): Action[JsValue]= Action.async(parse.json) { implicit request =>
    applicationService.update(request, id).map{
      case Right(()) => Accepted
      case Left(e) => Status(e.httpResponseStatus)(Json.toJson(e.reason))
    }

//    request.body.validate[DataModel] match {
//      case JsSuccess(dataModel,_) =>
//        dataRepository.update(id, dataModel).map(_ => Accepted) //calling the data access layer, give it necessary arguments and return a status code
//      case JsError(_)=> Future(BadRequest)
//    }
  }

//  def delete(id: String) = Action.async {implicit request =>
//    val bookDelete = dataRepository.delete(id)
//    Future(Accepted)
//    }

  def delete(id: String): Action[AnyContent]= Action.async {implicit request =>
    applicationService.delete(id).map {
      case Right(bool) => Ok
      case Left(e) => Status(e.httpResponseStatus)(Json.toJson(e.reason))
    }

  }

//  def getGoogleBook(search: String, term: String): Action[AnyContent] = Action.async { implicit request =>
//    val bookjson = services.getGoogleBook(search = search, term = term)
//      bookjson.map(items => Ok(Json.toJson(items)))
//  } //use error handling?


  def getBookByName(name: String): Action[AnyContent] = Action.async { implicit request =>
    applicationService.getBookByName(name).map{
      case Right(book: DataModel) => Ok(Json.toJson(book))
      case Left(e) => BadRequest
    }
  }



  def getGoogleBook(search: String, term: String): Action[AnyContent] = Action.async { implicit request => //status are actions ie the return type
     services.getGoogleBook(search = search, term = term).value.map {
      case Right(book) =>  Ok(Json.toJson(book)) //Ok(DataModel.formats.writes(book))
      case Left(error) => BadRequest //can accept arguments
    }
  }



  def updateName(id: String): Action[JsValue] = Action.async(parse.json){ implicit request =>
    applicationService.updateOneName(request, id).map{
      case Right(updatedBookName) => Ok(Json.toJson(updatedBookName))
      case Left(e) => BadRequest
    }
  }




  //actions? http responses ie the error codes wrapping data models

}
