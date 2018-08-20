name := "akka-cluster-kubernetes"

scalaVersion := "2.12.6"

// Needs 2.5.15
//resolvers += "Akka Snapshots" at "http://repo.akka.io/snapshots/"
lazy val akkaVersion = "2.5-SNAPSHOT"
lazy val akkaManagementVersion = "0.17.0+3-121d548d+20180817-1641"

enablePlugins(SbtReactiveAppPlugin)

dependencyOverrides ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
  "com.lightbend.akka.discovery" %% "akka-discovery-dns" % akkaManagementVersion,
  "com.lightbend.akka.discovery" %% "akka-discovery-config" % akkaManagementVersion,
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,

  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

// Used for debugging k8s DNS issues. Not needed for production
alpinePackages += "bind-tools"
alpinePackages += "busybox-extras"
alpinePackages += "curl"
alpinePackages += "strace"

endpoints += HttpEndpoint("management", 8558)
endpoints += HttpEndpoint("remoting", 2552)

deployMinikubeRpArguments ++= Seq("--pod-controller-replicas", "2")
deployMinikubeRpArguments ++= Seq("--service-cluster-ip", "None")
