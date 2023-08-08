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

-- Room hold

create table room_hold
(
    room_hold_id varchar not null primary key,
    user_id      varchar not null,
    hold_expiry  varchar not null, -- UTC timestamp

    constraint fk__room_hold__user_id__app_user
        foreign key (user_id) references app_user
);

create table room_stock_hold
(
    room_stock_hold_id varchar not null primary key,
    room_hold_id       varchar not null,
    room_stock_id      varchar not null,

    constraint fk__room_stock_hold__room_hold_id__room_hold
        foreign key (room_hold_id) references room_hold,

    constraint fk__room_stock_hold__room_stock_id__room_type
        foreign key (room_stock_id) references room_stock
);

-- Required data
insert into hotel(hotel_id, time_zone)
values ('test-hotel', 'Australia/Sydney');
