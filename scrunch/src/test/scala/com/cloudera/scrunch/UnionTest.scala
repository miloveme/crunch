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

import com.cloudera.crunch.io.{From => from}
import com.cloudera.crunch.test.FileHelper

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test

class UnionTest extends AssertionsForJUnit {
  val pipeline = new Pipeline[UnionTest]
  val shakespeare = FileHelper.createTempCopyOf("shakes.txt")
  val maugham = FileHelper.createTempCopyOf("maugham.txt")

  def wordCount(col: PCollection[String]) = {
    col.flatMap(_.toLowerCase.split("\\W+")).count
  }

  @Test def unionCollection {
    val union = pipeline.read(from.textFile(shakespeare)).union(
        pipeline.read(from.textFile(maugham)))
    val wc = wordCount(union).materialize
    assertTrue(wc.exists(_ == ("you", 3691)))
    pipeline.done
  }

  @Test def unionTable {
    val wcs = wordCount(pipeline.read(from.textFile(shakespeare)))
    val wcm = wordCount(pipeline.read(from.textFile(maugham)))
    val wc = wcs.union(wcm).groupByKey.combine(v => v.sum).materialize
    assertTrue(wc.exists(_ == ("you", 3691)))
    pipeline.done
  }
}
