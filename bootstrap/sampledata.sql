create table persistent_logins (
	series varchar(64) primary key,
	username varchar(255) not null REFERENCES t_user(username) ON DELETE CASCADE,
	token varchar(64) not null,
	last_used timestamp not null
);

CREATE INDEX persistent_logins_username_idx ON persistent_logins(username);

-- Creates the admin-user with user-name='admin' and password='Fish123'
insert into t_user(id, version, created, created_by, username, password, first_name, last_name)
	values(nextval('user_id_seq'), 0, CURRENT_TIMESTAMP, currval('user_id_seq'), 'admin',
		'17bsmbONjlAZcMzvS6yaK0U6azs+EgrDKv1crZkE5G1dZJUARcsWGdBAKXMrxd8i', 'Jesus', 'Cristus');

/*
insert into t_project(id, version, created, created_by, name)
	values(nextval('project_id_seq'), 0, CURRENT_TIMESTAMP, (SELECT id FROM t_user WHERE username = 'admin'), 'Project ONE');
*/

