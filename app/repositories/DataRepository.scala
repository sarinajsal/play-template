package repositories

  import com.google.inject.ImplementedBy
  import com.mongodb.client.result.DeleteResult
  import com.mongodb.session.ClientSession
  import models.{APIError, DataModel, UpdateOneThing}
  import org.mongodb.scala.bson.conversions.Bson
  import org.mongodb.scala.model.Filters.{bsonType, empty}
  import org.mongodb.scala.model._
  import org.mongodb.scala.result
  import play.api.libs.json.{JsBoolean, JsValue}
  import uk.gov.hmrc.mongo.MongoComponent
  import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

  import javax.inject.{Inject, Singleton}
  import scala.concurrent.{ExecutionContext, Future}
  import org.mongodb.scala._
  import org.mongodb.scala.model._
  import org.mongodb.scala.model.Filters._
  import org.mongodb.scala.model.Updates._
  import org.mongodb.scala.model.UpdateOptions
  import org.mongodb.scala.bson.BsonObjectId

@ImplementedBy(classOf[DataRepository]) //says this trait is a type of dataRepository
trait MockRepository{

  def index(): Future[Either[APIError, Seq[DataModel]]]
  def read(id: String): Future[Either[APIError, DataModel]]
  def create(book: DataModel): Future[Either[APIError, Unit]]
  def update(id: String, book: DataModel): Future[Either[APIError, Unit]]
  def getBookByName(name: String): Future[Either[APIError, DataModel]]
  def delete(id: String): Future[Either[APIError, Boolean]]
  def updateOneElement(id: String, updateOne: UpdateOneThing): Future[Either[APIError, DataModel]]
}

  @Singleton
  class DataRepository @Inject()(
                                  mongoComponent: MongoComponent
                                )(implicit ec: ExecutionContext) extends PlayMongoRepository[DataModel](
    collectionName = "dataModels",
    mongoComponent = mongoComponent,
    domainFormat = DataModel.formats,
    indexes = Seq(IndexModel(
      Indexes.ascending("_id")
    )),
    replaceIndexes = false
  ) with MockRepository{ //class is extending the trait, every method in mock rep has to be inside class datarep w a body

    //    def create(book: DataModel): Future[DataModel] =
    //      collection //a lazy val in PlayMongoRep
    //        .insertOne(book)
    //        .toFuture()
    //        .map(_ => book)

    def index(): Future[Either[APIError, Seq[DataModel]]] = {
      collection.find().toFuture().flatMap{
        case books:Seq[DataModel] => Future(Right(books))
        case _ => Future(Left(APIError.BadAPIResponse(404, "NO BOOKS")))
      }
    }

    def create(book: DataModel): Future[Either[APIError, Unit]] = {
      collection.insertOne(book).toFuture()
        .flatMap {
          case x if x.wasAcknowledged().equals(true) => Future(Right())
          case _ => Future(Left(APIError.BadAPIResponse(404, "cant create book/book not inserted")))
        }
    }

    private def byID(id: String): Bson =
      Filters.and(
        Filters.equal("_id", id)
      )

    private def byName(name: String): Bson =
      Filters.and(
        Filters.equal("name", name)
      )

    def read(id: String): Future[Either[APIError, DataModel]] =
      collection.find(byID(id)).headOption flatMap {
        case Some(data) =>
          Future(Right(data)) //a promise of a certain return type
        case _ =>
          Future(Left(APIError.BadAPIResponse(404, "NO BOOK?")))
      }

    def getBookByName(name: String): Future[Either[APIError, DataModel]] = {
      collection.find(byName(name)).headOption.flatMap {
        case Some(data) => Future(Right(data)) //explain some data? go over some
        case _ => Future(Left(APIError.BadAPIResponse(404, "CANT FIND BY NAME")))
      }
    }

    def update(id: String, book: DataModel): Future[Either[APIError, Unit]] =
      collection.replaceOne(
        filter = byID(id),
        replacement = book,
        options = new ReplaceOptions().upsert(true) //What happens when we set this to false?
      ).toFuture().flatMap {
        case x if x.wasAcknowledged().equals(true) => Future(Right(()))
        case _ => Future(Left(APIError.BadAPIResponse(404, "COULD NOT UPDATE")))
      }


    def delete(id: String): Future[Either[APIError, Boolean]] = {
      val x = collection.deleteOne(
        filter = byID(id))
      x.toFuture().map {
        case x if x.getDeletedCount == 1 => (Right(true))
        case _ => (Left(APIError.BadAPIResponse(404, "COULD NOT DELETE"))) //x if x.getDeletedCount == 0
      }
    }


    def x(id: String, newbook: DataModel): Unit = {
      collection.updateOne(equal("_id", "abcd"), set("_id", "abc"))
    }


    def updateOneElement(id: String, updateOne: UpdateOneThing): Future[Either[APIError, DataModel]] = {
      if (updateOne.nameOfDbColumn == "name") {
        collection.findOneAndUpdate(equal("_id", s"${id}"),
          set("name", s"${updateOne.value}"),
          options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)).headOption().flatMap {
          case Some(newData) => Future(Right(newData))
          case _ => Future(Left(APIError.BadAPIResponse(404, "CANNOT UPDATE NAME")))
        }
      } else if (updateOne.nameOfDbColumn == "description") {
        collection.findOneAndUpdate(equal("_id", s"${id}"),
          set("description", s"${updateOne.value}"),
          options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)).headOption().flatMap {
          case Some(newData) => Future(Right(newData))
          case _ => Future(Left(APIError.BadAPIResponse(404, "CANNOT UPDATE DESCRIPTION")))
        }
      }
      else {
        collection.findOneAndUpdate(equal("_id", s"${id}"),
          set("numSales", updateOne.value.toInt),
          options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)).headOption().flatMap {
          case Some(newData) => Future(Right(newData))
          case _ => Future(Left(APIError.BadAPIResponse(404, "CANNOT UPDATE NUMSALES")))
        }
      }
    }




    //
    //    def updateByOnefield(id: String, nameOption: Option[String], descriptionOption: Option[String], salesOption: Option[Int]): SingleObservable[DataModel] = {
    //      if (nameOption.isDefined){
    //        collection.findOneAndUpdate(equal(
    //          "_id", s"${id}"), set("name", s"${nameOption}"), options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER))
    //      }else if (descriptionOption.isDefined) {
    //        collection.findOneAndUpdate(equal(
    //          "_id", s"${id}"), set("description", s"${descriptionOption}"), options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
    //        }else if(salesOption.isDefined){
    //        collection.findOneAndUpdate(equal(
    //          "_id", s"${id}"), set("numSales", s"${salesOption}"), options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER))
    //        else{
    //
    //        }
    //      }

    //      collection.findOneAndUpdate(equal
    //        filter = (byID(id)),
    //        update = updatedBook,
    //        FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER))

    //      val x = collection.findOneAndUpdate(
    //        filter = (byID(id)),
    //        update = updatedBook,
    //        options = new FindOneAndUpdateOptions)
    //    }
    //
    //
    //
    //
    ////{"_id": "abcd"}, {$set: {"numSales": 2}},
    //
    def deleteAll(): Future[Unit] = collection.deleteMany(empty()).toFuture().map(_ => ())//Hint: needed for tests

  }




