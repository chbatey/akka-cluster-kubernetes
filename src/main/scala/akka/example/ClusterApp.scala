package akka.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer

object ClusterApp {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    system.log.info("Starting Akka Management")
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    val k8sHealthChecks = new KubernetesHealthChecks(system)

    val routes = k8sHealthChecks.k8sHealthChecks // add real app routes here

    // TODO do this on joining the cluster
    system.log.info("Starting Main App")

    Http().bindAndHandle(routes, "0.0.0.0", 8080)

    system.log.info(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  }

}
