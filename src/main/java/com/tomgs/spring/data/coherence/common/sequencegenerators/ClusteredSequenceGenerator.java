/*
 * File: ClusteredSequenceGenerator.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.tomgs.spring.data.coherence.common.sequencegenerators;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;
import com.tomgs.spring.data.coherence.common.ranges.Range;
import com.tomgs.spring.data.coherence.common.ranges.Ranges;

/**
 * A {@link ClusteredSequenceGenerator} is an implementation of a
 * {@link SequenceGenerator} that uses Coherence to maintain a cluster-scoped
 * specifically named sequence (based on a Coherence Cluster).
 *
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClusteredSequenceGenerator implements SequenceGenerator {
	/**
	 * The name of the sequence.
	 */
	private String sequenceName;

	/**
	 * The initial value of the sequence to be used if the named sequence has
	 * not been defined in the cluster.
	 */
	private long initialValue;
	private long maxValue;
	private long timeout;
	private long ageMillis = 0;

	/**
	 * Standard Constructor.
	 *
	 * @param sequenceName
	 *            The name of the sequence being maintained
	 * @param initialValue
	 *            The initial value of the sequence if it does not exist in the
	 *            cluster.
	 * @param maxValue
	 *            The maxvalue of the sequence
	 * @param timeout
	 *            The sequence's timeout(millis),if timeout, this sequence will
	 *            be removed
	 */
	public ClusteredSequenceGenerator(String sequenceName, long initialValue,
			long maxValue, long timeout) {
		this.sequenceName = sequenceName;
		this.initialValue = initialValue;
		this.maxValue = maxValue;
		this.timeout = timeout;
	}

	/**
	 * {@inheritDoc}
	 */
	public long next() {
		return next(1).getFrom();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getAgeMillis() {
		return ageMillis;
	}

	/**
	 * {@inheritDoc}
	 */
	public Range next(long sequenceSize) {
		NamedCache namedCache = CacheFactory.getCache(State.CACHENAME);
		State state = (State) namedCache.invoke(sequenceName,
				new GenerateSequenceNumberProcessor(initialValue, maxValue,
						sequenceSize, timeout));

		long from = state.getValue() - sequenceSize;
		ageMillis = timeout - state.getElapsedMillis();

		return Ranges.newRange(from, from + sequenceSize - 1);
	}

	/**
	 * The {@link GenerateSequenceNumberProcessor} is used to generate
	 * one-or-more sequence numbers from a named
	 * {@link ClusteredSequenceGenerator} (using the {@link State}).
	 */
	@SuppressWarnings("serial")
	public static class GenerateSequenceNumberProcessor extends
			AbstractProcessor implements ExternalizableLite, PortableObject {
		/**
		 * The number of sequence numbers to generate (skip after generating the
		 * first).
		 */
		private long sequenceSize;

		/**
		 * The initial value of the sequence, if it does not exist.
		 */
		private long initialValue;
		private long maxValue;

		/**
		 * The value of timout.
		 */
		private long timeout;

		/**
		 * Required for {@link ExternalizableLite} and {@link PortableObject}.
		 */
		public GenerateSequenceNumberProcessor() {
			this.sequenceSize = 1;
			this.initialValue = 1;
			this.maxValue = 1;
			this.timeout = 3600000;
		}

		/**
		 * Standard Constructor.
		 *
		 * @param initialValue
		 *            Used iff the underlying sequence does not exist
		 * @param sequenceSize
		 *            The number of values to generate (allocate from the
		 *            sequence)
		 */
		public GenerateSequenceNumberProcessor(long initialValue,
				long maxValue, long sequenceSize, long timeout) {
			this.initialValue = initialValue;
			this.maxValue = maxValue;
			this.sequenceSize = sequenceSize;
			this.timeout = timeout;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object process(Entry entry) {
			State state;
			BinaryEntry binaryEntry = (BinaryEntry) entry;
			if (binaryEntry.isPresent()) {

				state = (State) binaryEntry.getValue();

			} else {
				state = new State(initialValue);
			}

			long value = state.generate(sequenceSize);

			long ageMillis = timeout - state.getElapsedMillis();

			if (ageMillis <= 0) {
				binaryEntry.remove(false);
				System.out.println("entry removed");
			} else {
				if (value >= maxValue) {
					if (binaryEntry.getExpiry() > ageMillis)
						binaryEntry.expire(ageMillis);
				} else
					binaryEntry.setValue(state);
			}

			return state;
		}

		/**
		 * {@inheritDoc}
		 */
		public void readExternal(DataInput in) throws IOException {

			this.initialValue = ExternalizableHelper.readLong(in);
			this.maxValue = ExternalizableHelper.readLong(in);
			this.sequenceSize = ExternalizableHelper.readLong(in);
			this.timeout = ExternalizableHelper.readLong(in);
		}

		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(DataOutput out) throws IOException {
			ExternalizableHelper.writeLong(out, initialValue);
			ExternalizableHelper.writeLong(out, maxValue);
			ExternalizableHelper.writeLong(out, sequenceSize);
			ExternalizableHelper.writeLong(out, timeout);
		}

		/**
		 * {@inheritDoc}
		 */
		public void readExternal(PofReader reader) throws IOException {
			this.initialValue = reader.readLong(0);
			this.maxValue = reader.readLong(1);
			this.sequenceSize = reader.readLong(2);
			this.timeout = reader.readLong(3);
		}

		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(PofWriter writer) throws IOException {
			writer.writeLong(0, initialValue);
			writer.writeLong(1, maxValue);
			writer.writeLong(2, sequenceSize);
			writer.writeLong(3, timeout);
		}
	}

	/**
	 * The {@link State} class represents the next available sequence number for
	 * a named {@link ClusteredSequenceGenerator} in a Coherence Cache.
	 */
	@SuppressWarnings("serial")
	public static class State implements ExternalizableLite, PortableObject {
		/**
		 * The name of the Coherence Cache that will store
		 * {@link ClusteredSequenceGenerator} {@link State}s.
		 */
		public static final String CACHENAME = "coherence.common.sequencegenerators";

		/**
		 * The next value that will be issued for the named
		 * {@link ClusteredSequenceGenerator}.
		 */
		private long nextValue;

		private long createMillis;

		/**
		 * Required for {@link ExternalizableLite} and {@link PortableObject}.
		 */
		public State() {
		}

		/**
		 * Standard Constructor.
		 *
		 * @param initialValue
		 *            The initial value of the sequence.
		 */
		public State(long initialValue) {
			this.nextValue = initialValue;
			this.createMillis = com.tangosol.util.Base.getSafeTimeMillis();
		}

		/**
		 * Generates a specified number of sequential ids and returns the first
		 * number in the sequence.
		 *
		 * @param sequenceSize
		 *            The number of sequential ids to generate
		 *
		 * @return The first value in the sequence
		 */
		public long generate(long sequenceSize) {
			long start = nextValue;

			nextValue = nextValue + sequenceSize;

			return start;
		}

		public long getElapsedMillis() {
			return com.tangosol.util.Base.getSafeTimeMillis() - createMillis;
		}

		public long getValue() {
			return nextValue;
		}

		/**
		 * {@inheritDoc}
		 */
		public void readExternal(DataInput in) throws IOException {
			this.nextValue = ExternalizableHelper.readLong(in);
			this.createMillis = ExternalizableHelper.readLong(in);
		}

		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(DataOutput out) throws IOException {
			ExternalizableHelper.writeLong(out, nextValue);
			ExternalizableHelper.writeLong(out, createMillis);
		}

		/**
		 * {@inheritDoc}
		 */
		public void readExternal(PofReader reader) throws IOException {
			this.nextValue = reader.readLong(0);
			this.createMillis = reader.readLong(1);
		}

		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(PofWriter writer) throws IOException {
			writer.writeLong(0, nextValue);
			writer.writeLong(1, createMillis);
		}

		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return String
					.format("ClusteredSequenceGenerator.State{nextValue=%d}",
							nextValue);
		}
	}
}
