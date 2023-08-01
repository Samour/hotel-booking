-- User tables
create table app_user
(
    user_id    varchar not null primary key,
    user_roles varchar[] not null,
    name       varchar not null
);

create table user_credential
(
    user_id       varchar not null primary key,
    login_id      varchar not null,
    password_hash varchar not null,

    constraint fk__user_credential__user_id__user
        foreign key (user_id) references app_user (user_id)
);

create unique index idx__user_credential__login_id
    on user_credential (login_id);
