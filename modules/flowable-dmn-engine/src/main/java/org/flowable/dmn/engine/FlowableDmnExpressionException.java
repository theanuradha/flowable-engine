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
package org.flowable.dmn.engine;

import org.flowable.engine.common.api.FlowableException;

public class FlowableDmnExpressionException extends FlowableException {

    private static final long serialVersionUID = 1L;

    protected String expression;

    public FlowableDmnExpressionException(String message, String expression, Throwable cause) {
        super(message, cause);
        this.expression = expression;
    }

    public FlowableDmnExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowableDmnExpressionException(String message) {
        super(message);
    }

    public String getExpression() {
        return expression;
    }
}
