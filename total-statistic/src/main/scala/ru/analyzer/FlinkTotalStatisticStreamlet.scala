package ru.analyzer

import cloudflow.flink.{ FlinkStreamlet, FlinkStreamletLogic }
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import film.{ AnimationMetric, AnyGenreMetric }
import loadingEvent.LoadingDetails
import org.apache.flink.api.scala.createTypeInformation

class FlinkTotalStatisticStreamlet extends FlinkStreamlet {

  @transient private val inletAnimationStatic = AvroInlet[AnimationMetric]("in-res-animation-metric")

  @transient private val inletAnyGenreStatic = AvroInlet[AnyGenreMetric]("in-res-anygenre-metric")

  @transient private val inletDataExtractor: AvroInlet[LoadingDetails] = AvroInlet[LoadingDetails]("in-loadingdetails")

  override def shape(): StreamletShape =
    StreamletShape.withInlets(inletDataExtractor, inletAnimationStatic, inletAnyGenreStatic)

  override protected def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph(): Unit = {
      val readerMetric          = readStream(inletDataExtractor)
      val readerAnimationStatic = readStream(inletAnimationStatic)
      val readerAnyGenreStatic  = readStream(inletAnyGenreStatic)
      readerMetric.print()
      readerAnimationStatic.print()
      readerAnyGenreStatic.print()
    }
  }
}
