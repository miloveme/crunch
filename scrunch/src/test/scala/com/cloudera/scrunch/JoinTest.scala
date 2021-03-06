/**
 * Copyright (c) 2011, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.scrunch

import com.cloudera.crunch.io.{From => from, To => to}
import com.cloudera.crunch.test.FileHelper

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test

class JoinTest extends AssertionsForJUnit {
  val pipeline = new Pipeline[CogroupTest]

  def wordCount(fileName: String) = {
    pipeline.read(from.textFile(fileName))
        .flatMap(_.toLowerCase.split("\\W+")).count
  }

  @Test def join {
    val shakespeare = FileHelper.createTempCopyOf("shakes.txt")
    val maugham = FileHelper.createTempCopyOf("maugham.txt")
    val output = FileHelper.createOutputPath()
    output.deleteOnExit()
    val filtered = wordCount(shakespeare).join(wordCount(maugham))
        .map((k, v) => (k, v._1 - v._2))
        .write(to.textFile(output.getAbsolutePath()))
        .filter((k, d) => d > 0).materialize
    assertTrue(filtered.exists(_ == ("macbeth", 66)))
    pipeline.done
  }
}
