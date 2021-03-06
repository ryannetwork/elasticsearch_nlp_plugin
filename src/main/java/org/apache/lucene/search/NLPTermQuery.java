/*
 *  Copyright: (c) 2019 Mayo Foundation for Medical Education and
 *  Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 *  triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 *  Except as contained in the copyright notice above, or as used to identify
 *  MFMER as the author of this software, the trade names, trademarks, service
 *  marks, or product names of the copyright holder shall not be used in
 *  advertising, promotion or otherwise in connection with this software without
 *  prior written authorization of the copyright holder.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.lucene.search;

import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.TermStates;
import org.apache.lucene.search.components.NLPQueryWeight;
import org.ohnlp.elasticsearchnlp.lucene.NLPTerm;

import java.io.IOException;
import java.util.Objects;

/**
 * Much of this is copied from lucene with provisions for NLPTerms
 */
public class NLPTermQuery extends Query {

    private final NLPTerm term;
    private final TermStates perReaderTermState;


    public NLPTermQuery(NLPTerm t) {
        term = Objects.requireNonNull(t);
        perReaderTermState = null;
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, ScoreMode scoreMode, float boost) throws IOException {
        final IndexReaderContext context = searcher.getTopReaderContext();
        final TermStates termState;
        if (perReaderTermState == null
                || perReaderTermState.wasBuiltFor(context) == false) {
            termState = TermStates.build(context, term.getTerm(), scoreMode.needsScores());
        } else {
            // PRTS was pre-build for this IS
            termState = this.perReaderTermState;
        }
        return new NLPQueryWeight(searcher, boost, scoreMode, termState, this, term, term.getTerm().field());
    }

    @Override
    public String toString(String field) {
        return "NLP-Backed Term Query on " + term.getTerm() + " with nlp payload of " + term.getPyld();
    }

    @Override
    public boolean equals(Object other) {
        return sameClassAs(other) &&
                term.equals(((NLPTermQuery) other).term);
    }

    @Override
    public int hashCode() {
        return classHash() ^ term.hashCode();
    }
}
