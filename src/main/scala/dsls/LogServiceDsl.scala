package dsls

import domain.{RequestForLog, RequestStats}
import domain.derivatives.RequestForLogCreate

trait LogServiceDsl[F[_]] {
  def saveRequest(requestForLog: RequestForLogCreate): F[Unit]
  def getAllRequests: F[List[RequestForLog]]
  def getRequestStats: F[List[RequestStats]]
}
