package ru.analyzer

import akka.NotUsed
import akka.stream.scaladsl.{ FileIO, Flow, Framing, GraphDSL, RunnableGraph, Source }
import akka.stream.FlowShape
import akka.util.ByteString
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.{ AkkaStreamlet, AkkaStreamletLogic }
import cloudflow.streamlets.avro.AvroOutlet
import cloudflow.streamlets.{ RoundRobinPartitioner, StreamletShape }
import film.StartEvent
import films.unit.{ Film, FirstHalfUnitFilm, SecondHalfUnitFilm }
import loadingEvent.LoadingDetails
import java.nio.file.{ Path, Paths }
import java.util.Date

class AkkaDataExtractorStreamlet extends AkkaStreamlet {

  private val outLoadingDetails = AvroOutlet[LoadingDetails]("out-details").withPartitioner(RoundRobinPartitioner)
  private val outFilm           = AvroOutlet[Film]("out-film").withPartitioner(RoundRobinPartitioner)
  private val outStatus         = AvroOutlet[StartEvent]("out-event-loader").withPartitioner(RoundRobinPartitioner)
  private var counterRecords    = -1

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic {

    private val dataFile: Path   = Paths.get("data/netflix-dataset.csv")
    private val fileName: String = dataFile.getFileName.toString

    override def runnableGraph(): RunnableGraph[_] = {

      val sourceFile = FileIO.fromPath(dataFile)

      val flowToFilm = GraphDSL.create[FlowShape[ByteString, Film]]() { implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val flowConverter =
          Framing
            .delimiter(ByteString(System.lineSeparator()), maximumFrameLength = 50331648, allowTruncation = true)
            .map(_.utf8String)
            .map { x =>
              counterRecords += 1
              val unitFilm = x.replaceAll("\\^", "\\^ ").split("\\^")
              Film(
                fileName,
                s"recordID-$counterRecords",
                FirstHalfUnitFilm(
                  unitFilm(0),
                  unitFilm(1),
                  unitFilm(2),
                  unitFilm(3),
                  unitFilm(4),
                  unitFilm(5),
                  unitFilm(6),
                  unitFilm(7),
                  unitFilm(8),
                  unitFilm(9),
                  unitFilm(10),
                  unitFilm(11),
                  unitFilm(12),
                  unitFilm(13),
                  unitFilm(14),
                  unitFilm(15),
                  unitFilm(16)
                ),
                SecondHalfUnitFilm(
                  unitFilm(17),
                  unitFilm(18),
                  unitFilm(19),
                  unitFilm(20),
                  unitFilm(21),
                  unitFilm(22),
                  unitFilm(23),
                  unitFilm(24),
                  unitFilm(25),
                  unitFilm(26),
                  unitFilm(27),
                  unitFilm(28)
                )
              )
            }

        val inputFlow  = builder.add(Flow[ByteString])
        val outputFlow = builder.add(Flow[Film])

        inputFlow.out ~> flowConverter ~> outputFlow

        FlowShape(inputFlow.in, outputFlow.out)
      }

      def filmValidation(film: Film): LoadingDetails = {
        film match {
          case e if e.firstHalf.scoreIMDb.trim.isEmpty =>
            LoadingDetails(
              e.fileName,
              new Date().toString,
              s"${e.recordID} film with name: ${e.firstHalf.title} was del because ScoreIMBd was Empty"
            )

          case e if e.secondHalf.votesIMDb.trim.isEmpty =>
            LoadingDetails(
              e.fileName,
              new Date().toString,
              s"${e.recordID} film with name: ${e.firstHalf.title} was del because votesIMBd was Empty"
            )
          case _ => LoadingDetails("-", "-", "-")
        }
      }

      val sourceFilm = sourceFile.via(flowToFilm)

      val pipelineMetric = sourceFilm.map(x => filmValidation(x)).filterNot(_.causeError == "-")

      pipelineMetric.to(plainSink(outLoadingDetails)).run()

      val sourceEvent =
        Source(
          List(StartEvent("DataExtractor starting", new Date().toString))
        )
      sourceEvent.to(plainSink(outStatus))

      sourceFilm.to(plainSink(outFilm))
    }
  }
  override def shape(): StreamletShape = StreamletShape.withOutlets(outFilm, outLoadingDetails, outStatus)
}
