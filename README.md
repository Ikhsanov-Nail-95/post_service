## Service Template

Стандартный шаблон проекта на SpringBoot

## Использованные технологии

* [Spring Boot](https://spring.io/projects/spring-boot) – как основной фрэймворк
* [PostgreSQL](https://www.postgresql.org/) – как основная реляционная база данных
* [Redis](https://redis.io/) – как кэш и очередь сообщений через pub/sub
* [testcontainers](https://testcontainers.com/) – для изолированного тестирования с базой данных
* [Liquibase](https://www.liquibase.org/) – для ведения миграций схемы БД
* [Gradle](https://gradle.org/) – как система сборки приложения
* [Lombok](https://projectlombok.org/) – для удобной работы с POJO классами
* [MapStruct](https://mapstruct.org/) – для удобного маппинга между POJO классами

## База данных

* База поднимается в отдельном сервисе [infra](../infra)
* Redis поднимается в единственном инстансе тоже в [infra](../infra)
* Liquibase сам накатывает нужные миграции на голый PostgreSql при старте приложения
* В тестах используется [testcontainers](https://testcontainers.com/), в котором тоже запускается отдельный инстанс
  postgres
* В коде продемонстрирована работа как с JPA (Hibernate)

## Post Service - публикация постов с возможностью писать под ними комментарии и ставить лайки
[Контроллер](https://github.com/Ikhsanov-Nail-95/post_service/blob/main/src/main/java/faang/school/postservice/controller/PostController.java) - документировал API с помощью библиотеки **Swagger/OpenAPI**. Каждая операция (создание поста, публикация поста и обновление поста) документирована с указанием её назначения, возможных ответов и валидации параметров. 

[Сервис](https://github.com/Ikhsanov-Nail-95/post_service/blob/main/src/main/java/faang/school/postservice/service/PostService.java) - выполняет все действия описанные в контроллере. **Чистый и понятный код** с соблюдением принципов **SOLID** и других передовых практик.
