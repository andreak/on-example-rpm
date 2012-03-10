create table persistent_logins (
	series varchar(64) primary key,
	username varchar(255) not null REFERENCES rpm_user(username) ON DELETE CASCADE,
	token varchar(64) not null,
	last_used timestamp not null
);

CREATE INDEX persistent_logins_username_idx ON persistent_logins(username);

-- Creates the admin user with user-name='admin' and password='Fish123'
insert into rpm_user(id, version, created, created_by, username, password, first_name, last_name, image_icon_path)
	values(nextval('rpm_user_id_seq'), 0, CURRENT_TIMESTAMP, currval('rpm_user_id_seq'), 'admin',
		'17bsmbONjlAZcMzvS6yaK0U6azs+EgrDKv1crZkE5G1dZJUARcsWGdBAKXMrxd8i', 'Admin', 'Allmighty',
		'https://si0.twimg.com/profile_images/1668274537/andreas-krogh_normal.jpg');

-- Creates the liftco user with user-name='liftco' and password='Fish123'
insert into rpm_user(id, version, created, created_by, username, password, first_name, last_name, image_icon_path)
        values(nextval('rpm_user_id_seq'), 0, CURRENT_TIMESTAMP, currval('rpm_user_id_seq'), 'liftco',
                '17bsmbONjlAZcMzvS6yaK0U6azs+EgrDKv1crZkE5G1dZJUARcsWGdBAKXMrxd8i', 'Lift Co.', 'The official Lift-support company',
                'https://si0.twimg.com/profile_images/1851378741/lift_co_logo_normal.png');

insert into blog(id, version, created, created_by, key) values(nextval('blog_id_seq'), 0, current_timestamp, (select id from rpm_user where username = 'admin'), 'main');
insert into blog(id, version, created, created_by, key) values(nextval('blog_id_seq'), 0, current_timestamp, (select id from rpm_user where username = 'liftco'), 'main');
