package repositories

  import com.mongodb.client.result.DeleteResult
  import models.{APIError, DataModel}
  import org.mongodb.scala.bson.conversions.Bson
  import org.mongodb.scala.model.Filters.empty
  import org.mongodb.scala.model._
  import org.mongodb.scala.result
  import play.api.libs.json.{JsBoolean, JsValue}
  import uk.gov.hmrc.mongo.MongoComponent
  import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

  import javax.inject.{Inject, Singleton}
  import scala.concurrent.{ExecutionContext, Future}



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
  ) {

//    def create(book: DataModel): Future[DataModel] =
//      collection //a lazy val in PlayMongoRep
//        .insertOne(book)
//        .toFuture()
//        .map(_ => book)

    def create(book: DataModel): = {
      val x = collection.insertOne(book).toFuture()
        .flatMap{
        case x if x.wasAcknowledged().equals(true)  => Future(Right(book))
        case _ => Future(Left(APIError.BadAPIResponse(404, "cant create book/book not inserted")))
      }
    }

    private def byID(id: String): Bson =
      Filters.and(
        Filters.equal("_id", id)
      )

    def read(id: String): Future[Either[APIError, DataModel]]=
      collection.find(byID(id)).headOption flatMap {
        case Some(data) =>
          Future(Right(data)) //a promise of a certain return type
        case _ =>
          Future(Left(APIError.BadAPIResponse(404, "NO BOOK?")))
      }

    def update(id: String, book: DataModel): Future[result.UpdateResult] =
      collection.replaceOne(
        filter = byID(id),
        replacement = book,
        options = new ReplaceOptions().upsert(true) //What happens when we set this to false?
      ).toFuture()

    def delete(id: String): Future[Either[APIError, Boolean]] = {
      val x = collection.deleteOne(
        filter = byID(id))
        x.toFuture().map{
        case x if x.getDeletedCount == 1 => (Right(true))
        case _ => (Left(APIError.BadAPIResponse(404, "COULD NOT DELETE"))) //x if x.getDeletedCount == 0
      }
    }


    def deleteAll(): Future[Unit] = collection.deleteMany(empty()).toFuture().map(_ => ()) //Hint: needed for tests

  }



