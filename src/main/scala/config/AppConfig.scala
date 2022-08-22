package config

import cats.MonadThrow
import cats.effect.Resource
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.duration.FiniteDuration

case class HttpServerConfig(
    host: String,
    port: String
)

case class PostgresConfig(
    host: String,
    port: String,
    db: String,
    user: String,
    password: String
)

case class RedisConfig(uri: String)

case class RedisSettings(ttlForFilter: FiniteDuration)

case class AppConfig(
    httpServerConfig: HttpServerConfig,
    postgresConfig: PostgresConfig,
    redisConfig: RedisConfig,
    redisSettings: RedisSettings
)

object AppConfig {
  def resource[F[_]: MonadThrow]: Resource[F, AppConfig] =
    Resource.pure(ConfigSource.default.loadOrThrow[AppConfig])
}
