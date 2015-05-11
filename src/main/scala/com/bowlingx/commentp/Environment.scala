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

package com.bowlingx.commentp

import javax.inject.Inject

import _root_.akka.actor.ActorRef
import _root_.akka.pattern.ask
import _root_.akka.util.Timeout
import com.bowlingx.commentp.akka.{AkkaBroadcaster, AkkaCluster, DidBroadcast}
import org.atmosphere.cpr.{AtmosphereResource, BroadcasterFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

/**
 * Reflects the environment this Application runs in
 * Shares Access to common services
 */
trait Environment {

  val akkaCluster:AkkaCluster

  val actionActor: ActorRef

  val broadcasterFactory: BroadcasterFactory

  def getBroadcaster: AkkaBroadcaster = broadcasterFactory.get().asInstanceOf[AkkaBroadcaster]

  def getAkkaCluster: AkkaCluster = akkaCluster

  /**
   * Will run a given protocol and execute action
   * @param p protocol
   * @param timeout a timeout until the future should be completed
   * @return
   */
  def run(p: Protocol)(implicit timeout: Timeout): Future[Any] = {
    actionActor ? p
  }

  /**
   * Broadcast a message once and will destroy the broadcaster after
   * @param id the broadcast id
   * @param msg the message
   * @param resources resources to bind to this broadcaster
   * @param duration timeout for receiving a message
   * @return
   */
  def broadcastOnce(id: String, msg: Any, resources:List[AtmosphereResource] = List.empty[AtmosphereResource])
                   (implicit duration: FiniteDuration, executionContext:ExecutionContext): Future[DidBroadcast] = {
    val broadcaster = broadcasterFactory.lookup(id, true).asInstanceOf[AkkaBroadcaster]
    resources foreach(resource => broadcaster.addAtmosphereResource(resource))

    val future = broadcaster.future(msg)
    broadcaster.broadcast(msg)

    future foreach  { didBroadcast =>
      broadcaster.destroy()
    }

    future
  }
}

class ServletEnvironment @Inject()(val akkaCluster: AkkaCluster,
                                   val broadcasterFactory: BroadcasterFactory,
                                   val actionActor: ActorRef) extends Environment {

}
