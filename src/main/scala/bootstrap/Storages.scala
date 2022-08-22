package bootstrap

import cats.{Monad, MonadThrow}
import cats.effect.{Async, Resource}
import config.{PostgresConfig, RedisConfig, RedisSettings}
import dev.profunktor.redis4cats.effect.MkRedis
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import doobie.Transactor
import dsls._
import storages.{CarRepository, LogRepository, RedisCache}

final case class Storages[F[_]](
    carRepositoryDsl: CarRepositoryDsl[F],
    logRepositoryDsl: LogRepositoryDsl[F],
    cacheDsl: CacheDsl[F]
)

object Storages {
  private def transactorResource[F[_]: Monad: Async](
      config: PostgresConfig
  ): Resource[F, Transactor[F]] =
    Resource.pure[F, Transactor[F]](
      Transactor.fromDriverManager(
        "org.postgresql.Driver",
        s"jdbc:postgresql://${config.host}:${config.port}/${config.db}",
        config.user,
        config.password
      )
    )

  private def redisResource[F[_]: MkRedis: MonadThrow](
      config: RedisConfig
  ): Resource[F, RedisCommands[F, String, String]] = Redis[F].utf8(config.uri)

  def resource[F[_]: Async: Monad: MkRedis](
      postgresConfig: PostgresConfig,
      redisConfig: RedisConfig,
      redisSettings: RedisSettings
  ): Resource[F, Storages[F]] =
    for {
      tx <- transactorResource(postgresConfig)
      carRepository <- CarRepository.resource[F](tx)
      logRepository <- LogRepository.resource[F](tx)
      redisCommands <- redisResource[F](redisConfig)
      redis <- RedisCache.resource(redisCommands, redisSettings.ttlForFilter)
    } yield Storages(carRepository, logRepository, redis)
}
