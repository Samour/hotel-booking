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

-- Room tables
create table hotel
(
    hotel_id  varchar not null primary key,
    time_zone varchar not null
);

create table room_type
(
    room_type_id varchar not null primary key,
    hotel_id     varchar not null,
    stock_level  int     not null,

    constraint fk__room_type__hotel_id__hotel
        foreign key (hotel_id) references hotel (hotel_id)
);

create table room_type_description
(
    room_type_description_id varchar not null primary key,
    room_type_id             varchar not null,
    title                    varchar not null,
    description              varchar not null,
    image_urls               varchar[] not null,

    constraint fk__room_type_description__room_type_id__room_type
        foreign key (room_type_id) references room_type
);

create table room_stock
(
    room_stock_id varchar not null primary key,
    room_type_id  varchar not null,
    date          varchar not null,
    stock_level   int     not null,

    constraint fk__room_stock__room_type_id__room_type
        foreign key (room_type_id) references room_type (room_type_id)
);

create unique index idx__room_stock__room_type_id__date
    on room_stock (room_type_id, date);

create index idx__room_stock__date
    on room_stock (date);
