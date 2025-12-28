CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO public.users (id, username, firstname, lastname)
VALUES ('7557947d-bfdf-482b-bc2b-58f8ca5ee30c', 'app_user', 'App', 'User'),
       ('6d62151f-0c88-4db7-b971-920eafa7662f', 'app_admin', 'App', 'Admin'),
       ('bc367281-4c61-4ce5-88fb-a192a3f943c0', 'app_super_user', 'Super', 'User');

INSERT INTO public.documents (id, document_id)
VALUES ('ef6b952f-daae-41c5-9db5-8422d3a6f7dd', 1),
       ('198337c7-1db6-4827-b450-e625ed1dcac9', 2),
       ('5e7224f4-17c1-435f-af79-b88e9d75e348', 3);

INSERT INTO public.user_permissions (user_permission_id, user_id, document_id, permission_type)
VALUES (uuid_generate_v4(), '7557947d-bfdf-482b-bc2b-58f8ca5ee30c', 'ef6b952f-daae-41c5-9db5-8422d3a6f7dd', 'READ');

INSERT INTO public.user_permissions (user_permission_id, user_id, document_id, permission_type)
VALUES (uuid_generate_v4(), '6d62151f-0c88-4db7-b971-920eafa7662f', 'ef6b952f-daae-41c5-9db5-8422d3a6f7dd', 'READ');

INSERT INTO public.user_permissions (user_permission_id, user_id, document_id, permission_type)
VALUES (uuid_generate_v4(), '6d62151f-0c88-4db7-b971-920eafa7662f', '198337c7-1db6-4827-b450-e625ed1dcac9', 'READ');

INSERT INTO public.user_permissions (user_permission_id, user_id, document_id, permission_type)
VALUES (uuid_generate_v4(), '6d62151f-0c88-4db7-b971-920eafa7662f', '5e7224f4-17c1-435f-af79-b88e9d75e348', 'READ');

INSERT INTO public.user_permissions (user_permission_id, user_id, document_id, permission_type)
VALUES (uuid_generate_v4(), 'bc367281-4c61-4ce5-88fb-a192a3f943c0', 'ef6b952f-daae-41c5-9db5-8422d3a6f7dd', 'READ');