# Akka Cluster in Kubernetes

Warning: This depends on Akka `2.5.15` and Akka Management `0.18` which haven't been released yet. They are planned
to be released this week. 

Shows how to run Akka Cluster in Kubernetes.

Main points:
* Use Akka Bootstrap with `akka-dns` with cluster formation via DNS SRV records (requires Akka 2.5.15 & Akka Management 0.18)
* Use separate services for service traffic to internal Akka traffic (remoting, Akka management/bootstrap) so readiness probes for prod traffic don't interfere with boostrap
* Prod traffic readiness based on a member being up

## Pods

Use health checks that check cluster membership. See `KubernetesHealthChecks` along with the following readiness
and liveliness probes:

```
readinessProbe:
  httpGet:
    path: /ready
    port: 8080
livenessProbe:
  httpGet:
    path: /alive
    port: 8080
```

This will mean that a pod won't get traffic until it is part of a cluster which is important
if `ClusterSharding` and `ClusterSingleton` are used.


## Kubernetes Services

For Akka Cluster / Management use a headless service. This allows the solution to not be coupled to k8s as well
as there is no use case for load balancing across management/remoting ports.
In additional also publish endpoints regardless of readiness as cluster bootstrap needs these records
in DNS to work:

```
apiVersion: v1
kind: Service
metadata:
  labels:
    appName: "akka-cluster-kubernetes"
  annotations:
    service.alpha.kubernetes.io/tolerate-unready-endpoints: "true"
  name: "akka-cluster-kubernetes"
spec:
  ports:
    - name: management
      port: 8558
      protocol: TCP
      targetPort: 8558
    - name: remoting
      port: 2552
      protocol: TCP
      targetPort: 2552
  selector:
    appName: "akka-cluster-kubernetes"
  clusterIP: None
  publishNotReadyAddresses: true
```

Note there are currently two ways to set that addressed should be published if not ready, the initial way via an annotation 
`service.alpha.kubernetes.io/tolerate-unready-endpoints` and via the new officially supported way as the property `publishNotReadyAddresses`.
Set both as depending on your DNS solution it may have not migrated from the annotation to the property.

This will result in SRV records being published for the service that contain the nodes that are not ready. This allows
bootstrap to find them and form the cluster thus making them ready.


For prod traffic e.g. HTTP use a regular service. This results in traffic not being routed until bootstrap has finished.

```
apiVersion: v1
kind: Service
metadata:
  labels:
    appName: "akka-cluster-kubernetes"
  name: "akka-cluster-kubernetes-public"
spec:
  ports:
    - name: http 
      port: 8080
      protocol: TCP
      targetPort: 8080 
  selector:
    appName: "akka-cluster-kubernetes"
```

This will result in a ClusterIP being created and only added to `Endpoints` when the pods are `ready`

Note that the `appName` is the same for both services as we want the services to point to the same pods just have
different service types and DNS behavior.

## Scenarios

More scenarios I plan to test.

### Redeploys

### Network partitions

### Blue green

### Canary