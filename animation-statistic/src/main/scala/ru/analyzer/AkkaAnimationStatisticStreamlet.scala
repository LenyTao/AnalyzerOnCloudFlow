package ru.analyzer

import akka.stream.scaladsl.{ RunnableGraph, Source }
import cloudflow.akkastream.scaladsl.{ FlowWithCommittableContext, RunnableGraphStreamletLogic }
import cloudflow.akkastream.{ AkkaStreamlet, AkkaStreamletLogic }
import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import cloudflow.streamlets.{ RoundRobinPartitioner, StreamletShape }
import film.{ Animation, AnimationMetric, StartEvent }
import java.util.Date

class AkkaAnimationStatisticStreamlet extends AkkaStreamlet {

  private val inAnimation = AvroInlet[Animation]("in-animation")

  private val outAnimationMetric =
    AvroOutlet[AnimationMetric]("out-metric-animation").withPartitioner(RoundRobinPartitioner)

//  private val outStatus = AvroOutlet[StartEvent]("out-event-animation").withPartitioner(RoundRobinPartitioner)

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic {

    override def runnableGraph(): RunnableGraph[_] = {
      val flow =
        FlowWithCommittableContext[Animation]
          .map(animation => AnimationMetric(animation.animationID, animation.title, animation.score, animation.votes))

      val sourceEvent =
        Source(
          List(StartEvent("Animation Statistic starting", new Date().toString))
        )

//      sourceEvent.to(plainSink(outStatus)).run()

      sourceWithCommittableContext(inAnimation).via(flow).to(committableSink(outAnimationMetric))
    }
  }

  override def shape(): StreamletShape =
    StreamletShape.withInlets(inAnimation).withOutlets(outAnimationMetric/*, outStatus*/)
}
