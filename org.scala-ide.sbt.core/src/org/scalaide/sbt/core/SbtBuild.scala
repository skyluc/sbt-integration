package org.scalaide.sbt.core

import java.io.File

import scala.collection.Map
import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.tools.eclipse.logging.HasLogger

import org.eclipse.ui.console.MessageConsoleStream
import org.scalaide.sbt.ui.console.ConsoleProvider

import com.typesafe.sbtrc.client.AbstractSbtServerLocator
import com.typesafe.sbtrc.client.SimpleConnector

import sbt.client.SbtClient
import sbt.protocol.LogEvent
import sbt.protocol.LogMessage
import sbt.protocol.LogStdErr
import sbt.protocol.LogStdOut
import sbt.protocol.LogSuccess
import sbt.protocol.MinimalBuildStructure
import sbt.protocol.ProjectReference

object SbtBuild {

  private var builds = Map[File, SbtBuild]()
  private val buildsLock = new Object

  def buildFor(buildRoot: File): SbtBuild = {
    buildsLock.synchronized {
      builds.get(buildRoot) match {
        case Some(build) =>
          build
        case None =>
          //          val client = createSbtClientFor(buildRoot)
          val build = SbtBuild(buildRoot)
          builds += buildRoot -> build
          build
      }
    }
  }

  private def apply(buildRoot: File): SbtBuild = {
    val build = new SbtBuild(buildRoot)
    build.init()
    build
  }

  /** Create a SbtClient of the sbt build located at buildRoot. */
  private def createSbtClientFor(buildRoot: File): Future[SbtClient] = {
    val connector = new SimpleConnector(buildRoot, new IDEServerLocator)

    val promise = Promise[SbtClient]
    connector.onConnect { promise.success }
    promise.future
  }

  /** SbtServerLocator returning the bundled sbtLaunch.jar and sbt-server.properties. */
  private class IDEServerLocator extends AbstractSbtServerLocator {

    override def sbtLaunchJar: java.io.File = SbtRemotePlugin.plugin.SbtLaunchJarLocation

    override def sbtProperties(directory: java.io.File): java.net.URL = SbtRemotePlugin.plugin.sbtProperties

  }

}

class SbtBuild private (buildRoot: File) extends HasLogger {

  @volatile private var sbtClient: Future[SbtClient] = null

  @volatile private var build: Future[MinimalBuildStructure] = null
  private val buildLock = new Object

  private def init() {
    sbtClient = SbtBuild.createSbtClientFor(buildRoot)
    val console = ConsoleProvider(buildRoot)
    registerEventHandlers(console.newMessageStream())
  }

  private def registerEventHandlers(out: MessageConsoleStream): Unit = {
    import sbt.protocol._
    sbtClient.map {
      _ handleEvents {
        case LogEvent(LogSuccess(msg))        => out.println(s"[success] $msg")
        case LogEvent(LogMessage(level, msg)) => out.println(s"[$level] $msg")
        case LogEvent(LogStdOut(msg))         => out.println(s"[stdout] $msg")
        case LogEvent(LogStdErr(msg))         => out.println(s"[stderr] $msg")
        case m                                => logger.debug("No event handler for " + m)
      }
    }
  }
  
  private def fetchBuildStructure = {
		  val promise = Promise[MinimalBuildStructure]
    sbtClient.map { client =>
            client.watchBuild {
              case b: MinimalBuildStructure =>
                if (promise.isCompleted) {
                  // if the promise has already been completed, set the value in a new future
                  build = Future(b)
                } else {
                  promise.success(b)
                }
            }
    }
		  build = promise.future
  }

//  private def build(): Future[MinimalBuildStructure] =
//    _build.getOrElse {
//      buildLock.synchronized {
//        _build match {
//          case Some(build) =>
//            build
//          case None =>
//            val promise = Promise[MinimalBuildStructure]
//            client.watchBuild {
//              case build: MinimalBuildStructure =>
//                if (promise.isCompleted) {
//                  // if the promise has already been completed, set the value in a new future
//                  _build = Some(Future(build))
//                } else {
//                  promise.success(build)
//                }
//            }
//            _build = Some(promise.future)
//            promise.future
//        }
//      }
//    }

  def compile(project: String) {
    /*TODO: request compilation for the right project*/
    sbtClient.foreach(_.requestExecution("compile"))
  }

  def projects(): Future[immutable.Seq[ProjectReference]] = {
    build.map(_.projects.to[immutable.Seq])
  }
}