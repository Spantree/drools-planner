/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.benchmark.core.statistic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.drools.planner.benchmark.core.ProblemBenchmark;
import org.drools.planner.benchmark.core.SingleBenchmark;

public abstract class AbstractProblemStatistic implements ProblemStatistic {

    protected final ProblemBenchmark problemBenchmark;
    protected final ProblemStatisticType problemStatisticType;

    protected File csvStatisticFile = null;
    protected File graphStatisticFile = null;

    protected AbstractProblemStatistic(ProblemBenchmark problemBenchmark, ProblemStatisticType problemStatisticType) {
        this.problemBenchmark = problemBenchmark;
        this.problemStatisticType = problemStatisticType;
    }

    public ProblemBenchmark getProblemBenchmark() {
        return problemBenchmark;
    }

    public ProblemStatisticType getProblemStatisticType() {
        return problemStatisticType;
    }

    public String getAnchorId() {
        return problemBenchmark.getName() + "_" + problemStatisticType.name();
    }

    public String getCsvFilePath() {
        return problemBenchmark.getProblemReportDirectory().getName() + "/" + csvStatisticFile.getName();
    }

    public String getGraphFilePath() {
        return problemBenchmark.getProblemReportDirectory().getName() + "/" + graphStatisticFile.getName();
    }

    public void writeStatistic() {
        writeCsvStatistic();
        writeGraphStatistic();
    }

    protected abstract void writeCsvStatistic();

    protected abstract void writeGraphStatistic();

    public class ProblemStatisticCsv {

        private Map<Long, ProblemStatisticCsvLine> timeToLineMap = new HashMap<Long, ProblemStatisticCsvLine>();

        public void addPoint(SingleBenchmark singleBenchmark, long timeMillisSpend, long value) {
            addPoint(singleBenchmark, timeMillisSpend, Long.toString(value));
        }

        public void addPoint(SingleBenchmark singleBenchmark, long timeMillisSpend, double value) {
            addPoint(singleBenchmark, timeMillisSpend, Double.toString(value));
        }

        public void addPoint(SingleBenchmark singleBenchmark, long timeMillisSpend, String value) {
            ProblemStatisticCsvLine line = findOrCreateLine(timeMillisSpend);
            line.getValueMap().put(singleBenchmark, value);
        }

        protected ProblemStatisticCsvLine findOrCreateLine(long timeMillisSpend) {
            ProblemStatisticCsvLine line = timeToLineMap.get(timeMillisSpend);
            if (line == null) {
                line = new ProblemStatisticCsvLine(timeMillisSpend);
                timeToLineMap.put(timeMillisSpend, line);
            }
            return line;
        }

        public void writeCsvStatisticFile() {
            List<ProblemStatisticCsvLine> lines = new ArrayList<ProblemStatisticCsvLine>(timeToLineMap.values());
            Collections.sort(lines);
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(csvStatisticFile), "UTF-8");
                writer.append("\"TimeMillisSpend\"");
                for (SingleBenchmark singleBenchmark : problemBenchmark.getSingleBenchmarkList()) {
                    writer.append(",\"").append(singleBenchmark.getSolverBenchmark().getName()
                            .replaceAll("\\\"", "\\\"")).append("\"");
                }
                writer.append("\n");
                for (ProblemStatisticCsvLine line : lines) {
                    writer.write(Long.toString(line.getTimeMillisSpend()));
                    for (SingleBenchmark singleBenchmark : problemBenchmark.getSingleBenchmarkList()) {
                        writer.append(",");
                        String value = line.getValueMap().get(singleBenchmark);
                        if (value != null) {
                            writer.append(value);
                        }
                    }
                    writer.append("\n");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Problem writing csvStatisticFile: " + csvStatisticFile, e);
            } finally {
                IOUtils.closeQuietly(writer);
            }
        }

    }

    public static class ProblemStatisticCsvLine implements Comparable<ProblemStatisticCsvLine> {

        protected long timeMillisSpend;
        protected Map<SingleBenchmark, String> valueMap = new HashMap<SingleBenchmark, String>();

        private ProblemStatisticCsvLine(long timeMillisSpend) {
            this.timeMillisSpend = timeMillisSpend;
        }

        public long getTimeMillisSpend() {
            return timeMillisSpend;
        }

        public Map<SingleBenchmark, String> getValueMap() {
            return valueMap;
        }

        public int compareTo(ProblemStatisticCsvLine other) {
            return timeMillisSpend < other.timeMillisSpend ? -1 : (timeMillisSpend > other.timeMillisSpend ? 1 : 0);
        }

    }

}
