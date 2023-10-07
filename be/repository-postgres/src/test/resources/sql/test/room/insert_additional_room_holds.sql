insert into room_hold(room_hold_id, user_id, hold_expiry)
values ('test-hold-id-5', 'test-hold-user-1', '2023-08-09T15:03:00Z');

insert into room_stock_hold(room_stock_hold_id, room_hold_id, room_stock_id)
values ('room-stock-hold-14', 'test-hold-id-5', 'room-type-id-5-stock-id-7'),
       ('room-stock-hold-15', 'test-hold-id-5', 'room-type-id-5-stock-id-8'),
       ('room-stock-hold-16', 'test-hold-id-5', 'room-type-id-5-stock-id-9');
