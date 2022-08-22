package services

import cats.{Monad, MonadThrow}
import cats.effect.Resource
import cats.implicits._
import dsls._
import domain._
import domain.derivatives.RequestForLogCreate

final case class LogService[F[_]: Monad: MonadThrow](
    logRepositoryDsl: LogRepositoryDsl[F]
) extends LogServiceDsl[F] {
  override def saveRequest(
      requestForLog: RequestForLogCreate
  ): F[Unit] =
    logRepositoryDsl
      .saveRequest(requestForLog)
      .map(_ => ())

  override def getAllRequests: F[List[RequestForLog]] =
    logRepositoryDsl.getAllRequests

  override def getRequestStats: F[List[RequestStats]] =
    logRepositoryDsl.getRequestStats
}

object LogService {
  def resource[F[_]: Monad: MonadThrow](
      logRepositoryDsl: LogRepositoryDsl[F]
  ): Resource[F, LogService[F]] =
    Resource.pure[F, LogService[F]](LogService(logRepositoryDsl))
}
