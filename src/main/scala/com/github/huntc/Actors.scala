/*
 * Copyright 2018 huntc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.huntc
import akka.NotUsed
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.{ Materializer, OverflowStrategy }
import akka.stream.scaladsl.{ Sink, Source }
import com.github.huntc.Flows.WriteRequest

import scala.concurrent.duration.FiniteDuration

object Flows {
  case object WriteRequest
}

/**
  * A client represents the producer of requests
  */
object Client {

  def apply(writeRequests: Sink[Flows.WriteRequest.type, NotUsed])(
      implicit mat: Materializer
  ): Behavior[Time] = generateRequests(writeRequests)

  final case class Time(time: FiniteDuration)

  def generateRequests(
      writeSink: Sink[Flows.WriteRequest.type, NotUsed]
  )(implicit mat: Materializer): Behaviors.Receive[Time] = Behaviors.receiveMessage { _ =>
    Source
      .repeat(WriteRequest)
      .take(100)
      .runWith(writeSink)
    Behaviors.same
  }
}

/**
  * A server represents the consumer of requests
  */
object Server {

  def apply(writeRequests: Source[Flows.WriteRequest.type, NotUsed])(
      implicit mat: Materializer
  ): Behavior[Time] = handleRequests(writeRequests)

  final case class Time(time: FiniteDuration)

  def handleRequests(
      writeSource: Source[Flows.WriteRequest.type, NotUsed]
  )(implicit mat: Materializer): Behavior[Time] = Behaviors.receiveMessage { _ =>
    writeSource
      .log("consumer")
      .runWith(Sink.ignore)
    Behaviors.same
  }
}
