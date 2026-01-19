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
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import ru.at.library.core.utils.helpers.AssertionHelper;
import ru.at.library.core.utils.helpers.ScopedVariables;

/**
 * Главный класс, отвечающий за сопровождение тестовых шагов
 */
@Slf4j
public final class CoreScenario {

    private static CoreScenario instance = new CoreScenario();

    /**
     * Среда прогона тестов, хранит в себе: Cucumber.Scenario,
     * переменные, объявленные пользователем в сценарии и страницы, тестирование которых будет производиться
     */
    private static final ThreadLocal<CoreEnvironment> environment = new ThreadLocal<>();

    private static final ThreadLocal<AssertionHelper> assertionHelper = new ThreadLocal<>();

    private CoreScenario() {
    }

    public static CoreScenario getInstance() {
        return instance;
    }



    public CoreEnvironment getEnvironment() {
        return environment.get();
    }

    public AssertionHelper getAssertionHelper() {return assertionHelper.get(); }

    @Step("Создание Page и переменных для сценария")
    public void setEnvironment(CoreEnvironment coreEnvironment) {
        environment.set(coreEnvironment);
    }

    public void setAssertionHelper(AssertionHelper assertionHlp) { assertionHelper.set(assertionHlp);}

    /**
     * Возвращает текущий сценарий (Cucumber.api)
     */
    public Scenario getScenario() {
        return this.getEnvironment().getScenario();
    }


    /**
     * Получение переменной по имени, заданного пользователем, из пула переменных "variables" в CoreEnvironment.
     * Если переменная не найдена, выбрасывается {@link IllegalArgumentException}.
     *
     * @param name имя переменной, для которой необходимо получить ранее сохранённое значение
     */
    public Object getVar(String name) {
        Object obj = this.getEnvironment().getVar(name);
        if (obj == null) {
            throw new IllegalArgumentException("Переменная " + name + " не найдена");
        }
        return obj;
    }

    /**
     * Типобезопасное получение переменной по имени с проверкой типа.
     * Удобно использовать вместо ручного приведения типов:
     * <pre>
     *   Response resp = coreScenario.getVar("response", Response.class);
     * </pre>
     * Если переменная отсутствует или имеет другой тип, выбрасывается {@link IllegalArgumentException}.
     */
    public <T> T getVar(String name, Class<T> type) {
        Object value = getVar(name);
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(String.format(
                    "Переменная '%s' имеет тип %s, ожидается %s",
                    name,
                    value != null ? value.getClass().getName() : "null",
                    type.getName()
            ));
        }
        return type.cast(value);
    }

    /**
     * Получение переменной без проверки на NULL.
     * Возвращает значение переменной или {@code null}, если переменная отсутствует.
     */
    public Object tryGetVar(String name) {
        return this.getEnvironment().getVar(name);
    }


    /**
     * Добавление переменной в пул "variables" в классе CoreEnvironment
     *
     * @param name   имя переменной заданное пользователем, для которого сохраняется значение. Является ключом в пуле variables в классе CoreEnvironment
     * @param object значение, которое нужно сохранить в переменную
     */
    public void setVar(String name, Object object) {
        this.getEnvironment().setVar(name, object);
    }

    /**
     * Получение всех переменных из пула "variables" в классе CoreEnvironment
     */
    public ScopedVariables getVars() {
        return this.getEnvironment().getVariables().get();
    }
}
