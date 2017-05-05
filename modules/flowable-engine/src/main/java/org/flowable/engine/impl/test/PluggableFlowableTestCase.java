/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.flowable.engine.impl.test;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.common.api.FlowableException;
import org.flowable.engine.impl.ProcessEngineImpl;
import org.flowable.engine.impl.cfg.CommandExecutorImpl;
import org.flowable.engine.impl.interceptor.CommandExecutor;
import org.flowable.engine.impl.interceptor.CommandInterceptor;
import org.flowable.engine.impl.interceptor.CommandInvoker;
import org.flowable.engine.impl.interceptor.LoggingExecutionTreeCommandInvoker;
import org.flowable.engine.test.EnableVerboseExecutionTreeLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the flowable test cases.
 *
 * The main reason not to use our own test support classes is that we need to run our test suite with various configurations, e.g. with and without spring, standalone or on a server etc. Those
 * requirements create some complications so we think it's best to use a separate base class. That way it is much easier for us to maintain our own codebase and at the same time provide stability on
 * the test support classes that we offer as part of our api (in org.flowable.engine.test).
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class PluggableFlowableTestCase extends AbstractFlowableTestCase {

    private static Logger pluggableActivitiTestCaseLogger = LoggerFactory.getLogger(PluggableFlowableTestCase.class);

    protected static ProcessEngine cachedProcessEngine;

    protected void initializeProcessEngine() {
        if (cachedProcessEngine == null) {
            pluggableActivitiTestCaseLogger.info("No cached process engine found for test. Retrieving the default engine.");
            ProcessEngines.destroy(); // Just to be sure we're not getting any previously cached version

            cachedProcessEngine = ProcessEngines.getDefaultProcessEngine();
            if (cachedProcessEngine == null) {
                throw new FlowableException("no default process engine available");
            }
        }

        processEngine = cachedProcessEngine;
        processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();

        // Enable verbose execution tree debugging if needed
        if (this.getClass().isAnnotationPresent(EnableVerboseExecutionTreeLogging.class)) {
            swapCommandInvoker(true);
        }

    }

    @Override
    protected void closeDownProcessEngine() {
        super.closeDownProcessEngine();

        // Reset command invoker
        if (this.getClass().isAnnotationPresent(EnableVerboseExecutionTreeLogging.class)) {
            swapCommandInvoker(false);
        }
    }

    protected void swapCommandInvoker(boolean debug) {
        CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
        if (commandExecutor instanceof CommandExecutorImpl) {
            CommandExecutorImpl commandExecutorImpl = (CommandExecutorImpl) commandExecutor;

            CommandInterceptor previousCommandInterceptor = null;
            CommandInterceptor commandInterceptor = commandExecutorImpl.getFirst();

            while (commandInterceptor != null) {

                boolean matches = debug ? (commandInterceptor instanceof CommandInvoker) : (commandInterceptor instanceof LoggingExecutionTreeCommandInvoker);
                if (matches) {

                    CommandInterceptor commandInvoker = debug ? new LoggingExecutionTreeCommandInvoker() : new CommandInvoker();
                    if (previousCommandInterceptor != null) {
                        previousCommandInterceptor.setNext(commandInvoker);
                    } else {
                        commandExecutorImpl.setFirst(previousCommandInterceptor);
                    }
                    break;

                } else {
                    previousCommandInterceptor = commandInterceptor;
                    commandInterceptor = commandInterceptor.getNext();
                }
            }

        } else {
            pluggableActivitiTestCaseLogger.warn("Not using {}, ignoring the {} annotation", CommandExecutorImpl.class, EnableVerboseExecutionTreeLogging.class);
        }
    }

}
