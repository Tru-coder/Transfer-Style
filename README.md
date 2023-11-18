# Transfer Style Work
Gatys work: [https://arxiv.org/abs/1508.06576](https://arxiv.org/pdf/1508.06576.pdf)
Johnson https://arxiv.org/pdf/1603.08155.pdf

# Документация проекта
Swagger Doc http://localhost:8080/swagger-ui/index.html#/

# О проекте
Проект представляет backend приложение и мою интепретацию работы Леона Гатиса. Пользователь с помощью web-api перенести стиль одного изображения на другое и получить результат на почту или, скачав напрямую.

## Backend 
Написан на языке Java с использованием фреймворка Spring.

База данных: PostgreSQL
  Хранит информацию о пользователях, их JWT-токены и результат работы алгоритма Гатиса(изображение сохраняются в БД как массив байтов).
  
ORM: Hibernate

Безопасность: Spring Security (JWT-tokens)

Почта: Spring mail (email рассылка через мою личную почту)

Интерфейс взаимодействия между алгоритмом Гатиса, написанном на языке Python, и backend приложением возможен благодаря общему дисковому пространству.
Передача параметров в Python-скрипт осуществляется посредством CLI.

## Алгоритм переноса стиля
Написан на языке Python с использованием технологий: Сuda, Pytorch, PIL
