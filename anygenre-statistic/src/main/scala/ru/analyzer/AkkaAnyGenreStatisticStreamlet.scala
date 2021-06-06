package ru.analyzer

import akka.stream.scaladsl.{ RunnableGraph, Source }
import cloudflow.akkastream.scaladsl.{ FlowWithCommittableContext, RunnableGraphStreamletLogic }
import cloudflow.akkastream.{ AkkaStreamlet, AkkaStreamletLogic }
import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import cloudflow.streamlets.{ RoundRobinPartitioner, StreamletShape }
import film.{ AnyGenre, AnyGenreMetric, StartEvent }
import java.util.Date

class AkkaAnyGenreStatisticStreamlet extends AkkaStreamlet {

  private val inAnyGenre = AvroInlet[AnyGenre]("in-anygenre")

  private val outAnyGenreMetric =
    AvroOutlet[AnyGenreMetric]("out-metric-anygenre").withPartitioner(RoundRobinPartitioner)

  private val outStatus = AvroOutlet[StartEvent]("out-event-anygenre").withPartitioner(RoundRobinPartitioner)

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic {

    override def runnableGraph(): RunnableGraph[_] = {
      val flow =
        FlowWithCommittableContext[AnyGenre]
          .map(anyGenre => AnyGenreMetric(anyGenre.anyGenreID, anyGenre.title, anyGenre.score, anyGenre.votes))

      val sourceEvent =
        Source(
          List(StartEvent("Any Genre statistic starting", new Date().toString))
        )

      sourceEvent.to(plainSink(outStatus))

      sourceWithCommittableContext(inAnyGenre).via(flow).to(committableSink(outAnyGenreMetric))
    }
  }
  override def shape(): StreamletShape = StreamletShape.withInlets(inAnyGenre).withOutlets(outAnyGenreMetric, outStatus)
}
