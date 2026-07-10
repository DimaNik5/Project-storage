# Проект
Платформа для создания и обмена профессиональными проектами.
# Запуск
## База данных

```
CREATE DATABASE filedb;
CREATE USER psuser WITH PASSWORD 'psuser';
GRANT ALL PRIVILEGES ON DATABASE filedb TO psuser;
GRANT ALL ON SCHEMA public TO psuser;
```
После подключения
```
psql -U postgres -f db/init.sql
```

## Фронтенд

```
npm install
npm start
```

## Бэкед
Запускается java
