package org.scalaide.sbt.core.builder

import java.util.{ Map => JMap }
import scala.tools.eclipse.logging.HasLogger
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.IProgressMonitor
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalaide.sbt.core.SbtBuild

class RemoteBuilder extends IncrementalProjectBuilder with HasLogger {

  override def build(kind: Int, args: JMap[String, String], monitor: IProgressMonitor): Array[IProject] = {
    
    val build = SbtBuild.buildFor(getProject().getLocation().toFile())
    
    build.map(_.compile(/*TODO: real project name*/""))
    
    // TODO: get the compilation result (errors, ...)
    // TODO: refresh the output folders

    if (/*TODO: check result of the compilation*/true) {
      getProject.getReferencedProjects()
    } else {
      Array()
    }
  }

}
