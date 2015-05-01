// The MIT License (MIT)
//
// Copyright (c) 2015 David Heidrich, BowlingX <me@bowlingx.com>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.bowlingx.commentp.akka

import akka.actor._
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
 * Companion, interface for creating systems
 */
object ClusterSystem {

  /**
   * Creates a new Cluster System
   * @param name name of actorSystem
   * @param masterTimeout global worker timeout
   * @param config optional configuration (use none for testing)
   * @return
   */
  def apply(name: String, masterTimeout: FiniteDuration, config: Option[String],
            joinAddress: Option[Address]): ClusterSystem = {
    val system = config match {
      case Some(f) => ActorSystem(name, ConfigFactory.load(f))
      case None => ActorSystem(name)
    }
    apply(system, masterTimeout, joinAddress)
  }

  /**
   * Creates a new Cluster System
   * @param actorSystem actorSystem
   * @param masterTimeout timeout to connect
   * @return
   */
  def apply(actorSystem: ActorSystem, masterTimeout: FiniteDuration): ClusterSystem =
    new ClusterSystem(actorSystem, masterTimeout, None)

  /**
   * Creates a Cluster with specific join Address
   * @param actorSystem system
   * @param masterTimeout timeout to connect
   * @param joinAddress the address of the master node
   * @return
   */
  def apply(actorSystem: ActorSystem, masterTimeout: FiniteDuration, joinAddress: Option[Address])
  : ClusterSystem = new ClusterSystem(actorSystem, masterTimeout, joinAddress)

}

/**
 * A Cluster System
 *
 * @param clusterSystem actorSystem
 */
class ClusterSystem(clusterSystem: ActorSystem,
                    masterTimeout: FiniteDuration, joinAddress: Option[Address]) extends AkkaCluster {

  private[this] val address = joinAddress.getOrElse(Cluster(clusterSystem).selfAddress)
  Cluster(clusterSystem).joinSeedNodes(scala.collection.immutable.Seq(address))

  def getJoinAddress: Address = address

  /**
   * @return the created clusterSystem
   */
  def cluster: ActorSystem = clusterSystem

  /**
   * Shutdowns cluster
   */
  def shutdown() {
    cluster.shutdown()
    cluster.awaitTermination(masterTimeout)
  }
}

/**
 * An Akka cluster system
 */
trait AkkaCluster {

  def cluster: ActorSystem
}
