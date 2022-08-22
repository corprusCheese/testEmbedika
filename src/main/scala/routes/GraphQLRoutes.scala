package routes

import caliban.GraphQL.graphQL
import caliban._
import caliban.interop.cats.implicits._
import cats._
import cats.effect._
import cats.effect.std.Dispatcher
import cats.syntax.all._
import dsls._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import routes.decode._
import zio.Runtime

final case class GraphQLRoutes[F[_]: Monad: Async: JsonDecoder: Dispatcher](
    carServiceDsl: CarServiceDsl[F],
    logServiceDsl: LogServiceDsl[F]
) extends Http4sDsl[F] {

  private implicit val zioRuntime: Runtime[Any] = Runtime.default

  private val queries: Queries[F] = Queries(
    carServiceDsl.filterCars,
    carServiceDsl.getStats,
    logServiceDsl.getAllRequests,
    logServiceDsl.getRequestStats
  )

  private val mutations: Mutations[F] =
    Mutations(carServiceDsl.createCar, carServiceDsl.deleteCar)

  private val api = graphQL(RootResolver(queries, mutations))

  private def executeGraphQLRequest(
      graphQLRequest: GraphQLRequest
  ): F[Response[F]] = {
    val query = graphQLRequest.query.get

    for {
      interpreter <- api.interpreterAsync[F]
      _ <- interpreter.checkAsync(query)
      result <- interpreter.executeAsync[F](query) // ide mistaken
      response <-
        if (result.errors.isEmpty) Ok(result.asJson)
        else BadRequest(result.errors.asJson)
    } yield response
  }

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "schema" =>
      schemaResponses
    case ar @ POST -> Root =>
      graphQLResponses(ar)
  }

  def schemaResponses: F[Response[F]] = Ok(api.render)

  def graphQLResponses(ar: Request[F]): F[Response[F]] =
    ar.decodeWithParseHandling[GraphQLRequest](executeGraphQLRequest)

}

object GraphQLRoutes {
  def resource[F[_]: Monad: Async: JsonDecoder: Dispatcher](
      carServiceDsl: CarServiceDsl[F],
      logServiceDsl: LogServiceDsl[F]
  ): Resource[F, GraphQLRoutes[F]] =
    Resource.pure(GraphQLRoutes[F](carServiceDsl, logServiceDsl))
}
