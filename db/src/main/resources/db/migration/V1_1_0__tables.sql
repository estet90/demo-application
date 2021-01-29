CREATE TABLE todo.users
(
    id   BIGSERIAL,
    name TEXT NOT NULL,

    CONSTRAINT users_pk PRIMARY KEY (id)
);

CREATE TABLE todo.tasks
(
    id        BIGSERIAL,
    name      TEXT      NOT NULL,
    date_time TIMESTAMP NOT NULL,
    user_id   BIGINT    NOT NULL,

    CONSTRAINT tasks_pk PRIMARY KEY (id),
    CONSTRAINT tasks_user_id_fk FOREIGN KEY (user_id) REFERENCES todo.users (id) ON DELETE CASCADE
);
