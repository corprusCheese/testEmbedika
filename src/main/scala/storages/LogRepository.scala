package storages

import cats.effect._
import cats.implicits._
import domain._
import domain.derivatives._
import circe._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.implicits.legacy.instant._
import doobie.postgres.circe.jsonb.implicits._
import dsls._
import io.circe._
import io.circe.syntax._
import org.postgresql.util.PGobject

final case class LogRepository[F[_]: MonadCancelThrow](tx: Transactor[F])
    extends LogRepositoryDsl[F] {
  override def saveRequest(requestForLog: RequestForLogCreate): F[Boolean] = {
    LogRepository.queries
      .queryForRequestCreation(requestForLog)
      .run
      .transact(tx)
      .map(_ > 0)
  }

  override def getAllRequests: F[List[RequestForLog]] =
    LogRepository.queries.queryForGettingLog
      .to[List]
      .transact(tx)

  override def getRequestStats: F[List[RequestStats]] =
    LogRepository.queries.queryForGettingRequestStats.to[List].transact(tx)

}

object LogRepository {
  def resource[F[_]: MonadCancelThrow](
      tx: Transactor[F]
  ): Resource[F, LogRepository[F]] =
    Resource.pure[F, LogRepository[F]](LogRepository(tx))

  object queries {
    def queryForRequestCreation(
        requestForLog: RequestForLogCreate
    ): doobie.Update0 =
      sql"""|INSERT INTO requests (url, start_processing_at, end_processing_at) 
            |VALUES (${requestForLog.url}, ${requestForLog.startProcessingAt}, ${requestForLog.endProcessingAt})""".stripMargin.update

    def queryForGettingLog: doobie.Query0[RequestForLog] =
      sql"""SELECT id, url, start_processing_at, end_processing_at FROM requests"""
        .query[RequestForLog]

    def queryForGettingRequestStats: doobie.Query0[RequestStats] =
      sql"""|SELECT url, COUNT(id), MAX(end_processing_at - start_processing_at)
            |FROM requests
            |GROUP BY url
            """.stripMargin.query[RequestStats]

  }
}
