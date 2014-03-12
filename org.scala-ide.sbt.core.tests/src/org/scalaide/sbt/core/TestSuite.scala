package org.scalaide.sbt.core

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(classOf[Suite])
@Suite.SuiteClasses(
  Array(classOf[SbtBuildTest])
)
class TestsSuite { }