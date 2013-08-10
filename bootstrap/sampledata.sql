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

-- Create a "main"-blog for user admin
insert into blog(id, version, created, created_by, key) values(nextval('blog_id_seq'), 0, current_timestamp, (select id from rpm_user where username = 'admin'), 'main');
-- Create a "main"-blog for user liftco
insert into blog(id, version, created, created_by, key) values(nextval('blog_id_seq'), 0, current_timestamp, (select id from rpm_user where username = 'liftco'), 'main');


-- insert into activity (id, activity_type, created, project_id, name) values(nextval('activity_id_seq'), 'feature', current_timestamp, 1, 'Activity 3');
