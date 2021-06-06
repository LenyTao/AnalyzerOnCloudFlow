package ru.analyzer

import cloudflow.spark.sql.SQLImplicits.newProductEncoder
import cloudflow.spark.{ SparkStreamlet, SparkStreamletLogic, StreamletQueryExecution }
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import film.StartEvent

class SparkStageJournalStreamlet extends SparkStreamlet {

  private val inletEventExtractor          = AvroInlet[StartEvent]("in-extractor-event")
  private val inletEventProcessor          = AvroInlet[StartEvent]("in-processor-event")
  private val inletEventAnimationStatistic = AvroInlet[StartEvent]("in-animation-event")
  private val inletEventAnyGenreStatistic  = AvroInlet[StartEvent]("in-anygenre-event")

  val shape: StreamletShape =
    StreamletShape.withInlets(
      inletEventExtractor,
      inletEventProcessor,
      inletEventAnimationStatistic,
      inletEventAnyGenreStatistic
    )

  override protected def createLogic(): SparkStreamletLogic = new SparkStreamletLogic() {

    override def buildStreamingQueries: StreamletQueryExecution = {
      val readerExtractorStream = readStream(inletEventExtractor)
      val readerProcessorStream = readStream(inletEventProcessor)
      val readerAnimationStream = readStream(inletEventAnimationStatistic)
      val readerAnyGenreStream  = readStream(inletEventAnyGenreStatistic)

      readerExtractorStream.writeStream
        .format("console")
        .start()
        .toQueryExecution

      readerProcessorStream.writeStream
        .format("console")
        .start()
        .toQueryExecution

      readerAnimationStream.writeStream
        .format("console")
        .start()
        .toQueryExecution

      readerAnyGenreStream.writeStream
        .format("console")
        .start()
        .toQueryExecution
    }
  }
}
