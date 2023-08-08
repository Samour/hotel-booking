insert into app_user(user_id, user_roles, name)
values ('test-hold-user-1', '{}', 'Test hold user 1'),
       ('test-hold-user-2', '{}', 'Test hold user 2'),
       ('test-hold-user-3', '{}', 'Test hold user 3');

insert into room_hold(room_hold_id, user_id, hold_expiry)
values ('test-hold-id-1', 'test-hold-user-1', '2023-08-09T15:00:00Z'),
       ('test-hold-id-2', 'test-hold-user-2', '2023-08-09T15:00:00Z'),
       ('test-hold-id-3', 'test-hold-user-3', '2023-08-09T15:00:00Z'),
       -- Expired hold
       ('test-hold-id-4', 'test-hold-user-3', '2023-08-09T03:00:00Z');

insert into room_stock_hold(room_stock_hold_id, room_hold_id, room_stock_id)
values ('room-stock-hold-1', 'test-hold-id-1', 'room-type-1-stock-id-7'),
       ('room-stock-hold-2', 'test-hold-id-1', 'room-type-1-stock-id-8'),
       ('room-stock-hold-3', 'test-hold-id-1', 'room-type-1-stock-id-9'),
       ('room-stock-hold-5', 'test-hold-id-2', 'room-type-1-stock-id-9'),
       ('room-stock-hold-6', 'test-hold-id-2', 'room-type-1-stock-id-11'),
       ('room-stock-hold-7', 'test-hold-id-2', 'room-type-1-stock-id-12'),
       ('room-stock-hold-8', 'test-hold-id-3', 'room-type-2-stock-id-12'),
       ('room-stock-hold-9', 'test-hold-id-3', 'room-type-2-stock-id-13'),
       ('room-stock-hold-10', 'test-hold-id-3', 'room-type-2-stock-id-14'),
       ('room-stock-hold-11', 'test-hold-id-4', 'room-type-2-stock-id-13'),
       ('room-stock-hold-12', 'test-hold-id-4', 'room-type-2-stock-id-14'),
       ('room-stock-hold-13', 'test-hold-id-4', 'room-type-2-stock-id-16');
