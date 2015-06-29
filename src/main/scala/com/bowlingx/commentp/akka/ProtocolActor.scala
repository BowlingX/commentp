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

import akka.actor.Actor
import com.bowlingx.commentp.util.Logging
import com.bowlingx.commentp.{Channel, Protocol}
import org.atmosphere.cpr.BroadcasterFactory
import org.json4s.JsonAST.{JInt, JString}

case class ActionResponse(id: String, result: Any)

case class MarkingResponse(startOffset: Int, endOffset: Int, startContainer: String, endContainer: String)

/**
 * Handles protocols
 */
class ProtocolActor(broadcastFactory: BroadcasterFactory) extends Actor with Logging {

  val VOID_RESULT = "OK"
  val INVALID_RESULT = "ER"

  def receive: Receive = {
    case Channel(_, Protocol("init", id, params)) =>
      sender ! ActionResponse(id, VOID_RESULT)

    case Channel(_@channel, Protocol("mark", id, params)) =>
      params.values.toList match {
        case List(startOffset: JInt, endOffset: JInt, startContainer: JString, endContainer: JString) =>
          logger.info(channel)
          broadcastFactory.lookup(channel, true).asInstanceOf[AkkaBroadcaster].broadcast(
            MarkingResponse(startOffset.num.intValue(), endOffset.num.intValue(), startContainer.s, endContainer.s))
          sender ! ActionResponse(id, VOID_RESULT)
        case _ =>
          sender ! ActionResponse(id, INVALID_RESULT)
      }
    case _ => logger.debug("received unimplemented action")
  }
}
