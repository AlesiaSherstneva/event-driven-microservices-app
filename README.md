# event-driven-microservices-app

Я прохожу курс
[Event-Driven Microservices: Spring Boot, Kafka and Elastic](https://www.udemy.com/course/event-driven-microservices-spring-boot-kafka-and-elasticsearch/)
на платформе Udemy.com.

Если верить описанию, курс посвящён созданию приложения, в котором будут взаимодействовать между собой около десяти разнообразных
микросервисов согласно паттернам Event Sourcing и Event Driven. 

С большинством технологий, которые будут применяться в проекте, я так или иначе знакома. Новыми для меня станут 
Keycloak, Kafka Streams, ELK-stack.

Судя по версиям Java 11 и Spring Boot 2.3.4, курс не актуален. На момент моего прохождения актуальны Java 24 (но писать 
буду на Java 17) и Spring Boot 3.5.0. Поэтому готовлюсь много гуглить, особенно по работе со Spring Cloud (там чуть ли
не все компоненты кардинально поменялись) :smile:.

---
Пора начинать вести таблицу, какие более новые технологии я использовала.

| Технология в курсе | Технология, которую я использовала |
|:-:|:-:|
| Java 11 | Java 17 |
| Spring Boot 2.3.4 | Spring Boot 3.5.0 |
| Logback | Log4g2 |

---
В папку resources сервиса `twitter-to-kafka-service` следует поместить конфигурационный файл twitter4j.properties.
Пример файла:

```properties
debug=true
oauth.consumerKey=*********************
oauth.consumerSecret=******************************************
oauth.accessToken=**************************************************
oauth.accessTokenSecret=******************************************
```

Ключи и токены можно получить в аккаунте разработчика социальной сети X (ex-Twitter).