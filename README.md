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
| POST&nbsp;`documents/by‑text`           | Поиск твитов по тексту. <br/> Пример запроса JSON: <br/> `{ "text": "java" }` |               :white_check_mark:               |       :white_check_mark:        |
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
