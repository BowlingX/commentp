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

import java.net.URI
import java.util.UUID

import akka.actor._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.{Publish, Unsubscribe}
import akka.routing.{Deafen, Listen, Listeners}
import com.bowlingx.commentp.util.Logging
import org.atmosphere.cpr._
import org.atmosphere.util.AbstractBroadcasterProxy

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.contrib.pattern.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import scala.collection.JavaConversions._
import scala.util.Try
import scala.util.control.NonFatal

/**
 * Constants for Akka Broadcaster
 */
object AkkaBroadcaster {
  val clusterSystem = getClass().getName()
}

/**
 * Akka Publisher, handles message and a notification if a message has been delivered to client
 * @param topic
 */
class Publisher(topic: String) extends Actor with ActorLogging {


  // activate the extension
  val mediator = DistributedPubSubExtension(context.system).mediator

  def receive: Receive = {
    case o: Broadcast => mediator ! Publish(topic, o)
  }
}

/**
 * Message that occurs when a message has been delivered to client
 * @param message message that has been delivered
 * @param id the ID of the message
 */
@SerialVersionUID(1L) case class DidBroadcast(message: Any, id: UUID)

/**
 * Serializable message for a Broadcast event
 * @param message message that should be delivered
 * @param uniqueId id of this message
 */
@SerialVersionUID(1L) case class Broadcast(message: Any, uniqueId: UUID)

/**
 * Atmosphere Broadcaster implementation with Akka Pub/Sub
 */
class AkkaBroadcaster() extends AbstractBroadcasterProxy with Logging {

  // scalastyle:off
  implicit var system: ActorSystem = null.asInstanceOf[ActorSystem]

  var publisher: ActorRef = null.asInstanceOf[ActorRef]
  var subscriber: ActorRef = null.asInstanceOf[ActorRef]
  // scalastyle:on

  val didPublishTimeoutSeconds = 6

  /**
   * Initializes this Broadcaster
   *
   * @param id channel/topic
   * @param uri uri (unused)
   * @param config atmosphere config
   * @return
   */
  override def initialize(id: String, uri: URI, config: AtmosphereConfig): Broadcaster = {

    super.initialize(id, uri, config)

    /**
     * Actor System, will be resolved through an servlet attribute defined in
     * [[AkkaBroadcaster.clusterSystem]]
     */
    system = config.getServletContext.getAttribute(AkkaBroadcaster.clusterSystem).asInstanceOf[ActorSystem]
    publisher = system.actorOf(Props(new Publisher(id)))
    subscriber = system.actorOf(Props(new Actor with Listeners {

      import context.dispatcher

      val mediator = DistributedPubSubExtension(context.system).mediator

      override def postStop() {
        mediator ! Unsubscribe(id, self)
      }

      override def preStart() {
        // subscribe to broadcaster topic
        mediator ! Subscribe(id, self)
      }

      def receive: Actor.Receive = {
        {
          {
            case SubscribeAck(Subscribe(_, _, `self`)) =>
              context become ready
          }: Actor.Receive
        } orElse listenerManagement
      }

      def ready: Actor.Receive = {
        {
          case b: DidBroadcast =>
            gossip(b)

          case broadcastedMsg@Broadcast(m, uniqueID) =>

            // Register listener and notify publisher if message has been broadcast successfully
            val listener = new AtmosphereResourceEventListener {
              def onThrowable(event: AtmosphereResourceEvent) {
                event.getResource.removeEventListener(this)
              }

              def onBroadcast(event: AtmosphereResourceEvent) {
                system.scheduler.scheduleOnce(didPublishTimeoutSeconds seconds) {
                  mediator ! Publish(id, DidBroadcast(m, uniqueID))
                }
                event.getResource.removeEventListener(this)
              }

              def onSuspend(event: AtmosphereResourceEvent) {}

              def onPreSuspend(event: AtmosphereResourceEvent) {}

              def onClose(event: AtmosphereResourceEvent) {}

              def onDisconnect(event: AtmosphereResourceEvent) {}

              def onResume(event: AtmosphereResourceEvent) {
                system.scheduler.scheduleOnce(didPublishTimeoutSeconds seconds) {
                  mediator ! Publish(id, DidBroadcast(m, uniqueID))
                }
                event.getResource.removeEventListener(this)
              }

              override def onHeartbeat(event: AtmosphereResourceEvent): Unit = {}
            }
            getAtmosphereResources foreach {
              r =>
                r.addEventListener(listener)
            }
            broadcastReceivedMessage(broadcastedMsg)
        }: Actor.Receive
      } orElse listenerManagement


    }))

    this
  }

  /**
   * Will return a future and will resolve if broadcast succeeded and client did receive the message
   * @param msg the message to check
   * @return
   */
  def future(msg: Any)(implicit duration: FiniteDuration): Future[DidBroadcast] = {

    val p = TimeoutPromise[DidBroadcast](duration, system.dispatcher, system)

    subscriber ! Listen(system.actorOf(Props(new Actor {
      def receive: Actor.Receive = {
        case b@DidBroadcast(message, _) if message == msg =>
          if (!p.isCompleted) {
            logger.debug(s"Message Received:: ${message}")
            p success b
          }
          self ! PoisonPill
          subscriber ! Deafen(self)
        case _ => logger.debug("skipped unprocessable message")
      }
    })))

    p.future
  }

  def incomingBroadcast() {}

  /**
   * Will destroy all actors that have been created during Broadcaster lifecycle
   */
  override def destroy() {
    this.synchronized {
      logger.debug("destroying broadcaster")
      super.destroy()
      if (Option(system).isDefined) {
        // Make Sure we kill our actors when killing the broadcaster, but wait a
        // bit until DidBroadcast Messages are delivered
        system.scheduler.scheduleOnce(didPublishTimeoutSeconds * 3 seconds) {
          publisher ! PoisonPill
          subscriber ! PoisonPill
        }(system.dispatcher)
      }
    }
  }

  def outgoingBroadcast(message: scala.Any) {
    publisher ! Broadcast(message, UUID.randomUUID())
  }

  override def broadcastReceivedMessage(message: scala.Any): Unit = {
    val broadcast = message.asInstanceOf[Broadcast]
    Try {
      Option(filter(broadcast.message)) foreach { newMessage =>
        push(new Deliver(newMessage, new AkkaBroadcastFuture(broadcast, newMessage), broadcast.message))
      }
    } recover {
      case NonFatal(e) => logger.error(e.getMessage, e)
    }
  }

}

/**
 * Akka Future that contains broadcasted message
 * @param broadcast message
 */
class AkkaBroadcastFuture(val broadcast: Broadcast, filteredMessage: Any)
  extends BroadcasterFuture[Any](filteredMessage)