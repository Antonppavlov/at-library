/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.at.library.core.cucumber.api;

import io.cucumber.java.Scenario;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.at.library.core.utils.helpers.ScopedVariables;

/**
 * Класс, связанный с CoreScenario, используется для хранения Cucumber-сценария и переменных внутри сценария.
 * Веб-страницы и WebDriver-специфичная логика вынесены в модуль at-library-web.
 */
@Slf4j
public class CoreEnvironment {

    /**
     * Сценарий (Cucumber.api), с которым связана среда
     */
    @Getter
    private final Scenario scenario;

    /**
     * Переменные, объявленные пользователем внутри сценария
     * ThreadLocal обеспечивает отсутствие коллизий при многопоточном запуске
     */
    @Getter
    private final ThreadLocal<ScopedVariables> variables = new ThreadLocal<>();

    public CoreEnvironment(Scenario scenario) {
        this.scenario = scenario;
        variables.set(new ScopedVariables());
    }

    public Object getVar(String name) {
        return getVariables().get().get(name);
    }

    public void setVar(String name, Object object) {
        getVariables().get().put(name, object);
    }

}
