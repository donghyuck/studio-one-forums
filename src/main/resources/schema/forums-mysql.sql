create table if not exists tb_application_forums (
    id bigint auto_increment primary key,
    slug varchar(120) not null unique,
    name varchar(200) not null,
    description text,
    type varchar(30) not null default 'COMMON',
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at datetime(6) not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at datetime(6) not null,
    version bigint not null default 0
) engine=InnoDB;

create table if not exists tb_application_categories (
    id bigint auto_increment primary key,
    forum_id bigint not null,
    name varchar(200) not null,
    description text,
    position int not null,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at datetime(6) not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at datetime(6) not null,
    version bigint not null default 0,
    constraint fk_categories_forum foreign key (forum_id) references tb_application_forums(id) on delete cascade
) engine=InnoDB;

create table if not exists tb_application_topics (
    id bigint auto_increment primary key,
    forum_id bigint not null,
    category_id bigint not null,
    title varchar(400) not null,
    tags varchar(2000),
    status varchar(30) not null,
    pinned boolean not null default false,
    locked boolean not null default false,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at datetime(6) not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at datetime(6) not null,
    deleted_at datetime(6),
    deleted_by_id bigint,
    version bigint not null default 0,
    constraint fk_topics_forum foreign key (forum_id) references tb_application_forums(id) on delete cascade,
    constraint fk_topics_category foreign key (category_id) references tb_application_categories(id) on delete cascade
) engine=InnoDB;

create table if not exists tb_application_forum_member (
    forum_id bigint not null,
    user_id bigint not null,
    role varchar(30) not null,
    created_by_id bigint,
    created_at datetime(6) not null,
    primary key (forum_id, user_id),
    constraint fk_forum_member_forum foreign key (forum_id) references tb_application_forums(id) on delete cascade
) engine=InnoDB;

create index idx_forum_member_user on tb_application_forum_member (user_id);
create index idx_forum_member_forum_role on tb_application_forum_member (forum_id, role);

create table if not exists tb_application_posts (
    id bigint auto_increment primary key,
    topic_id bigint not null,
    content text not null,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at datetime(6) not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at datetime(6) not null,
    deleted_at datetime(6),
    deleted_by_id bigint,
    hidden_at datetime(6),
    hidden_by_id bigint,
    version bigint not null default 0,
    constraint fk_posts_topic foreign key (topic_id) references tb_application_topics(id) on delete cascade
) engine=InnoDB;

create index idx_topics_forum_updated on tb_application_topics (forum_id, updated_at desc);
create index idx_posts_topic_created on tb_application_posts (topic_id, created_at asc);

create table if not exists tb_forum_audit_log (
    audit_id bigint auto_increment primary key,
    board_id bigint,
    entity_type varchar(30),
    entity_id bigint,
    action varchar(50),
    actor_id bigint not null,
    at datetime(6) not null,
    detail json
) engine=InnoDB;

create index idx_forum_audit_board_at on tb_forum_audit_log (board_id, at desc);
create index idx_forum_audit_entity on tb_forum_audit_log (entity_type, entity_id);
create index idx_forum_audit_actor_at on tb_forum_audit_log (actor_id, at desc);
