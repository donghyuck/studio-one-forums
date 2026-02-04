alter table tb_application_forum_acl_rule
    add column if not exists subject_type varchar(20) not null default 'ROLE',
    add column if not exists identifier_type varchar(20) not null default 'NAME',
    add column if not exists subject_id bigint,
    add column if not exists subject_name varchar(200);

alter table tb_application_forum_acl_rule
    add constraint ck_forum_acl_rule_subject_type
        check (subject_type in ('ROLE', 'USER')),
    add constraint ck_forum_acl_rule_identifier_type
        check (identifier_type in ('ID', 'NAME')),
    add constraint ck_forum_acl_rule_subject_identifier
        check (
            (identifier_type = 'ID' and subject_id is not null and subject_name is null)
            or (identifier_type = 'NAME' and subject_name is not null and subject_id is null)
        );

create index if not exists idx_forum_acl_rule_board_category_subject_action_enabled
    on tb_application_forum_acl_rule (board_id, category_id, action, subject_type, identifier_type, enabled);

create index if not exists idx_forum_acl_rule_board_category_subject_id_action_enabled
    on tb_application_forum_acl_rule (board_id, category_id, subject_type, identifier_type, subject_id, action, enabled)
    where identifier_type = 'ID';

create index if not exists idx_forum_acl_rule_board_category_subject_name_action_enabled
    on tb_application_forum_acl_rule (board_id, category_id, subject_type, identifier_type, subject_name, action, enabled)
    where identifier_type = 'NAME';
