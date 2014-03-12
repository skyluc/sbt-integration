package org.scalaide.sbt.core

import org.junit.Test
import org.junit.Assert._
import rx.lang.scala.Observable
import sbt.client.SbtClient
import sbt.protocol.MinimalBuildStructure
import sbt.protocol.ProjectReference
import scala.concurrent.Await
import scala.concurrent.duration._
import org.eclipse.ui.console.MessageConsole
import java.net.URI

class SbtBuildTest {
  
  @Test
  def getBuildValue {
    // create the fake sbtClient
    val sbtClient = new ConfigurableSbtClient()
    
    // set the sbtClient content
    val expectedProjects = Seq(ProjectReference(new URI("file:///home"), "test"))
    
    val build = MinimalBuildStructure(Nil, expectedProjects)
    sbtClient.setBuild(build)
    
    // create the observable
    val sbtClientObservable = Observable[SbtClient] { subscriber =>
      subscriber.onNext(sbtClient)
      subscriber.onCompleted()
    }
    
    // initialize sbtBuild
    val sbtBuild = new SbtBuild(null, sbtClientObservable, null)
    
    // test the value
    val actualProjects = Await.result(sbtBuild.projects(), 1.second)
    assertEquals("Wrong projects", expectedProjects, actualProjects)
  }

}