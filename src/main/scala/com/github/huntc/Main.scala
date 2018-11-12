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
import java.time.Instant
import java.time.{ Duration => JavaDuration }
import java.util.concurrent.TimeUnit

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.stream.typed.scaladsl.ActorMaterializer
import akka.stream.scaladsl.{ BroadcastHub, Keep, MergeHub, Source }

import scala.concurrent.duration._

object Main {
  case object Tick

  final case class Time(time: FiniteDuration)

  def main(args: Array[String]): Unit = {

    def run: Behavior[Time] = Behaviors.setup { context =>
      implicit val mat: ActorMaterializer = ActorMaterializer.boundToActor(context)

      val (writeSink, writeSource) =
        MergeHub
          .source[Flows.WriteRequest.type]
          .toMat(BroadcastHub.sink)(Keep.both)
          .run()

      val client = context.spawn(Client(writeSink), "client")
      val server = context.spawn(Server(writeSource), "server")

      Behaviors.receiveMessage {
        case Time(t) =>
          client ! Client.Time(t)
          server ! Server.Time(t)
          Behaviors.same
      }
    }

    implicit val simulation: ActorSystem[Time] =
      ActorSystem[Time](run, "fake-news")
    implicit val mat: ActorMaterializer = ActorMaterializer()

    val startTime = Instant.now()

    val _ =
      Source
        .tick(0.seconds, 1.seconds, Tick)
        .map(
          _ =>
            Time(
              FiniteDuration(JavaDuration.between(startTime, Instant.now()).toMillis,
                             TimeUnit.MILLISECONDS)
          )
        )
        .runForeach(simulation.tell)
  }
}
