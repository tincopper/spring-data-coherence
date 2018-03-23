/*
 * Copyright 2009 Aleksandar Seovic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomgs.spring.data.coherence.core.factory;


import com.tomgs.spring.data.coherence.core.Factory;

import java.util.LinkedList;
import java.util.List;


/**
 * {@link Factory} implementation that creates a <tt>java.util.LinkedList</tt>
 * instance.
 *
 * @author Aleksandar Seovic  2010.11.08
 */
public class LinkedListFactory<E>
        extends AbstractFactory<List<E>> {

    private static final long serialVersionUID = 8380641116254657032L;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> create() {
        return new LinkedList<E>();
    }
}