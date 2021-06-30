package ru.analyzer

import akka.kafka.ConsumerMessage
import akka.stream.scaladsl.{ RunnableGraph, Source }
import cloudflow.akkastream.scaladsl.{ FlowWithCommittableContext, RunnableGraphStreamletLogic }
import cloudflow.akkastream.util.scaladsl.Splitter
import cloudflow.akkastream.{ AkkaStreamlet, AkkaStreamletLogic }
import cloudflow.streamlets.{ RoundRobinPartitioner, StreamletShape }
import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import film.{ Animation, AnyGenre, StartEvent }
import films.unit.Film
import java.util.Date

class AkkaDataProcessorStreamlet extends AkkaStreamlet {

  private val inFilm       = AvroInlet[Film]("in-film")
  private val outAnimation = AvroOutlet[Animation]("out-animation").withPartitioner(RoundRobinPartitioner)
  private val outAnyGenre  = AvroOutlet[AnyGenre]("out-anygenre").withPartitioner(RoundRobinPartitioner)
//  private val outStatus    = AvroOutlet[StartEvent]("out-event-processor").withPartitioner(RoundRobinPartitioner)

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic {
    private var counterAnyGenre  = -1
    private var counterAnimation = -1

    override def runnableGraph(): RunnableGraph[_] = {

      val sourceValidFilm = FlowWithCommittableContext[Film]
        .filterNot(_.firstHalf.scoreIMDb.trim == "")
        .filterNot(_.secondHalf.votesIMDb.trim == "")

      def splitterForGenreFlow: sourceValidFilm.Repr[Either[AnyGenre, Animation], ConsumerMessage.Committable] =
        sourceValidFilm.map { film =>
          if (!film.firstHalf.genre.contains("Animation")) {
            counterAnyGenre += 1
            Left(
              AnyGenre(
                s"AnyGenreID-$counterAnyGenre",
                film.firstHalf.title,
                film.firstHalf.scoreIMDb.trim,
                film.secondHalf.votesIMDb.trim
              )
            )
          } else {
            counterAnimation += 1
            Right(
              Animation(
                s"AnimationID-$counterAnimation",
                film.firstHalf.title,
                film.firstHalf.scoreIMDb.trim,
                film.secondHalf.votesIMDb.trim
              )
            )
          }
        }

      val sourceEvent =
        Source(
          List(StartEvent("DataProcessor starting", new Date().toString))
        )

//      sourceEvent.to(plainSink(outStatus)).run()

      sourceWithCommittableContext(inFilm).to(Splitter.sink(splitterForGenreFlow, outAnyGenre, outAnimation))
    }
  }

  override def shape(): StreamletShape =
    StreamletShape.withInlets(inFilm).withOutlets(outAnimation, outAnyGenre/*, outStatus*/)
}
