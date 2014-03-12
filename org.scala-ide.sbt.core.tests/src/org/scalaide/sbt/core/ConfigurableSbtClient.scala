package org.scalaide.sbt.core

import sbt.client.SbtClient
import sbt.protocol.MinimalBuildStructure
import sbt.client.Subscription

// TODO: need multithreading support?
class ConfigurableSbtClient extends SbtClient {

	// Members declared in java.io.Closeable
  
  def close(): Unit = ???
  
  // Members declared in sbt.client.SbtClient
  
  def handleEvents(listener: sbt.client.Event => Unit)(implicit ex: scala.concurrent.ExecutionContext): sbt.client.Subscription = ???
  
  def lookupScopedKey(name: String): scala.concurrent.Future[Seq[sbt.client.ScopedKey]] = ???
  
  def possibleAutocompletions(partialCommand: String,detailLevel: Int): scala.concurrent.Future[Set[sbt.client.Completion]] = ???
  
  def requestExecution(commandOrTask: String,interaction: Option[(sbt.client.Interaction, scala.concurrent.ExecutionContext)]): scala.concurrent.Future[Unit] = ???
  
  def watch[T](key: sbt.client.TaskKey[T])(l: (sbt.client.ScopedKey, sbt.client.TaskResult[T]) => Unit)(implicit ex: scala.concurrent.ExecutionContext): sbt.client.Subscription = ???
  
  def watch[T](key: sbt.client.SettingKey[T])(listener: (sbt.client.ScopedKey, sbt.client.TaskResult[T]) => Unit)(implicit ex: scala.concurrent.ExecutionContext): sbt.client.Subscription = ???
  
  def watchBuild(listener: sbt.client.MinimalBuildStructure => Unit)(implicit ex: scala.concurrent.ExecutionContext): Subscription = {
    buildListeners ::= listener
    
    build.foreach{b => listener(b)}
    
    new Subscription {
      override def cancel() {
        buildListeners = buildListeners.filter(_ != listener)
      }
    }
  }
  
  // configuration
  
  private var build: Option[MinimalBuildStructure] = None
  
  private var buildListeners = List[sbt.client.MinimalBuildStructure => Unit]() 
  
  def setBuild(b: MinimalBuildStructure) {
    build = Some(b)
    buildListeners.foreach{ l =>
      l(b)
    }
  }
 
}