update tb_forum_acl_rule
   set subject_type = 'ROLE',
       identifier_type = 'NAME',
       subject_name = role
 where subject_name is null
   and role is not null;
