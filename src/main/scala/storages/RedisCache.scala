package storages

import cats.Monad
import cats.effect.Resource
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import domain._
import circe._
import domain.derivatives._
import dsls.CacheDsl
import io.circe.Json
import io.circe.jawn._
import io.circe.syntax.EncoderOps

import scala.concurrent.duration.FiniteDuration

final case class RedisCache[F[_]: Monad](
    redisApi: RedisCommands[F, String, String],
    ttlForFilter: FiniteDuration
) extends CacheDsl[F] {

  private def parseToJson(str: String): Option[Json] =
    parse(str).toOption

  override def addStats(stats: CarRepositoryStats): F[Unit] =
    redisApi.set(RedisCache.keys.statsKey, stats.asJson.noSpaces).as(())

  override def addFilterResult(
      carFilter: CarFilter,
      result: List[Car]
  ): F[Unit] =
    redisApi
      .setEx(
        RedisCache.keys.filterKey(carFilter),
        result.asJson.noSpaces,
        ttlForFilter
      )
      .map(_ => ())

  override def getStats: F[Option[CarRepositoryStats]] = {
    redisApi
      .get(RedisCache.keys.statsKey)
      .map(
        _.flatMap(str =>
          parseToJson(str).flatMap(_.as[CarRepositoryStats] match {
            case Left(_) => none[CarRepositoryStats]
            case Right(stats) =>
              stats.some
          })
        )
      )
  }

  override def getFilterResult(carFilter: CarFilter): F[Option[List[Car]]] = {
    redisApi
      .get(RedisCache.keys.filterKey(carFilter))
      .map(
        _.flatMap(str =>
          parseToJson(str).flatMap(_.as[List[Car]] match {
            case Left(_)     => none[List[Car]]
            case Right(list) => list.some
          })
        )
      )
  }

  override def removeCache(): F[Unit] = redisApi.flushAll

  override def updateExpireFilterResult(carFilter: CarFilter): F[Unit] =
    redisApi.expire(RedisCache.keys.filterKey(carFilter), ttlForFilter).as(())
}

object RedisCache {

  def resource[F[_]: Monad](
      redisApi: RedisCommands[F, String, String],
      ttlForFilter: FiniteDuration
  ): Resource[F, RedisCache[F]] =
    Resource.pure[F, RedisCache[F]](RedisCache(redisApi, ttlForFilter))

  object keys {
    val statsKey = "Stats"
    def filterKey(filter: CarFilter) =
      s"Filter:${filter.numberOpt}:${filter.stampOpt}:${filter.colorOpt}:${filter.yearFrom}:${filter.yearTo}"
  }
}
