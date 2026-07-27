# language: ru
Функционал: Локальные контракты шагов проверки API-ответов

  Сценарий: Сравнение JSON и проверка непустого массива
    Дано подготовлен локальный JSON-ответ "local_json":
      """
      {"items":[{"name":"alpha"},{"name":"beta"}]}
      """
    Тогда в ответе "local_json" содержимое равно json "local_json_body"
    И в ответе "local_json" массив значений найденных по jsonPath "items.name" не пустой

  Сценарий: Проверки массивов YAML
    Дано подготовлен локальный YAML-ответ "local_yaml":
      """
      items:
        - name: alpha-contract
          status: active
          date: "2026-07-20T10:00:00+03:00"
        - name: beta-contract
          status: active
          date: "2026-07-21T10:00:00+03:00"
      """
    И подготовлен локальный YAML-ответ "local_yaml_desc":
      """
      items:
        - name: beta-contract
        - name: alpha-contract
      """
    Тогда в ответе "local_yaml" содержимое равно yaml "local_yaml_body"
    И в ответе "local_yaml" массив значений найденных по yamlPath "items.status" все значения равны "active"
    И в ответе "local_yaml" массив значений найденных по yamlPath "items.name" все значения содержат "contract"
    И в ответе "local_yaml" массив значений найденных по yamlPath "items.name" размер 2
    И в ответе "local_yaml" массив значений найденных по yamlPath "items.name" отсортирован по возрастанию
    И в ответе "local_yaml_desc" массив значений найденных по yamlPath "items.name" отсортирован по убыванию
    И в ответе "local_yaml" массив значений найденных по yamlPath "items.date" в периоде между "2026-07-20T00:00:00+03:00" и "2026-07-22T00:00:00+03:00" в формате "yyyy-MM-dd'T'HH:mm:ssXXX"

  Сценарий: Сортировка и диапазон дат в XML
    Дано подготовлен локальный XML-ответ "local_xml_asc":
      """
      <items>
        <item><name>alpha</name><date>2026-07-20T10:00:00+03:00</date></item>
        <item><name>beta</name><date>2026-07-21T10:00:00+03:00</date></item>
      </items>
      """
    И подготовлен локальный XML-ответ "local_xml_desc":
      """
      <items>
        <item><name>beta</name></item>
        <item><name>alpha</name></item>
      </items>
      """
    Тогда в ответе "local_xml_asc" список значений найденных по xPath "//item/name" отсортирован по возрастанию
    И в ответе "local_xml_desc" список значений найденных по xPath "//item/name" отсортирован по убыванию
    И в ответе "local_xml_asc" список значений найденных по xPath "//item/date" в периоде между "2026-07-20T00:00:00+03:00" и "2026-07-22T00:00:00+03:00" в формате "yyyy-MM-dd'T'HH:mm:ssXXX"
