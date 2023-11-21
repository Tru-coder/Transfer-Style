# Transfer Style Work
Gatys work: [https://arxiv.org/abs/1508.06576](https://arxiv.org/pdf/1508.06576.pdf)

Johnson https://arxiv.org/pdf/1603.08155.pdf

# Документация проекта
Swagger Doc http://localhost:8080/swagger-ui/index.html#/

# О проекте
Проект представляет backend приложение и мою интепретацию работы Леона Гатиса. Пользователь с помощью web-api может перенести стиль одного изображения на другое и получить результат на почту или, скачав напрямую.

## Backend 
Написан на языке Java с использованием фреймворка Spring.

База данных: PostgreSQL
  Хранит информацию о пользователях, их JWT-токены и результат работы алгоритма Гатиса(изображение сохраняются в БД как массив байтов).
  
ORM: Hibernate

Безопасность: Spring Security (JWT-tokens)

Почта: Spring mail (email рассылка через мою личную почту, в .yml неактуальный пароль)

Интерфейс взаимодействия между алгоритмом Гатиса, написанном на языке Python, и backend приложением возможен благодаря общему дисковому пространству.
Передача параметров в Python-скрипт осуществляется посредством CLI.

## Алгоритм переноса стиля
Написан на языке Python с использованием технологий: Сuda, Pytorch, PIL


# Запуск
Необходимо настроить базу данных в соответствии с файлом .yml,поставив свои данные

Запустить метод *main* TransferStyleRebuildMavenApplication

## Python

Для запуска алгоритма переноса стиля(Style Transfer Controller) необходимо зааменить значения из application.yml  

output.image

input.image

python.script.style.transfer.gatys.path

python.script.style.transfer.venv.path

на свои.

Также для Python необходимы следующие пакеты, предварительно убедитесь в их установки:
numpy, matplotlib, PIL, tenserflow, PyTorch

# Просмотр запущенного приложения
Откройте в браузере http://localhost:8080/swagger-ui/index.html#/ , чтобы ознакомиться с возможностями приложения