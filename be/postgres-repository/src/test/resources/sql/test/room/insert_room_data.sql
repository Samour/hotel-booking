insert into room_type(room_type_id, hotel_id, stock_level)
values ('room-type-id-1', 'test-hotel', 5),
       ('room-type-id-2', 'test-hotel', 5),
       ('room-type-id-3', 'test-hotel', 5),
       ('room-type-id-4', 'test-hotel', 5),
       ('room-type-id-5', 'test-hotel', 5),
       ('room-type-id-6', 'test-hotel', 5),
       ('room-type-id-7', 'test-hotel', 5),
       ('room-type-id-8', 'test-hotel', 5),
       ('room-type-id-9', 'test-hotel', 5),
       ('room-type-id-10', 'test-hotel', 5),
       ('room-type-id-11', 'test-hotel', 5),
       ('room-type-id-12', 'test-hotel', 5),
       ('room-type-id-13', 'test-hotel', 5),
       ('room-type-id-14', 'test-hotel', 5),
       ('room-type-id-15', 'test-hotel', 5);

insert into room_type_description(room_type_description_id, room_type_id, title, description, image_urls)
values ('room-type-description-id-1', 'room-type-id-1', 'room-title-1', 'room-description-1',
        '{"room-type-1-image-1","room-type-1-image-2","room-type-1-image-3"}'),
       ('room-type-description-id-2', 'room-type-id-2', 'room-title-2', 'room-description-2',
        '{"room-type-2-image-1","room-type-2-image-2","room-type-2-image-3"}'),
       ('room-type-description-id-3', 'room-type-id-3', 'room-title-3', 'room-description-3',
        '{"room-type-3-image-1","room-type-3-image-2","room-type-3-image-3"}'),
       ('room-type-description-id-4', 'room-type-id-4', 'room-title-4', 'room-description-4',
        '{"room-type-4-image-1","room-type-4-image-2","room-type-4-image-3"}'),
       ('room-type-description-id-5', 'room-type-id-5', 'room-title-5', 'room-description-5',
        '{"room-type-5-image-1","room-type-5-image-2","room-type-5-image-3"}'),
       ('room-type-description-id-6', 'room-type-id-6', 'room-title-6', 'room-description-6',
        '{"room-type-6-image-1","room-type-6-image-2","room-type-6-image-3"}'),
       ('room-type-description-id-7', 'room-type-id-7', 'room-title-7', 'room-description-7',
        '{"room-type-7-image-1","room-type-7-image-2","room-type-7-image-3"}'),
       ('room-type-description-id-8', 'room-type-id-8', 'room-title-8', 'room-description-8',
        '{"room-type-8-image-1","room-type-8-image-2","room-type-8-image-3"}'),
       ('room-type-description-id-9', 'room-type-id-9', 'room-title-9', 'room-description-9',
        '{"room-type-9-image-1","room-type-9-image-2","room-type-9-image-3"}'),
       ('room-type-description-id-10', 'room-type-id-10', 'room-title-10', 'room-description-10',
        '{"room-type-10-image-1","room-type-10-image-2","room-type-10-image-3"}'),
       ('room-type-description-id-11', 'room-type-id-11', 'room-title-11', 'room-description-11',
        '{"room-type-11-image-1","room-type-11-image-2","room-type-11-image-3"}'),
       ('room-type-description-id-12', 'room-type-id-12', 'room-title-12', 'room-description-12',
        '{"room-type-12-image-1","room-type-12-image-2","room-type-12-image-3"}'),
       ('room-type-description-id-13', 'room-type-id-13', 'room-title-13', 'room-description-13',
        '{"room-type-13-image-1","room-type-13-image-2","room-type-13-image-3"}'),
       ('room-type-description-id-14', 'room-type-id-14', 'room-title-14', 'room-description-14',
        '{"room-type-14-image-1","room-type-14-image-2","room-type-14-image-3"}'),
       ('room-type-description-id-15', 'room-type-id-15', 'room-title-15', 'room-description-15',
        '{"room-type-15-image-1","room-type-15-image-2","room-type-15-image-3"}');

insert into room_stock(room_stock_id, room_type_id, date, stock_level)
values ('room-type-1-stock-id-1', 'room-type-id-1', '2023-08-09', 1),
       ('room-type-1-stock-id-2', 'room-type-id-1', '2023-08-10', 2),
       ('room-type-1-stock-id-3', 'room-type-id-1', '2023-08-11', 3),
       ('room-type-1-stock-id-4', 'room-type-id-1', '2023-08-12', 4),
       ('room-type-1-stock-id-5', 'room-type-id-1', '2023-08-13', 0),
       ('room-type-1-stock-id-6', 'room-type-id-1', '2023-08-14', 1),
       ('room-type-1-stock-id-7', 'room-type-id-1', '2023-08-15', 2),
       ('room-type-1-stock-id-8', 'room-type-id-1', '2023-08-16', 3),
       ('room-type-1-stock-id-9', 'room-type-id-1', '2023-08-17', 4),
       ('room-type-1-stock-id-10', 'room-type-id-1', '2023-08-18', 0),
       ('room-type-1-stock-id-11', 'room-type-id-1', '2023-08-19', 1),
       ('room-type-1-stock-id-12', 'room-type-id-1', '2023-08-20', 2),
       ('room-type-1-stock-id-13', 'room-type-id-1', '2023-08-21', 3),
       ('room-type-1-stock-id-14', 'room-type-id-1', '2023-08-22', 4),
       ('room-type-1-stock-id-15', 'room-type-id-1', '2023-08-23', 0),
       ('room-type-1-stock-id-16', 'room-type-id-1', '2023-08-24', 1),
       ('room-type-1-stock-id-17', 'room-type-id-1', '2023-08-25', 2),
       ('room-type-1-stock-id-18', 'room-type-id-1', '2023-08-26', 3),
       ('room-type-1-stock-id-19', 'room-type-id-1', '2023-08-27', 4),
       ('room-type-1-stock-id-20', 'room-type-id-1', '2023-08-28', 0),
       ('room-type-1-stock-id-21', 'room-type-id-1', '2023-08-29', 1),
       ('room-type-1-stock-id-22', 'room-type-id-1', '2023-08-30', 2),
       ('room-type-1-stock-id-23', 'room-type-id-1', '2023-08-31', 3),
       ('room-type-1-stock-id-24', 'room-type-id-1', '2023-09-01', 4),
       ('room-type-1-stock-id-25', 'room-type-id-1', '2023-09-02', 0),
       ('room-type-1-stock-id-26', 'room-type-id-1', '2023-09-03', 1),
       ('room-type-1-stock-id-27', 'room-type-id-1', '2023-09-04', 2),
       ('room-type-1-stock-id-28', 'room-type-id-1', '2023-09-05', 3),
       ('room-type-1-stock-id-29', 'room-type-id-1', '2023-09-06', 4),
       ('room-type-1-stock-id-30', 'room-type-id-1', '2023-09-07', 0),
       ('room-type-2-stock-id-1', 'room-type-id-2', '2023-08-09', 1),
       ('room-type-2-stock-id-2', 'room-type-id-2', '2023-08-10', 2),
       ('room-type-2-stock-id-3', 'room-type-id-2', '2023-08-11', 3),
       ('room-type-2-stock-id-4', 'room-type-id-2', '2023-08-12', 4),
       ('room-type-2-stock-id-5', 'room-type-id-2', '2023-08-13', 0),
       ('room-type-2-stock-id-6', 'room-type-id-2', '2023-08-14', 1),
       ('room-type-2-stock-id-7', 'room-type-id-2', '2023-08-15', 2),
       ('room-type-2-stock-id-8', 'room-type-id-2', '2023-08-16', 3),
       ('room-type-2-stock-id-9', 'room-type-id-2', '2023-08-17', 4),
       ('room-type-2-stock-id-10', 'room-type-id-2', '2023-08-18', 0),
       ('room-type-2-stock-id-11', 'room-type-id-2', '2023-08-19', 1),
       ('room-type-2-stock-id-12', 'room-type-id-2', '2023-08-20', 2),
       ('room-type-2-stock-id-13', 'room-type-id-2', '2023-08-21', 3),
       ('room-type-2-stock-id-14', 'room-type-id-2', '2023-08-22', 4),
       ('room-type-2-stock-id-15', 'room-type-id-2', '2023-08-23', 0),
       ('room-type-2-stock-id-16', 'room-type-id-2', '2023-08-24', 1),
       ('room-type-2-stock-id-17', 'room-type-id-2', '2023-08-25', 2),
       ('room-type-2-stock-id-18', 'room-type-id-2', '2023-08-26', 3),
       ('room-type-2-stock-id-19', 'room-type-id-2', '2023-08-27', 4),
       ('room-type-2-stock-id-20', 'room-type-id-2', '2023-08-28', 0),
       ('room-type-2-stock-id-21', 'room-type-id-2', '2023-08-29', 1),
       ('room-type-2-stock-id-22', 'room-type-id-2', '2023-08-30', 2),
       ('room-type-2-stock-id-23', 'room-type-id-2', '2023-08-31', 3),
       ('room-type-2-stock-id-24', 'room-type-id-2', '2023-09-01', 4),
       ('room-type-2-stock-id-25', 'room-type-id-2', '2023-09-02', 0),
       ('room-type-2-stock-id-26', 'room-type-id-2', '2023-09-03', 1),
       ('room-type-2-stock-id-27', 'room-type-id-2', '2023-09-04', 2),
       ('room-type-2-stock-id-28', 'room-type-id-2', '2023-09-05', 3),
       ('room-type-2-stock-id-29', 'room-type-id-2', '2023-09-06', 4),
       ('room-type-2-stock-id-30', 'room-type-id-2', '2023-09-07', 0),
       ('room-type-3-stock-id-1', 'room-type-id-3', '2023-08-09', 1),
       ('room-type-3-stock-id-2', 'room-type-id-3', '2023-08-10', 2),
       ('room-type-3-stock-id-3', 'room-type-id-3', '2023-08-11', 3),
       ('room-type-3-stock-id-4', 'room-type-id-3', '2023-08-12', 4),
       ('room-type-3-stock-id-5', 'room-type-id-3', '2023-08-13', 0),
       ('room-type-3-stock-id-6', 'room-type-id-3', '2023-08-14', 1),
       ('room-type-3-stock-id-7', 'room-type-id-3', '2023-08-15', 2),
       ('room-type-3-stock-id-8', 'room-type-id-3', '2023-08-16', 3),
       ('room-type-3-stock-id-9', 'room-type-id-3', '2023-08-17', 4),
       ('room-type-3-stock-id-10', 'room-type-id-3', '2023-08-18', 0),
       ('room-type-3-stock-id-11', 'room-type-id-3', '2023-08-19', 1),
       ('room-type-3-stock-id-12', 'room-type-id-3', '2023-08-20', 2),
       ('room-type-3-stock-id-13', 'room-type-id-3', '2023-08-21', 3),
       ('room-type-3-stock-id-14', 'room-type-id-3', '2023-08-22', 4),
       ('room-type-3-stock-id-15', 'room-type-id-3', '2023-08-23', 0),
       ('room-type-3-stock-id-16', 'room-type-id-3', '2023-08-24', 1),
       ('room-type-3-stock-id-17', 'room-type-id-3', '2023-08-25', 2),
       ('room-type-3-stock-id-18', 'room-type-id-3', '2023-08-26', 3),
       ('room-type-3-stock-id-19', 'room-type-id-3', '2023-08-27', 4),
       ('room-type-3-stock-id-20', 'room-type-id-3', '2023-08-28', 0),
       ('room-type-3-stock-id-21', 'room-type-id-3', '2023-08-29', 1),
       ('room-type-3-stock-id-22', 'room-type-id-3', '2023-08-30', 2),
       ('room-type-3-stock-id-23', 'room-type-id-3', '2023-08-31', 3),
       ('room-type-3-stock-id-24', 'room-type-id-3', '2023-09-01', 4),
       ('room-type-3-stock-id-25', 'room-type-id-3', '2023-09-02', 0),
       ('room-type-3-stock-id-26', 'room-type-id-3', '2023-09-03', 1),
       ('room-type-3-stock-id-27', 'room-type-id-3', '2023-09-04', 2),
       ('room-type-3-stock-id-28', 'room-type-id-3', '2023-09-05', 3),
       ('room-type-3-stock-id-29', 'room-type-id-3', '2023-09-06', 4),
       ('room-type-3-stock-id-30', 'room-type-id-3', '2023-09-07', 0),
       ('room-type-4-stock-id-1', 'room-type-id-4', '2023-08-09', 1),
       ('room-type-4-stock-id-2', 'room-type-id-4', '2023-08-10', 2),
       ('room-type-4-stock-id-3', 'room-type-id-4', '2023-08-11', 3),
       ('room-type-4-stock-id-4', 'room-type-id-4', '2023-08-12', 4),
       ('room-type-4-stock-id-5', 'room-type-id-4', '2023-08-13', 0),
       ('room-type-4-stock-id-6', 'room-type-id-4', '2023-08-14', 1),
       ('room-type-4-stock-id-7', 'room-type-id-4', '2023-08-15', 2),
       ('room-type-4-stock-id-8', 'room-type-id-4', '2023-08-16', 3),
       ('room-type-4-stock-id-9', 'room-type-id-4', '2023-08-17', 4),
       ('room-type-4-stock-id-10', 'room-type-id-4', '2023-08-18', 0),
       ('room-type-4-stock-id-11', 'room-type-id-4', '2023-08-19', 1),
       ('room-type-4-stock-id-12', 'room-type-id-4', '2023-08-20', 2),
       ('room-type-4-stock-id-13', 'room-type-id-4', '2023-08-21', 3),
       ('room-type-4-stock-id-14', 'room-type-id-4', '2023-08-22', 4),
       ('room-type-4-stock-id-15', 'room-type-id-4', '2023-08-23', 0),
       ('room-type-4-stock-id-16', 'room-type-id-4', '2023-08-24', 1),
       ('room-type-4-stock-id-17', 'room-type-id-4', '2023-08-25', 2),
       ('room-type-4-stock-id-18', 'room-type-id-4', '2023-08-26', 3),
       ('room-type-4-stock-id-19', 'room-type-id-4', '2023-08-27', 4),
       ('room-type-4-stock-id-20', 'room-type-id-4', '2023-08-28', 0),
       ('room-type-4-stock-id-21', 'room-type-id-4', '2023-08-29', 1),
       ('room-type-4-stock-id-22', 'room-type-id-4', '2023-08-30', 2),
       ('room-type-4-stock-id-23', 'room-type-id-4', '2023-08-31', 3),
       ('room-type-4-stock-id-24', 'room-type-id-4', '2023-09-01', 4),
       ('room-type-4-stock-id-25', 'room-type-id-4', '2023-09-02', 0),
       ('room-type-4-stock-id-26', 'room-type-id-4', '2023-09-03', 1),
       ('room-type-4-stock-id-27', 'room-type-id-4', '2023-09-04', 2),
       ('room-type-4-stock-id-28', 'room-type-id-4', '2023-09-05', 3),
       ('room-type-4-stock-id-29', 'room-type-id-4', '2023-09-06', 4),
       ('room-type-4-stock-id-30', 'room-type-id-4', '2023-09-07', 0),
       ('room-type-5-stock-id-1', 'room-type-id-5', '2023-08-09', 1),
       ('room-type-5-stock-id-2', 'room-type-id-5', '2023-08-10', 2),
       ('room-type-5-stock-id-3', 'room-type-id-5', '2023-08-11', 3),
       ('room-type-5-stock-id-4', 'room-type-id-5', '2023-08-12', 4),
       ('room-type-5-stock-id-5', 'room-type-id-5', '2023-08-13', 0),
       ('room-type-5-stock-id-6', 'room-type-id-5', '2023-08-14', 1),
       ('room-type-5-stock-id-7', 'room-type-id-5', '2023-08-15', 2),
       ('room-type-5-stock-id-8', 'room-type-id-5', '2023-08-16', 3),
       ('room-type-5-stock-id-9', 'room-type-id-5', '2023-08-17', 4),
       ('room-type-5-stock-id-10', 'room-type-id-5', '2023-08-18', 0),
       ('room-type-5-stock-id-11', 'room-type-id-5', '2023-08-19', 1),
       ('room-type-5-stock-id-12', 'room-type-id-5', '2023-08-20', 2),
       ('room-type-5-stock-id-13', 'room-type-id-5', '2023-08-21', 3),
       ('room-type-5-stock-id-14', 'room-type-id-5', '2023-08-22', 4),
       ('room-type-5-stock-id-15', 'room-type-id-5', '2023-08-23', 0),
       ('room-type-5-stock-id-16', 'room-type-id-5', '2023-08-24', 1),
       ('room-type-5-stock-id-17', 'room-type-id-5', '2023-08-25', 2),
       ('room-type-5-stock-id-18', 'room-type-id-5', '2023-08-26', 3),
       ('room-type-5-stock-id-19', 'room-type-id-5', '2023-08-27', 4),
       ('room-type-5-stock-id-20', 'room-type-id-5', '2023-08-28', 0),
       ('room-type-5-stock-id-21', 'room-type-id-5', '2023-08-29', 1),
       ('room-type-5-stock-id-22', 'room-type-id-5', '2023-08-30', 2),
       ('room-type-5-stock-id-23', 'room-type-id-5', '2023-08-31', 3),
       ('room-type-5-stock-id-24', 'room-type-id-5', '2023-09-01', 4),
       ('room-type-5-stock-id-25', 'room-type-id-5', '2023-09-02', 0),
       ('room-type-5-stock-id-26', 'room-type-id-5', '2023-09-03', 1),
       ('room-type-5-stock-id-27', 'room-type-id-5', '2023-09-04', 2),
       ('room-type-5-stock-id-28', 'room-type-id-5', '2023-09-05', 3),
       ('room-type-5-stock-id-29', 'room-type-id-5', '2023-09-06', 4),
       ('room-type-5-stock-id-30', 'room-type-id-5', '2023-09-07', 0),
       ('room-type-6-stock-id-1', 'room-type-id-6', '2023-08-09', 1),
       ('room-type-6-stock-id-2', 'room-type-id-6', '2023-08-10', 2),
       ('room-type-6-stock-id-3', 'room-type-id-6', '2023-08-11', 3),
       ('room-type-6-stock-id-4', 'room-type-id-6', '2023-08-12', 4),
       ('room-type-6-stock-id-5', 'room-type-id-6', '2023-08-13', 0),
       ('room-type-6-stock-id-6', 'room-type-id-6', '2023-08-14', 1),
       ('room-type-6-stock-id-7', 'room-type-id-6', '2023-08-15', 2),
       ('room-type-6-stock-id-8', 'room-type-id-6', '2023-08-16', 3),
       ('room-type-6-stock-id-9', 'room-type-id-6', '2023-08-17', 4),
       ('room-type-6-stock-id-10', 'room-type-id-6', '2023-08-18', 0),
       ('room-type-6-stock-id-11', 'room-type-id-6', '2023-08-19', 1),
       ('room-type-6-stock-id-12', 'room-type-id-6', '2023-08-20', 2),
       ('room-type-6-stock-id-13', 'room-type-id-6', '2023-08-21', 3),
       ('room-type-6-stock-id-14', 'room-type-id-6', '2023-08-22', 4),
       ('room-type-6-stock-id-15', 'room-type-id-6', '2023-08-23', 0),
       ('room-type-6-stock-id-16', 'room-type-id-6', '2023-08-24', 1),
       ('room-type-6-stock-id-17', 'room-type-id-6', '2023-08-25', 2),
       ('room-type-6-stock-id-18', 'room-type-id-6', '2023-08-26', 3),
       ('room-type-6-stock-id-19', 'room-type-id-6', '2023-08-27', 4),
       ('room-type-6-stock-id-20', 'room-type-id-6', '2023-08-28', 0),
       ('room-type-6-stock-id-21', 'room-type-id-6', '2023-08-29', 1),
       ('room-type-6-stock-id-22', 'room-type-id-6', '2023-08-30', 2),
       ('room-type-6-stock-id-23', 'room-type-id-6', '2023-08-31', 3),
       ('room-type-6-stock-id-24', 'room-type-id-6', '2023-09-01', 4),
       ('room-type-6-stock-id-25', 'room-type-id-6', '2023-09-02', 0),
       ('room-type-6-stock-id-26', 'room-type-id-6', '2023-09-03', 1),
       ('room-type-6-stock-id-27', 'room-type-id-6', '2023-09-04', 2),
       ('room-type-6-stock-id-28', 'room-type-id-6', '2023-09-05', 3),
       ('room-type-6-stock-id-29', 'room-type-id-6', '2023-09-06', 4),
       ('room-type-6-stock-id-30', 'room-type-id-6', '2023-09-07', 0),
       ('room-type-7-stock-id-1', 'room-type-id-7', '2023-08-09', 1),
       ('room-type-7-stock-id-2', 'room-type-id-7', '2023-08-10', 2),
       ('room-type-7-stock-id-3', 'room-type-id-7', '2023-08-11', 3),
       ('room-type-7-stock-id-4', 'room-type-id-7', '2023-08-12', 4),
       ('room-type-7-stock-id-5', 'room-type-id-7', '2023-08-13', 0),
       ('room-type-7-stock-id-6', 'room-type-id-7', '2023-08-14', 1),
       ('room-type-7-stock-id-7', 'room-type-id-7', '2023-08-15', 2),
       ('room-type-7-stock-id-8', 'room-type-id-7', '2023-08-16', 3),
       ('room-type-7-stock-id-9', 'room-type-id-7', '2023-08-17', 4),
       ('room-type-7-stock-id-10', 'room-type-id-7', '2023-08-18', 0),
       ('room-type-7-stock-id-11', 'room-type-id-7', '2023-08-19', 1),
       ('room-type-7-stock-id-12', 'room-type-id-7', '2023-08-20', 2),
       ('room-type-7-stock-id-13', 'room-type-id-7', '2023-08-21', 3),
       ('room-type-7-stock-id-14', 'room-type-id-7', '2023-08-22', 4),
       ('room-type-7-stock-id-15', 'room-type-id-7', '2023-08-23', 0),
       ('room-type-7-stock-id-16', 'room-type-id-7', '2023-08-24', 1),
       ('room-type-7-stock-id-17', 'room-type-id-7', '2023-08-25', 2),
       ('room-type-7-stock-id-18', 'room-type-id-7', '2023-08-26', 3),
       ('room-type-7-stock-id-19', 'room-type-id-7', '2023-08-27', 4),
       ('room-type-7-stock-id-20', 'room-type-id-7', '2023-08-28', 0),
       ('room-type-7-stock-id-21', 'room-type-id-7', '2023-08-29', 1),
       ('room-type-7-stock-id-22', 'room-type-id-7', '2023-08-30', 2),
       ('room-type-7-stock-id-23', 'room-type-id-7', '2023-08-31', 3),
       ('room-type-7-stock-id-24', 'room-type-id-7', '2023-09-01', 4),
       ('room-type-7-stock-id-25', 'room-type-id-7', '2023-09-02', 0),
       ('room-type-7-stock-id-26', 'room-type-id-7', '2023-09-03', 1),
       ('room-type-7-stock-id-27', 'room-type-id-7', '2023-09-04', 2),
       ('room-type-7-stock-id-28', 'room-type-id-7', '2023-09-05', 3),
       ('room-type-7-stock-id-29', 'room-type-id-7', '2023-09-06', 4),
       ('room-type-7-stock-id-30', 'room-type-id-7', '2023-09-07', 0),
       ('room-type-8-stock-id-1', 'room-type-id-8', '2023-08-09', 1),
       ('room-type-8-stock-id-2', 'room-type-id-8', '2023-08-10', 2),
       ('room-type-8-stock-id-3', 'room-type-id-8', '2023-08-11', 3),
       ('room-type-8-stock-id-4', 'room-type-id-8', '2023-08-12', 4),
       ('room-type-8-stock-id-5', 'room-type-id-8', '2023-08-13', 0),
       ('room-type-8-stock-id-6', 'room-type-id-8', '2023-08-14', 1),
       ('room-type-8-stock-id-7', 'room-type-id-8', '2023-08-15', 2),
       ('room-type-8-stock-id-8', 'room-type-id-8', '2023-08-16', 3),
       ('room-type-8-stock-id-9', 'room-type-id-8', '2023-08-17', 4),
       ('room-type-8-stock-id-10', 'room-type-id-8', '2023-08-18', 0),
       ('room-type-8-stock-id-11', 'room-type-id-8', '2023-08-19', 1),
       ('room-type-8-stock-id-12', 'room-type-id-8', '2023-08-20', 2),
       ('room-type-8-stock-id-13', 'room-type-id-8', '2023-08-21', 3),
       ('room-type-8-stock-id-14', 'room-type-id-8', '2023-08-22', 4),
       ('room-type-8-stock-id-15', 'room-type-id-8', '2023-08-23', 0),
       ('room-type-8-stock-id-16', 'room-type-id-8', '2023-08-24', 1),
       ('room-type-8-stock-id-17', 'room-type-id-8', '2023-08-25', 2),
       ('room-type-8-stock-id-18', 'room-type-id-8', '2023-08-26', 3),
       ('room-type-8-stock-id-19', 'room-type-id-8', '2023-08-27', 4),
       ('room-type-8-stock-id-20', 'room-type-id-8', '2023-08-28', 0),
       ('room-type-8-stock-id-21', 'room-type-id-8', '2023-08-29', 1),
       ('room-type-8-stock-id-22', 'room-type-id-8', '2023-08-30', 2),
       ('room-type-8-stock-id-23', 'room-type-id-8', '2023-08-31', 3),
       ('room-type-8-stock-id-24', 'room-type-id-8', '2023-09-01', 4),
       ('room-type-8-stock-id-25', 'room-type-id-8', '2023-09-02', 0),
       ('room-type-8-stock-id-26', 'room-type-id-8', '2023-09-03', 1),
       ('room-type-8-stock-id-27', 'room-type-id-8', '2023-09-04', 2),
       ('room-type-8-stock-id-28', 'room-type-id-8', '2023-09-05', 3),
       ('room-type-8-stock-id-29', 'room-type-id-8', '2023-09-06', 4),
       ('room-type-8-stock-id-30', 'room-type-id-8', '2023-09-07', 0),
       ('room-type-9-stock-id-1', 'room-type-id-9', '2023-08-09', 1),
       ('room-type-9-stock-id-2', 'room-type-id-9', '2023-08-10', 2),
       ('room-type-9-stock-id-3', 'room-type-id-9', '2023-08-11', 3),
       ('room-type-9-stock-id-4', 'room-type-id-9', '2023-08-12', 4),
       ('room-type-9-stock-id-5', 'room-type-id-9', '2023-08-13', 0),
       ('room-type-9-stock-id-6', 'room-type-id-9', '2023-08-14', 1),
       ('room-type-9-stock-id-7', 'room-type-id-9', '2023-08-15', 2),
       ('room-type-9-stock-id-8', 'room-type-id-9', '2023-08-16', 3),
       ('room-type-9-stock-id-9', 'room-type-id-9', '2023-08-17', 4),
       ('room-type-9-stock-id-10', 'room-type-id-9', '2023-08-18', 0),
       ('room-type-9-stock-id-11', 'room-type-id-9', '2023-08-19', 1),
       ('room-type-9-stock-id-12', 'room-type-id-9', '2023-08-20', 2),
       ('room-type-9-stock-id-13', 'room-type-id-9', '2023-08-21', 3),
       ('room-type-9-stock-id-14', 'room-type-id-9', '2023-08-22', 4),
       ('room-type-9-stock-id-15', 'room-type-id-9', '2023-08-23', 0),
       ('room-type-9-stock-id-16', 'room-type-id-9', '2023-08-24', 1),
       ('room-type-9-stock-id-17', 'room-type-id-9', '2023-08-25', 2),
       ('room-type-9-stock-id-18', 'room-type-id-9', '2023-08-26', 3),
       ('room-type-9-stock-id-19', 'room-type-id-9', '2023-08-27', 4),
       ('room-type-9-stock-id-20', 'room-type-id-9', '2023-08-28', 0),
       ('room-type-9-stock-id-21', 'room-type-id-9', '2023-08-29', 1),
       ('room-type-9-stock-id-22', 'room-type-id-9', '2023-08-30', 2),
       ('room-type-9-stock-id-23', 'room-type-id-9', '2023-08-31', 3),
       ('room-type-9-stock-id-24', 'room-type-id-9', '2023-09-01', 4),
       ('room-type-9-stock-id-25', 'room-type-id-9', '2023-09-02', 0),
       ('room-type-9-stock-id-26', 'room-type-id-9', '2023-09-03', 1),
       ('room-type-9-stock-id-27', 'room-type-id-9', '2023-09-04', 2),
       ('room-type-9-stock-id-28', 'room-type-id-9', '2023-09-05', 3),
       ('room-type-9-stock-id-29', 'room-type-id-9', '2023-09-06', 4),
       ('room-type-9-stock-id-30', 'room-type-id-9', '2023-09-07', 0),
       ('room-type-10-stock-id-1', 'room-type-id-10', '2023-08-09', 1),
       ('room-type-10-stock-id-2', 'room-type-id-10', '2023-08-10', 2),
       ('room-type-10-stock-id-3', 'room-type-id-10', '2023-08-11', 3),
       ('room-type-10-stock-id-4', 'room-type-id-10', '2023-08-12', 4),
       ('room-type-10-stock-id-5', 'room-type-id-10', '2023-08-13', 0),
       ('room-type-10-stock-id-6', 'room-type-id-10', '2023-08-14', 1),
       ('room-type-10-stock-id-7', 'room-type-id-10', '2023-08-15', 2),
       ('room-type-10-stock-id-8', 'room-type-id-10', '2023-08-16', 3),
       ('room-type-10-stock-id-9', 'room-type-id-10', '2023-08-17', 4),
       ('room-type-10-stock-id-10', 'room-type-id-10', '2023-08-18', 0),
       ('room-type-10-stock-id-11', 'room-type-id-10', '2023-08-19', 1),
       ('room-type-10-stock-id-12', 'room-type-id-10', '2023-08-20', 2),
       ('room-type-10-stock-id-13', 'room-type-id-10', '2023-08-21', 3),
       ('room-type-10-stock-id-14', 'room-type-id-10', '2023-08-22', 4),
       ('room-type-10-stock-id-15', 'room-type-id-10', '2023-08-23', 0),
       ('room-type-10-stock-id-16', 'room-type-id-10', '2023-08-24', 1),
       ('room-type-10-stock-id-17', 'room-type-id-10', '2023-08-25', 2),
       ('room-type-10-stock-id-18', 'room-type-id-10', '2023-08-26', 3),
       ('room-type-10-stock-id-19', 'room-type-id-10', '2023-08-27', 4),
       ('room-type-10-stock-id-20', 'room-type-id-10', '2023-08-28', 0),
       ('room-type-10-stock-id-21', 'room-type-id-10', '2023-08-29', 1),
       ('room-type-10-stock-id-22', 'room-type-id-10', '2023-08-30', 2),
       ('room-type-10-stock-id-23', 'room-type-id-10', '2023-08-31', 3),
       ('room-type-10-stock-id-24', 'room-type-id-10', '2023-09-01', 4),
       ('room-type-10-stock-id-25', 'room-type-id-10', '2023-09-02', 0),
       ('room-type-10-stock-id-26', 'room-type-id-10', '2023-09-03', 1),
       ('room-type-10-stock-id-27', 'room-type-id-10', '2023-09-04', 2),
       ('room-type-10-stock-id-28', 'room-type-id-10', '2023-09-05', 3),
       ('room-type-10-stock-id-29', 'room-type-id-10', '2023-09-06', 4),
       ('room-type-10-stock-id-30', 'room-type-id-10', '2023-09-07', 0),
       ('room-type-11-stock-id-1', 'room-type-id-11', '2023-08-09', 1),
       ('room-type-11-stock-id-2', 'room-type-id-11', '2023-08-10', 2),
       ('room-type-11-stock-id-3', 'room-type-id-11', '2023-08-11', 3),
       ('room-type-11-stock-id-4', 'room-type-id-11', '2023-08-12', 4),
       ('room-type-11-stock-id-5', 'room-type-id-11', '2023-08-13', 0),
       ('room-type-11-stock-id-6', 'room-type-id-11', '2023-08-14', 1),
       ('room-type-11-stock-id-7', 'room-type-id-11', '2023-08-15', 2),
       ('room-type-11-stock-id-8', 'room-type-id-11', '2023-08-16', 3),
       ('room-type-11-stock-id-9', 'room-type-id-11', '2023-08-17', 4),
       ('room-type-11-stock-id-10', 'room-type-id-11', '2023-08-18', 0),
       ('room-type-11-stock-id-11', 'room-type-id-11', '2023-08-19', 1),
       ('room-type-11-stock-id-12', 'room-type-id-11', '2023-08-20', 2),
       ('room-type-11-stock-id-13', 'room-type-id-11', '2023-08-21', 3),
       ('room-type-11-stock-id-14', 'room-type-id-11', '2023-08-22', 4),
       ('room-type-11-stock-id-15', 'room-type-id-11', '2023-08-23', 0),
       ('room-type-11-stock-id-16', 'room-type-id-11', '2023-08-24', 1),
       ('room-type-11-stock-id-17', 'room-type-id-11', '2023-08-25', 2),
       ('room-type-11-stock-id-18', 'room-type-id-11', '2023-08-26', 3),
       ('room-type-11-stock-id-19', 'room-type-id-11', '2023-08-27', 4),
       ('room-type-11-stock-id-20', 'room-type-id-11', '2023-08-28', 0),
       ('room-type-11-stock-id-21', 'room-type-id-11', '2023-08-29', 1),
       ('room-type-11-stock-id-22', 'room-type-id-11', '2023-08-30', 2),
       ('room-type-11-stock-id-23', 'room-type-id-11', '2023-08-31', 3),
       ('room-type-11-stock-id-24', 'room-type-id-11', '2023-09-01', 4),
       ('room-type-11-stock-id-25', 'room-type-id-11', '2023-09-02', 0),
       ('room-type-11-stock-id-26', 'room-type-id-11', '2023-09-03', 1),
       ('room-type-11-stock-id-27', 'room-type-id-11', '2023-09-04', 2),
       ('room-type-11-stock-id-28', 'room-type-id-11', '2023-09-05', 3),
       ('room-type-11-stock-id-29', 'room-type-id-11', '2023-09-06', 4),
       ('room-type-11-stock-id-30', 'room-type-id-11', '2023-09-07', 0),
       ('room-type-12-stock-id-1', 'room-type-id-12', '2023-08-09', 1),
       ('room-type-12-stock-id-2', 'room-type-id-12', '2023-08-10', 2),
       ('room-type-12-stock-id-3', 'room-type-id-12', '2023-08-11', 3),
       ('room-type-12-stock-id-4', 'room-type-id-12', '2023-08-12', 4),
       ('room-type-12-stock-id-5', 'room-type-id-12', '2023-08-13', 0),
       ('room-type-12-stock-id-6', 'room-type-id-12', '2023-08-14', 1),
       ('room-type-12-stock-id-7', 'room-type-id-12', '2023-08-15', 2),
       ('room-type-12-stock-id-8', 'room-type-id-12', '2023-08-16', 3),
       ('room-type-12-stock-id-9', 'room-type-id-12', '2023-08-17', 4),
       ('room-type-12-stock-id-10', 'room-type-id-12', '2023-08-18', 0),
       ('room-type-12-stock-id-11', 'room-type-id-12', '2023-08-19', 1),
       ('room-type-12-stock-id-12', 'room-type-id-12', '2023-08-20', 2),
       ('room-type-12-stock-id-13', 'room-type-id-12', '2023-08-21', 3),
       ('room-type-12-stock-id-14', 'room-type-id-12', '2023-08-22', 4),
       ('room-type-12-stock-id-15', 'room-type-id-12', '2023-08-23', 0),
       ('room-type-12-stock-id-16', 'room-type-id-12', '2023-08-24', 1),
       ('room-type-12-stock-id-17', 'room-type-id-12', '2023-08-25', 2),
       ('room-type-12-stock-id-18', 'room-type-id-12', '2023-08-26', 3),
       ('room-type-12-stock-id-19', 'room-type-id-12', '2023-08-27', 4),
       ('room-type-12-stock-id-20', 'room-type-id-12', '2023-08-28', 0),
       ('room-type-12-stock-id-21', 'room-type-id-12', '2023-08-29', 1),
       ('room-type-12-stock-id-22', 'room-type-id-12', '2023-08-30', 2),
       ('room-type-12-stock-id-23', 'room-type-id-12', '2023-08-31', 3),
       ('room-type-12-stock-id-24', 'room-type-id-12', '2023-09-01', 4),
       ('room-type-12-stock-id-25', 'room-type-id-12', '2023-09-02', 0),
       ('room-type-12-stock-id-26', 'room-type-id-12', '2023-09-03', 1),
       ('room-type-12-stock-id-27', 'room-type-id-12', '2023-09-04', 2),
       ('room-type-12-stock-id-28', 'room-type-id-12', '2023-09-05', 3),
       ('room-type-12-stock-id-29', 'room-type-id-12', '2023-09-06', 4),
       ('room-type-12-stock-id-30', 'room-type-id-12', '2023-09-07', 0),
       ('room-type-13-stock-id-1', 'room-type-id-13', '2023-08-09', 1),
       ('room-type-13-stock-id-2', 'room-type-id-13', '2023-08-10', 2),
       ('room-type-13-stock-id-3', 'room-type-id-13', '2023-08-11', 3),
       ('room-type-13-stock-id-4', 'room-type-id-13', '2023-08-12', 4),
       ('room-type-13-stock-id-5', 'room-type-id-13', '2023-08-13', 0),
       ('room-type-13-stock-id-6', 'room-type-id-13', '2023-08-14', 1),
       ('room-type-13-stock-id-7', 'room-type-id-13', '2023-08-15', 2),
       ('room-type-13-stock-id-8', 'room-type-id-13', '2023-08-16', 3),
       ('room-type-13-stock-id-9', 'room-type-id-13', '2023-08-17', 4),
       ('room-type-13-stock-id-10', 'room-type-id-13', '2023-08-18', 0),
       ('room-type-13-stock-id-11', 'room-type-id-13', '2023-08-19', 1),
       ('room-type-13-stock-id-12', 'room-type-id-13', '2023-08-20', 2),
       ('room-type-13-stock-id-13', 'room-type-id-13', '2023-08-21', 3),
       ('room-type-13-stock-id-14', 'room-type-id-13', '2023-08-22', 4),
       ('room-type-13-stock-id-15', 'room-type-id-13', '2023-08-23', 0),
       ('room-type-13-stock-id-16', 'room-type-id-13', '2023-08-24', 1),
       ('room-type-13-stock-id-17', 'room-type-id-13', '2023-08-25', 2),
       ('room-type-13-stock-id-18', 'room-type-id-13', '2023-08-26', 3),
       ('room-type-13-stock-id-19', 'room-type-id-13', '2023-08-27', 4),
       ('room-type-13-stock-id-20', 'room-type-id-13', '2023-08-28', 0),
       ('room-type-13-stock-id-21', 'room-type-id-13', '2023-08-29', 1),
       ('room-type-13-stock-id-22', 'room-type-id-13', '2023-08-30', 2),
       ('room-type-13-stock-id-23', 'room-type-id-13', '2023-08-31', 3),
       ('room-type-13-stock-id-24', 'room-type-id-13', '2023-09-01', 4),
       ('room-type-13-stock-id-25', 'room-type-id-13', '2023-09-02', 0),
       ('room-type-13-stock-id-26', 'room-type-id-13', '2023-09-03', 1),
       ('room-type-13-stock-id-27', 'room-type-id-13', '2023-09-04', 2),
       ('room-type-13-stock-id-28', 'room-type-id-13', '2023-09-05', 3),
       ('room-type-13-stock-id-29', 'room-type-id-13', '2023-09-06', 4),
       ('room-type-13-stock-id-30', 'room-type-id-13', '2023-09-07', 0),
       ('room-type-14-stock-id-1', 'room-type-id-14', '2023-08-09', 1),
       ('room-type-14-stock-id-2', 'room-type-id-14', '2023-08-10', 2),
       ('room-type-14-stock-id-3', 'room-type-id-14', '2023-08-11', 3),
       ('room-type-14-stock-id-4', 'room-type-id-14', '2023-08-12', 4),
       ('room-type-14-stock-id-5', 'room-type-id-14', '2023-08-13', 0),
       ('room-type-14-stock-id-6', 'room-type-id-14', '2023-08-14', 1),
       ('room-type-14-stock-id-7', 'room-type-id-14', '2023-08-15', 2),
       ('room-type-14-stock-id-8', 'room-type-id-14', '2023-08-16', 3),
       ('room-type-14-stock-id-9', 'room-type-id-14', '2023-08-17', 4),
       ('room-type-14-stock-id-10', 'room-type-id-14', '2023-08-18', 0),
       ('room-type-14-stock-id-11', 'room-type-id-14', '2023-08-19', 1),
       ('room-type-14-stock-id-12', 'room-type-id-14', '2023-08-20', 2),
       ('room-type-14-stock-id-13', 'room-type-id-14', '2023-08-21', 3),
       ('room-type-14-stock-id-14', 'room-type-id-14', '2023-08-22', 4),
       ('room-type-14-stock-id-15', 'room-type-id-14', '2023-08-23', 0),
       ('room-type-14-stock-id-16', 'room-type-id-14', '2023-08-24', 1),
       ('room-type-14-stock-id-17', 'room-type-id-14', '2023-08-25', 2),
       ('room-type-14-stock-id-18', 'room-type-id-14', '2023-08-26', 3),
       ('room-type-14-stock-id-19', 'room-type-id-14', '2023-08-27', 4),
       ('room-type-14-stock-id-20', 'room-type-id-14', '2023-08-28', 0),
       ('room-type-14-stock-id-21', 'room-type-id-14', '2023-08-29', 1),
       ('room-type-14-stock-id-22', 'room-type-id-14', '2023-08-30', 2),
       ('room-type-14-stock-id-23', 'room-type-id-14', '2023-08-31', 3),
       ('room-type-14-stock-id-24', 'room-type-id-14', '2023-09-01', 4),
       ('room-type-14-stock-id-25', 'room-type-id-14', '2023-09-02', 0),
       ('room-type-14-stock-id-26', 'room-type-id-14', '2023-09-03', 1),
       ('room-type-14-stock-id-27', 'room-type-id-14', '2023-09-04', 2),
       ('room-type-14-stock-id-28', 'room-type-id-14', '2023-09-05', 3),
       ('room-type-14-stock-id-29', 'room-type-id-14', '2023-09-06', 4),
       ('room-type-14-stock-id-30', 'room-type-id-14', '2023-09-07', 0),
       ('room-type-15-stock-id-1', 'room-type-id-15', '2023-08-09', 1),
       ('room-type-15-stock-id-2', 'room-type-id-15', '2023-08-10', 2),
       ('room-type-15-stock-id-3', 'room-type-id-15', '2023-08-11', 3),
       ('room-type-15-stock-id-4', 'room-type-id-15', '2023-08-12', 4),
       ('room-type-15-stock-id-5', 'room-type-id-15', '2023-08-13', 0),
       ('room-type-15-stock-id-6', 'room-type-id-15', '2023-08-14', 1),
       ('room-type-15-stock-id-7', 'room-type-id-15', '2023-08-15', 2),
       ('room-type-15-stock-id-8', 'room-type-id-15', '2023-08-16', 3),
       ('room-type-15-stock-id-9', 'room-type-id-15', '2023-08-17', 4),
       ('room-type-15-stock-id-10', 'room-type-id-15', '2023-08-18', 0),
       ('room-type-15-stock-id-11', 'room-type-id-15', '2023-08-19', 1),
       ('room-type-15-stock-id-12', 'room-type-id-15', '2023-08-20', 2),
       ('room-type-15-stock-id-13', 'room-type-id-15', '2023-08-21', 3),
       ('room-type-15-stock-id-14', 'room-type-id-15', '2023-08-22', 4),
       ('room-type-15-stock-id-15', 'room-type-id-15', '2023-08-23', 0),
       ('room-type-15-stock-id-16', 'room-type-id-15', '2023-08-24', 1),
       ('room-type-15-stock-id-17', 'room-type-id-15', '2023-08-25', 2),
       ('room-type-15-stock-id-18', 'room-type-id-15', '2023-08-26', 3),
       ('room-type-15-stock-id-19', 'room-type-id-15', '2023-08-27', 4),
       ('room-type-15-stock-id-20', 'room-type-id-15', '2023-08-28', 0),
       ('room-type-15-stock-id-21', 'room-type-id-15', '2023-08-29', 1),
       ('room-type-15-stock-id-22', 'room-type-id-15', '2023-08-30', 2),
       ('room-type-15-stock-id-23', 'room-type-id-15', '2023-08-31', 3),
       ('room-type-15-stock-id-24', 'room-type-id-15', '2023-09-01', 4),
       ('room-type-15-stock-id-25', 'room-type-id-15', '2023-09-02', 0),
       ('room-type-15-stock-id-26', 'room-type-id-15', '2023-09-03', 1),
       ('room-type-15-stock-id-27', 'room-type-id-15', '2023-09-04', 2),
       ('room-type-15-stock-id-28', 'room-type-id-15', '2023-09-05', 3),
       ('room-type-15-stock-id-29', 'room-type-id-15', '2023-09-06', 4),
       ('room-type-15-stock-id-30', 'room-type-id-15', '2023-09-07', 0);
