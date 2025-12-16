create table users
(
    id            bigserial primary key,
    phone         varchar(20)  not null unique,
    login         varchar(64)  not null unique,
    password_hash varchar(255) not null,
    role          varchar(16)  not null,
    info          varchar(255),
    created_at    timestamp    not null,
    updated_at    timestamp    not null
);

create table groups
(
    id          bigserial primary key,
    name        varchar(128) not null,
    description varchar(512),
    invite_code varchar(16)  not null unique,
    coach_id    bigint       not null references users (id),
    created_at  timestamp    not null,
    updated_at  timestamp    not null
);

create table group_members
(
    group_id  bigint    not null references groups (id),
    user_id   bigint    not null references users (id),
    joined_at timestamp not null,
    primary key (group_id, user_id)
);

create table disciplines
(
    id   serial primary key,
    code varchar(32) not null,
    name varchar(64) not null
);

create table results
(
    id               bigserial primary key,
    athlete_id       bigint       not null references users (id),
    discipline_id    int          not null references disciplines (id),
    result_value     varchar(32)  not null,
    result_numeric   double precision,
    competition_name varchar(128) not null,
    place            int,
    date             date         not null,
    info             varchar(512),
    created_at       timestamp    not null
);

create table sms_codes
(
    id         bigserial primary key,
    phone      varchar(20) not null,
    code       varchar(8)  not null,
    expires_at timestamp   not null,
    used       boolean     not null
);
