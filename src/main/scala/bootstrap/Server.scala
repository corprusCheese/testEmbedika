package bootstrap

import cats.effect.{Async, Resource}
import com.comcast.ip4s.{Host, Port}
import config.HttpServerConfig
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

object Server {
  def buildAsResource[F[_]: Async](
      httpServerConfig: HttpServerConfig,
      httpApp: HttpApp[F]
  ): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHostOption(Host.fromString(httpServerConfig.host))
      .withPort(Port.fromString(httpServerConfig.port).get)
      .withHttpApp(httpApp)
      .build
}
