package dsls

import domain.{RequestForLog, RequestStats}
import domain.derivatives.RequestForLogCreate

trait LogRepositoryDsl[F[_]] {
  def saveRequest(requestForLog: RequestForLogCreate): F[Boolean]
  def getAllRequests: F[List[RequestForLog]]
  def getRequestStats: F[List[RequestStats]]

}
