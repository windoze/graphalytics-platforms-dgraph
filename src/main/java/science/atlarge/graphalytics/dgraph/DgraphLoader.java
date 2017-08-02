/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package science.atlarge.graphalytics.dgraph;

import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.dgraph.DgraphConfiguration;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;


/**
 * Base class for graph loading in the platform driver.
 *
 * @author windoze
 */
public class DgraphLoader {

	private static final Logger LOG = LogManager.getLogger();

	protected CommandLine commandLine;
	protected FormattedGraph formattedGraph;
	protected DgraphConfiguration platformConfig;


	/**
	 *	Graph loader for Dgraph.
	 * @param formattedGraph
	 * @param platformConfig
	 */
	public DgraphLoader(FormattedGraph formattedGraph, DgraphConfiguration platformConfig) {
		this.formattedGraph = formattedGraph;
		this.platformConfig = platformConfig;
	}

	public int load() throws Exception {
		String loaderDir = platformConfig.getLoaderPath();
		commandLine = new CommandLine(Paths.get(loaderDir).toFile());

		commandLine.addArgument("--graph-name");
		commandLine.addArgument(formattedGraph.getName());
		commandLine.addArgument("--input-vertex-path");
		commandLine.addArgument(formattedGraph.getVertexFilePath());
		commandLine.addArgument("--input-edge-path");
		commandLine.addArgument(formattedGraph.getEdgeFilePath());
		commandLine.addArgument("--output-path");
		commandLine.addArgument(getLoadedPath(formattedGraph).toString());
		commandLine.addArgument("--directed");
		commandLine.addArgument(formattedGraph.isDirected() ? "true" : "false");
		commandLine.addArgument("--weighted");
		commandLine.addArgument(formattedGraph.hasEdgeProperties() ? "true" : "false");


		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
		LOG.info(String.format("Execute graph loader with command-line: [%s]", commandString));

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);

		return executor.execute(commandLine);
	}

	public int unload() throws Exception {
		String unloaderDir = platformConfig.getUnloaderPath();
		commandLine = new CommandLine(Paths.get(unloaderDir).toFile());

		commandLine.addArgument("--graph-name");
		commandLine.addArgument(formattedGraph.getName());

		commandLine.addArgument("--output-path");
		commandLine.addArgument(getLoadedPath(formattedGraph).toString());

		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
		LOG.info(String.format("Execute graph unloader with command-line: [%s]", commandString));

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);

		return executor.execute(commandLine);
	}

	public static String getLoadedPath(FormattedGraph formattedGraph) {
		Path intermediatePath = Paths.get("./intermediate").resolve(formattedGraph.getName());
		return intermediatePath.toAbsolutePath().toString();
	}

}
