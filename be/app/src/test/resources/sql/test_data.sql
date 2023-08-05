insert into app_user(user_id, user_roles, name)
values ('test-admin-id', '{"MANAGE_USERS"}', 'Test Admin');

insert into user_credential(user_id, login_id, password_hash)
values ('test-admin-id', 'test-admin', '$2a$06$p11nWTlSKYvrIyH3dPdLTudBS5wTW55H0orSVGrmFB41OmPfFq8fO');
