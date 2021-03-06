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
import javax.servlet.http.HttpServletRequest

import com.bowlingx.commentp.akka.{InitiatedMarkingResponse, ActionResponse, MarkingResponse}
import com.bowlingx.commentp.atmosphere.AtmosphereServlet
import org.atmosphere.cpr.{ApplicationConfig, AtmosphereResourceFactory, BroadcasterFactory}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.duration._
import scala.language.postfixOps

case class Channel(id: String, protocol: Protocol, atmosphereUuid:String)

/**
 * Simple protocol defining an action to run with given params
 * @param action name of action
 * @param params action parameters
 */
case class Protocol(action: String, id: String, params: Map[String, JValue])

/**
 * Servlet that handles a socket connection and protocol for all clients
 * @param broadcastFactory factory to create broadcasters
 */
final class WebSocketServlet @Inject()(broadcastFactory: BroadcasterFactory, env: Environment,
                                       resourceFactory: AtmosphereResourceFactory)
  extends AtmosphereServlet {

  implicit val jsonFormats = org.json4s.DefaultFormats

  val broadcasterFactory: BroadcasterFactory = broadcastFactory

  val channel = "/sock/sub/*".intern

  private def extractAtmosphereResourceId(req:HttpServletRequest):String = {
    req.getAttribute(ApplicationConfig.SUSPENDED_ATMOSPHERE_RESOURCE_UUID).asInstanceOf[String]
  }

  // subscribe endpoint
  channel(channel) {
    case (action, message:InitiatedMarkingResponse) =>
      val uuid = extractAtmosphereResourceId(action.req)
      // do not send marking to connection that issued the marking
      if(!message.atmosphereUuid.equals(uuid)) {
        Some(compact(render(Extraction.decompose(message.marking))))
      } else {
        None
      }
  }

  // publish endpoint
  post(channel) { a =>
    val postBody = a.req.getReader.readLine()
    // try to extract protocol
    parse(postBody).extractOpt[Protocol] map { action =>
      implicit val context = env.actorSystem.dispatcher
      implicit val timeout = 1 minute
      val uuid = extractAtmosphereResourceId(a.req)
      a.routeParams.get('splat).flatMap(_.headOption) foreach { channelName =>
        env.run(Channel(channelName, action, uuid))(timeout) foreach {
          case r@ActionResponse(id, message) =>
            Option(resourceFactory.find(uuid)).foreach(resource => {
              // Write answer directly to requested resource
              resource.write(compact(render(Extraction.decompose(r))))
            })
        }
      }
      ""
    } getOrElse "!"
  }
}
