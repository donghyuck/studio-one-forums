create table if not exists tb_application_forums (
    id bigserial primary key,
    slug varchar(120) not null unique,
    name varchar(200) not null,
    description text,
    type varchar(30) not null default 'COMMON',
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at timestamptz not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table if not exists tb_application_forum_property (
    forum_id bigint not null references tb_application_forums(id) on delete cascade,
    property_name varchar(200) not null,
    property_value text,
    primary key (forum_id, property_name)
);

create table if not exists tb_application_forum_acl_rule (
    rule_id bigserial primary key,
    board_id bigint not null,
    category_id bigint,
    role varchar(200) not null,
    subject_type varchar(20) not null default 'ROLE',
    identifier_type varchar(20) not null default 'NAME',
    subject_id bigint,
    subject_name varchar(200),
    action varchar(60) not null,
    effect varchar(20) not null,
    ownership varchar(20) not null,
    priority int not null default 0,
    enabled boolean not null default true,
    created_by_id bigint,
    created_at timestamptz,
    updated_by_id bigint,
    updated_at timestamptz,
    constraint ck_forum_acl_rule_subject_type check (subject_type in ('ROLE', 'USER')),
    constraint ck_forum_acl_rule_identifier_type check (identifier_type in ('ID', 'NAME')),
    constraint ck_forum_acl_rule_subject_identifier check (
        (identifier_type = 'ID' and subject_id is not null and subject_name is null)
        or (identifier_type = 'NAME' and subject_name is not null and subject_id is null)
    )
);

create index if not exists idx_forum_acl_rule_board_category_role_action_enabled
    on tb_application_forum_acl_rule (board_id, category_id, role, action, enabled);
create index if not exists idx_forum_acl_rule_board_role_action_enabled
    on tb_application_forum_acl_rule (board_id, role, action, enabled);
create index if not exists idx_forum_acl_rule_board_category_subject_action_enabled
    on tb_application_forum_acl_rule (board_id, category_id, action, subject_type, identifier_type, enabled);
create index if not exists idx_forum_acl_rule_board_category_subject_id_action_enabled
    on tb_application_forum_acl_rule (board_id, category_id, subject_type, identifier_type, subject_id, action, enabled)
    where identifier_type = 'ID';
create index if not exists idx_forum_acl_rule_board_category_subject_name_action_enabled
    on tb_application_forum_acl_rule (board_id, category_id, subject_type, identifier_type, subject_name, action, enabled)
    where identifier_type = 'NAME';

create table if not exists tb_application_forum_member (
    forum_id bigint not null references tb_application_forums(id) on delete cascade,
    user_id bigint not null,
    role varchar(30) not null,
    created_by_id bigint,
    created_at timestamptz not null default now(),
    primary key (forum_id, user_id)
);

create index if not exists idx_forum_member_user on tb_application_forum_member (user_id);
create index if not exists idx_forum_member_forum_role on tb_application_forum_member (forum_id, role);

create table if not exists tb_application_categories (
    id bigserial primary key,
    forum_id bigint not null references tb_application_forums(id) on delete cascade,
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

create table if not exists tb_application_topics (
    id bigserial primary key,
    forum_id bigint not null references tb_application_forums(id) on delete cascade,
    category_id bigint references tb_application_categories(id) on delete cascade,
    title varchar(400) not null,
    tags varchar(2000),
    status varchar(30) not null,
    pinned boolean not null default false,
    locked boolean not null default false,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at timestamptz not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at timestamptz not null,
    deleted_at timestamptz,
    deleted_by_id bigint,
    version bigint not null default 0
);

create table if not exists tb_application_posts (
    id bigserial primary key,
    topic_id bigint not null references tb_application_topics(id) on delete cascade,
    content text not null,
    created_by_id bigint not null,
    created_by varchar(120) not null,
    created_at timestamptz not null,
    updated_by_id bigint not null,
    updated_by varchar(120) not null,
    updated_at timestamptz not null,
    deleted_at timestamptz,
    deleted_by_id bigint,
    hidden_at timestamptz,
    hidden_by_id bigint,
    version bigint not null default 0
);

create index if not exists idx_topics_forum_updated on tb_application_topics (forum_id, updated_at desc);
create index if not exists idx_posts_topic_created on tb_application_posts (topic_id, created_at asc);

create table if not exists tb_application_forum_audit_log (
    audit_id bigserial primary key,
    board_id bigint,
    entity_type varchar(30),
    entity_id bigint,
    action varchar(50),
    actor_id bigint not null,
    at timestamptz not null,
    detail jsonb
);

create index if not exists idx_forum_audit_board_at on tb_application_forum_audit_log (board_id, at desc);
create index if not exists idx_forum_audit_entity on tb_application_forum_audit_log (entity_type, entity_id);
create index if not exists idx_forum_audit_actor_at on tb_application_forum_audit_log (actor_id, at desc);
