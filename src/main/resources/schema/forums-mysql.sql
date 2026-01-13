create table if not exists tb_forums (
    id bigint auto_increment primary key,
    slug varchar(120) not null unique,
    name varchar(200) not null,
    description text,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at datetime(6) not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at datetime(6) not null,
    version bigint not null default 0
) engine=InnoDB;

create table if not exists tb_categories (
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
    constraint fk_categories_forum foreign key (forum_id) references tb_forums(id) on delete cascade
) engine=InnoDB;

create table if not exists tb_topics (
    id bigint auto_increment primary key,
    forum_id bigint not null,
    category_id bigint not null,
    title varchar(400) not null,
    tags varchar(2000),
    status varchar(30) not null,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at datetime(6) not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at datetime(6) not null,
    version bigint not null default 0,
    constraint fk_topics_forum foreign key (forum_id) references tb_forums(id) on delete cascade,
    constraint fk_topics_category foreign key (category_id) references tb_categories(id) on delete cascade
) engine=InnoDB;

create table if not exists tb_posts (
    id bigint auto_increment primary key,
    topic_id bigint not null,
    content text not null,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at datetime(6) not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at datetime(6) not null,
    version bigint not null default 0,
    constraint fk_posts_topic foreign key (topic_id) references tb_topics(id) on delete cascade
) engine=InnoDB;

create index idx_topics_forum_updated on tb_topics (forum_id, updated_at desc);
create index idx_posts_topic_created on tb_posts (topic_id, created_at asc);
