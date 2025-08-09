# event-driven-microservices-app

Я прохожу курс
[Event-Driven Microservices: Spring Boot, Kafka and Elastic](https://www.udemy.com/course/event-driven-microservices-spring-boot-kafka-and-elasticsearch/)
на платформе Udemy.com.

Курс посвящён созданию приложения, в котором будут взаимодействовать между собой несколько микросервисов 
под управлением Spring Cloud, с использованием паттернов Event Sourcing и Event Driven.

![схема приложения](pictures/app-structure.jpg)

С большинством технологий, которые будут применяться в проекте, я так или иначе знакома. Новыми для меня станут 
Keycloak, Kafka Streams, ELK-stack.

## technologies

Курс неактуальный, 2020 года (судя по версии Spring Boot 2.3.4). Прохожу я его летом 2025 года. Писать буду
на Java 17 и Spring Boot 3.5.0, меняя библиотеки и технологии на ходу. Поэтому готовлюсь много гуглить,
особенно по работе со Spring Cloud (там чуть ли не все компоненты кардинально поменялись) :smile:.

Решила вести таблицу, какие более новые технологии я использовала.

| Что было в курсе | Что я использовала |
|:-:|:-:|
| Java 11 | Java 17 |
| Spring Boot 2.3.4 | Spring Boot 3.5.0 |
| Spring Cloud 2.2.6.RELEASE | Spring Cloud 4.3.0 |
| Logback | Log4j2 |
| Apache Httpclient | Apache Httpclient5 |
| Apache Kafka + Zookeeper | Apache Kafka KRaft mode |
| springdoc-openapi-ui | springdoc-openapi-starter-webmvc-ui |

## pre-requisites

Перед запуском микросервисов необходимо развернуть инфраструктуру с помощью Docker Compose и настроить Keycloak
для аутентификации.

<details>
<summary>Запуск Docker Compose</summary>
В терминале войти в директорию `docker-compose`, запустить контейнер, содержащий сервисы:

```bash
docker-compose up -d
```

Список запущенных сервисов:
| Сервис | Порт | Назначение |
|:-------|:----:|:-----------|
|Kafka Brokers | 9091-9093 | Брокеры сообщений |
| Schema Registry | 8181 | Реестр схем Avro |
| Elasticsearch | 9200 | NoSQL база данных для поиска и аналитики |
| Postgres | 5433 | База данных для Keycloak |
| Keycloak | 8080 | Сервер аутентификации |
</details>

<details>
<summary>Настройка Keycloak</summary>

1. В браузере войти в UI Keycloak (`http://localhost:8080/`) под учётными данными администратора:
- :key: `admin`
- :lock: `admin`

2. Создать realm (область/домен):
- левое меню: "Manage realms"
- выбрать "Create realm"
- в поле "Realm name" name ввести имя домена (например, `twitter-service-realm`) → "Create"

3. Настроить roles (уровни доступа):
- левое меню: "Realm roles"
- выбрать "Create role"
- в поле "Role name" ввести название роли для пользователя (например, `app_user_role`) → "Save"
- аналогичным способом создать роли для пользователя с расширенными привилегиями и администратора
(например, `app_super_user_role` и `app_admin_role`)

4. Создать groups (группы уровней доступа):
- левое меню: "Groups"
- выбрать "Create group"
- в поле "Name" ввести название группы для пользователей (например, `app_user_group`) → "Create"
- кликнуть на имя созданной группы → "Role mapping" → "Assign role" → "Realm roles" → выбрать 
соответствующую роль
- аналогично создать группы для привилегированных пользователей и администраторов (например,
`app_super_user_group` и `app_admin_group`), назначить им соответствующие роли

5. Создать users (пользователей):
- левое меню: "Users"
- выбрать "Create new user"
- в поле "Username" ввести имя пользователя (например, `app_user`) → "Join Groups" → выбрать соответствующую
группу → "Join" → "Create"
- выбрать "Credentials" → "Set password" → дважды ввести пароль пользователя, перевести "Temporary" 
в положение "Off" → "Save" → "Save password"
- аналогично создать администратора и пользователя с расширенными привилегиями (например, `app_admin` и 
`app_super_user`), включить их в соответствующие группы

6. Создать Client (клиента):
- левое меню: "Clients"
- выбрать "Create client", заполнить настройки нового клиента:
    - "Client ID" - `elastic-query-web-client`
    - "Client authentication" - перевести в положение "On"
    - "Home URL" - `http://localhost:8184/elastic-query-web-client`
    - "Valid Redirect URIs":
        - `http://localhost:8184/elastic-query-web-client/login/oauth2/code/keycloak`
        - `http://localhost:8184/elastic-query-web-client`
    - "Web origins" - `http://localhost:8184`
- сохранить клиента кнопкой "Save"

7. Настроить Mappers (мапперы):
- левое меню: "Clients"
- кликнуть на имя клиента → "Client scopes" → `elastic-query-web-client-dedicated` → "Add predefined mappers"
- найти маппер под названием "groups" → "Add"
- "Add mapper" → "By configuration" → выбрать "Audience" → в поле "Name" ввести  
`elastic-query-service` → "Save"
- "Add mapper" → "By configuration" → выбрать "User Session Note", заполнить настройки:
    - "Name" - client-id
    - "User Session Note" - clientID
    - "Token Claim Name" - clientID
- аналогично создать ещё два маппера "User Session Note" со следующими настройками:
    - "Name" - client-host, "User Session Note" - clientHost, "Token Claim Name" - clientHost
    - "Name" - client-ip, "User Session Note" - clientIPAddress, "Token Claim Name" - clientIPAddress

8. Настроить Client Scopes (области доступов клиентов):
- левое меню: "Client scopes"
- выбрать "Create client scope", заполнить настройки:
  - "Name" - ввести название роли для пользователя (например, `app_user_role`)
  - "Type" - из выпадающего меню выбрать "Default"
- сохранить область доступа кнопкой "Save"
- перейти на вкладку "Scope" → "Assign role" → "Realm roles" → выбрать ранее созданную роль пользователя
→ "Assign"
- аналогично создать области доступов для администратора и привилегированного пользователя (например,
`app_admin_role` и `app_super_user_role`), назначить им соответствующие роли

9. Создать ещё одного Client (клиента):
- левое меню: "Clients"
- выбрать "Create client", заполнить настройки:
  - "Client ID" - `elastic-query-service`
  - "Client authentication" - перевести в положение "On"
  - "Valid Redirect URIs" - `http://localhost:8184/elastic-query-service/login/oauth2/code/keycloak`
- сохранить клиента кнопкой "Save"
- добавить мапперы, аналогичные описанным в пункте 7:
  - маппер "groups"
  - три маппера "User Session Note"
  - два маппера "Audience" с полями "Name":
    - `kafka-streams-service`
    - `analytics-service`
</details>

## twitter-to-kafka-service

Микросервис предназначен для получения потока твитов из социальной сети X (ex-Twitter) по заданным ключевым
словам. Работает как kafka-producer, отправляет полученные/сгенерированные твиты в топик `twitter-topic`.

Поработать с настоящим потоком твитов мне так и не удалось. Я зарегистрировала аккаунт разработчика в 
X, но, как оказалось, с 2023 года на бесплатном аккаунте получение стриммингового потока 
недоступно. Ни с помощью библиотеки `twitter4j-stream`, ни с помощью класса `TwitterV2StreamHelper`, 
написанного мною по аналогии с репозиторием `Twitter V2 API` от самих разработчиков Twitter :angry: 
Единственное, что мне удалось получить, это десяток твитов через одноразовый REST-запрос в Postman. 
Минимальный базовый тарифный план, на котором доступен стримминговый поток твитов, стоит 175-200$/месяц.

Тем не менее, классы и конфигурации я сохранила:

- класс `TwitterKafkaStreamRunner` работает на основе библиотеки `twitter4j-stream`. Для организации 
стримминга твитов следует поместить в папку resources конфигурационный файл `twitter4j.properties`. 
Ключи и токены нужно сгенерировать в аккаунте разработчика. Пример файла:

```properties
debug=true
oauth.consumerKey=*********************
oauth.consumerSecret=******************************************
oauth.accessToken=**************************************************
oauth.accessTokenSecret=******************************************
```

- класс `TwitterV2KafkaStreamRunner` работает на основе класса `TwitterV2StreamHelper`. В этом случае
для организации стримминга твитов не нужен отдельный файл конфигурации, достаточно настроек в
`application.yaml`. Но потребуется BearerToken, который также генерируется в аккаунте разработчика.

- класс `MockKafkaStreamRunner` генерирует поток псевдотвитов, собираемых из набора слов с добавлением 
одного случайно выбранного ключевого слова.

## kafka-to-elastic-service

Микросервис получает потоки твитов, индексирует их и сохраняет в Elasticsearch (индекс `twitter-index`).
Работает как kafka-consumer, принимает твиты из топика `twitter-topic`.

## elastic-query-services & elastic-query-web-clients

Серверные микросервисы предоставляют доступ к содержимому индекса `twitter-index` по запросу:
- модуль `elastic-query-service` реализован с использованием традиционного (блокирующего) подхода.  
Базовый URL: `http://localhost:8183/elastic-query-service`.
- модуль `elastic-query-service-reactive` реализован в реактивном (неблокирующем) стиле.  
Базовый URL: `http://localhost:8183/elastic-query-service-reactive`.

| Функция                                 | Описание                                                                      |               Блокирующая версия               |        Реактивная версия        |
|:----------------------------------------|:------------------------------------------------------------------------------|:----------------------------------------------:|:-------------------------------:|
| Авторизация                             | Доступ к API                                                                  | :key:&nbsp;`test` <br/> :lock:&nbsp;`test1234` | :unlock:&nbsp;Не&nbsp;требуется |
| GET `/documents`                        | Получение всех твитов                                                         |               :white_check_mark:               |               :x:               |
| GET `/documents/{id}`                   | Получение твита по id                                                         |               :white_check_mark:               |               :x:               |
| POST&nbsp;`/documents/by‑text`          | Поиск твитов по тексту. <br/> Пример запроса JSON: <br/> `{ "text": "java" }` |               :white_check_mark:               |       :white_check_mark:        |
| Swagger&nbsp;UI&nbsp;`/swagger‑ui.html` | Интерактивная документация                                                    |               :white_check_mark:               |               :x:               |
 
Клиентские микросервисы предоставляют минималистичный UI-интерфейс для поиска твитов по тексту:
- модуль `elastic-query-web-client` - блокирующий, Spring MVC.  
Базовый URL: `http://localhost:8184/elastic-query-web-client`.
- модуль `elastic-query-web-client-reactive` - реактивный, Spring WebFlux.  
Базовый URL: `http://localhost:8184/elastic-query-web-client-reactive`.

| Страница             | Описание                                   |               Блокирующая версия               |   Реактивная версия   |
|:---------------------|:-------------------------------------------|:----------------------------------------------:|:---------------------:|
| `/home`              | Авторизация и/или вход на главную страницу | :key:&nbsp;`test` <br/> :lock:&nbsp;`test1234` | :unlock: Не требуется |
| `/documents/by‑text` | Поиск твитов по тексту                     |               :white_check_mark:               |  :white_check_mark:   |
| `/error`             | Обработка ошибок                           |               :white_check_mark:               |  :white_check_mark:   |                                                                              

Видео (.gif) работы UI выложу позже, когда проект будет закончен.

## config-server

Микросервис является сервером общих конфигураций для остальных микросервисов. Традиционно при работе со
`spring-cloud-config-server` используется удалённый приватный git-репозиторий на GitHub/GitLab/BitBucket,
в котором хранятся сами файлы с общими настройками для микросервисов проекта.

Но на моём бесплатном аккаунте на GitHub единственный доступный приватный репозиторий уже занят -
как раз-таки репозиторием с конфигурациями `spring-cloud-config-server` для другого проекта на`Spring Cloud`
:slightly_smiling_face:	Поэтому в качестве репозитория я использую директорию (не модуль!)
`config-server-repository`.

Чтобы сделать из директории git-репозиторий, нужно перейти внутрь этой директории в терминале и выполнить
следующие команды:
```bash
git init
git status
git commit -am "Initial commit"
git branch -M main
git update-ref refs/remotes/origin/main refs/heads/main
```
Если впоследствии вносятся какие-либо изменения внутри git-репозитория, то для их применения нужно выполнить
в терминале команды:
```bash
git add .
git commit -m "Add new configs"
```
