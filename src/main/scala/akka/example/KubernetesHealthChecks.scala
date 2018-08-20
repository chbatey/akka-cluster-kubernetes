package akka.example

import akka.actor.ActorSystem
import akka.cluster.{Cluster, MemberStatus}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route


class KubernetesHealthChecks(system: ActorSystem) {

  val log: LoggingAdapter = Logging(system, getClass)
  val cluster = Cluster(system)

  private val readyStates: Set[MemberStatus] = Set(MemberStatus.Up, MemberStatus.Down)
  private val aliveStates: Set[MemberStatus] = Set(MemberStatus.Joining, MemberStatus.WeaklyUp, MemberStatus.Up, MemberStatus.Leaving, MemberStatus.Exiting)

  val k8sHealthChecks: Route =
    path("ready") {
      get {
        val selfState = cluster.selfMember.status
        log.info("ready? clusterState {}", selfState)
        if (readyStates.contains(selfState)) complete(StatusCodes.OK)
        else complete(StatusCodes.InternalServerError)
      }
    } ~
      path("alive") {
        get {
          val selfState = cluster.selfMember.status
          log.info("alive? clusterState {}", selfState)
          if (aliveStates.contains(selfState)) complete(StatusCodes.OK)
          else complete(StatusCodes.InternalServerError)
        }
      }
}
