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
package com.cloudera.crunch.io.seq;

import java.io.IOException;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;

import com.cloudera.crunch.io.CompositePathIterable;
import com.cloudera.crunch.io.ReadableSourceTarget;
import com.cloudera.crunch.io.SourceTargetHelper;
import com.cloudera.crunch.type.PType;

public class SeqFileSourceTarget<T> extends SeqFileTarget implements ReadableSourceTarget<T> {

  private static final Log LOG = LogFactory.getLog(SeqFileSourceTarget.class);
  
  private final PType<T> ptype;
  
  public SeqFileSourceTarget(String path, PType<T> ptype) {
    this(new Path(path), ptype);
  }
  
  public SeqFileSourceTarget(Path path, PType<T> ptype) {
    super(path);
    this.ptype = ptype;
  }
  
  @Override
  public boolean equals(Object other) {
    if (other == null || !(other instanceof SeqFileSourceTarget)) {
      return false;
    }
    SeqFileSourceTarget o = (SeqFileSourceTarget) other;
    return path.equals(o.path);
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(path).toHashCode();
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public String toString() {
    return "SeqSourceTarget(" + path.toString() + ")";
  }

  @Override
  public PType<T> getType() {
    return ptype;
  }

  @Override
  public void configureSource(Job job, int inputId) throws IOException {
    SourceTargetHelper.configureSource(job, inputId, SequenceFileInputFormat.class, path);
  }

  @Override
  public long getSize(Configuration configuration) {
    try {
      return SourceTargetHelper.getPathSize(configuration, path);
    } catch (IOException e) {
      LOG.info(String.format("Exception thrown looking up size of: %s", path), e);
    }
    return 1L;
  }
  
  @Override
  public Iterable<T> read(Configuration conf) throws IOException {
	FileSystem fs = FileSystem.get(conf);
	return CompositePathIterable.create(fs, path, 
	    new SeqFileReaderFactory<T>(ptype, conf));
  }
}
