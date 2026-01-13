create table if not exists tb_forums (
    id bigserial primary key,
    slug varchar(120) not null unique,
    name varchar(200) not null,
    description text,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at timestamptz not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table if not exists tb_categories (
    id bigserial primary key,
    forum_id bigint not null references tb_forums(id) on delete cascade,
    name varchar(200) not null,
    description text,
    position int not null,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at timestamptz not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table if not exists tb_topics (
    id bigserial primary key,
    forum_id bigint not null references tb_forums(id) on delete cascade,
    category_id bigint not null references tb_categories(id) on delete cascade,
    title varchar(400) not null,
    tags varchar(2000),
    status varchar(30) not null,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at timestamptz not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table if not exists tb_posts (
    id bigserial primary key,
    topic_id bigint not null references tb_topics(id) on delete cascade,
    content text not null,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at timestamptz not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index if not exists idx_topics_forum_updated on tb_topics (forum_id, updated_at desc);
create index if not exists idx_posts_topic_created on tb_posts (topic_id, created_at asc);
