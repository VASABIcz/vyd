create type friend_request_state as enum ('accepted', 'declined', 'pending');

alter type friend_request_state owner to vasabi;

create type channel_type as enum ('friends', 'voice', 'text', 'group', 'category');

alter type channel_type owner to vasabi;

create table if not exists channels
(
    id        bigint unsigned auto_increment
        primary key,
    type      text                                  not null,
    timestamp timestamp default current_timestamp() not null,
    constraint id
        unique (id)
);

create table if not exists default_avatars
(
    id     bigint unsigned auto_increment
        primary key,
    avatar blob not null,
    constraint id
        unique (id)
);

create table if not exists join_test
(
    channel int not null,
    user    int not null,
    primary key (channel, user)
);

create table if not exists text_channel_permissions
(
    id                   bigint unsigned auto_increment
        primary key,
    send_messages        tinyint(1) null,
    send_embeds          tinyint(1) null,
    send_attachments     tinyint(1) null,
    add_reactions        tinyint(1) null,
    send_external_emojis tinyint(1) null,
    mention_everyone     tinyint(1) null,
    manage_messages      tinyint(1) null,
    view_history         tinyint(1) null,
    view_channel         tinyint(1) null,
    manage_channel       tinyint(1) null,
    manage_permissions   tinyint(1) null
);

create table if not exists usernames
(
    username      varchar(255) not null
        primary key,
    discriminator varchar(5)   not null
);

create table if not exists users
(
    id            bigint unsigned auto_increment
        primary key,
    name          varchar(255)                          not null,
    discriminator varchar(5)                            not null,
    register_date timestamp default current_timestamp() not null,
    hash          blob                                  not null,
    salt          blob                                  not null,
    constraint id
        unique (id)
);

create table if not exists dm_channels
(
    channel_id bigint unsigned                       not null
        primary key,
    creator    bigint unsigned                       null,
    name       varchar(20)                           null,
    timestamp  timestamp default current_timestamp() not null,
    type       text                                  not null,
    constraint dm_channels_channels_id_fk
        foreign key (channel_id) references channels (id)
            on update cascade on delete cascade,
    constraint dm_channels_users_id_fk
        foreign key (creator) references users (id)
            on update cascade on delete cascade
);

create table if not exists dm_avatars
(
    dm_id  bigint unsigned not null
        primary key,
    avatar blob            null,
    constraint dm_avatars_dm_channels_channel_id_fk
        foreign key (dm_id) references dm_channels (channel_id)
            on update cascade on delete cascade
);

create table if not exists dm_members
(
    user_id    bigint unsigned                       not null,
    channel_id bigint unsigned                       not null,
    inviter    bigint unsigned                       null,
    timestamp  timestamp default current_timestamp() not null,
    primary key (user_id, channel_id),
    constraint dm_members_users_id_fk
        foreign key (user_id) references users (id)
            on update cascade on delete cascade,
    constraint table_name_channels_id_fk
        foreign key (channel_id) references channels (id)
            on update cascade on delete cascade,
    constraint table_name_users_id_fk_2
        foreign key (inviter) references users (id)
            on update cascade
);

create table if not exists dm_invites
(
    channel_id       bigint unsigned                         not null,
    url              varchar(10)                             not null
        primary key,
    timestamp        timestamp default current_timestamp()   not null,
    expire_timestamp timestamp default '0000-00-00 00:00:00' not null,
    author           bigint unsigned                         not null,
    constraint dm_invites_dm_channels_channel_id_fk
        foreign key (channel_id) references dm_channels (channel_id)
            on update cascade on delete cascade,
    constraint dm_invites_dm_members_user_id_fk
        foreign key (author) references dm_members (user_id)
            on update cascade on delete cascade
);

create table if not exists friend_requests
(
    id        bigint unsigned auto_increment
        primary key,
    requester bigint unsigned                       not null,
    receiver  bigint unsigned                       not null,
    state     text      default 'pending'           not null,
    timestamp timestamp default current_timestamp() not null,
    constraint frined_requests_users_id_fk
        foreign key (requester) references users (id)
            on update cascade on delete cascade,
    constraint frined_requests_users_id_fk_2
        foreign key (receiver) references users (id)
            on update cascade on delete cascade
);

create table if not exists friends
(
    user1      bigint unsigned      not null,
    user2      bigint unsigned      not null,
    channel_id bigint unsigned      not null,
    friends    tinyint(1) default 1 not null,
    primary key (user1, user2),
    constraint friend2_fk
        foreign key (user2) references users (id)
            on update cascade on delete cascade,
    constraint friends_channels_id_fk
        foreign key (channel_id) references channels (id)
            on update cascade on delete cascade,
    constraint friends_users_id_fk
        foreign key (user1) references users (id)
            on update cascade on delete cascade
);

create index if not exists friends_channel_id_uindex
    on friends (channel_id);

create table if not exists guilds
(
    id        bigint unsigned auto_increment
        primary key,
    owner_id  bigint unsigned                       not null,
    timestamp timestamp default current_timestamp() not null,
    name      varchar(255)                          not null,
    constraint id
        unique (id),
    constraint guilds_users_id_fk
        foreign key (owner_id) references users (id)
            on update cascade on delete cascade
);

create table if not exists guild_avatars
(
    guild_id  bigint unsigned                       not null
        primary key,
    avatar    blob                                  not null,
    timestamp timestamp default current_timestamp() not null,
    constraint guild_avatars_guilds_id_fk
        foreign key (guild_id) references guilds (id)
            on update cascade on delete cascade
);

create table if not exists guild_bans
(
    guild_id  bigint unsigned                       not null,
    reason    varchar(255)                          not null,
    author    bigint unsigned                       not null,
    user      bigint unsigned                       not null,
    timestamp timestamp default current_timestamp() not null,
    primary key (guild_id, user),
    constraint guild_bans_guilds_id_fk
        foreign key (guild_id) references guilds (id)
            on update cascade on delete cascade,
    constraint guild_bans_users_id_fk
        foreign key (user) references users (id)
            on update cascade on delete cascade,
    constraint guild_bans_users_id_fk_2
        foreign key (author) references users (id)
            on update cascade on delete cascade
);

create table if not exists guild_channel_ordering
(
    guild_id bigint unsigned not null
        primary key,
    channels text            not null,
    constraint guild_channel_ordering_guilds_id_fk
        foreign key (guild_id) references guilds (id)
);

create table if not exists guild_invites
(
    guild_id         bigint unsigned                               not null,
    author           bigint unsigned                               not null,
    url              varchar(10)                                   not null
        primary key,
    uses             bigint unsigned default 0                     not null,
    use_limit        bigint unsigned                               null,
    timestamp        timestamp       default current_timestamp()   not null,
    expire_timestamp timestamp       default '0000-00-00 00:00:00' not null,
    constraint guild_invites_guilds_id_fk
        foreign key (guild_id) references guilds (id)
            on update cascade on delete cascade,
    constraint guild_invites_users_id_fk
        foreign key (author) references users (id)
            on update cascade on delete cascade
);

create table if not exists guild_members
(
    id        bigint unsigned auto_increment
        primary key,
    user_id   bigint unsigned                       not null,
    guild_id  bigint unsigned                       not null,
    nick      varchar(255)                          null,
    timestamp timestamp default current_timestamp() not null,
    constraint guild_members_guilds_id_fk
        foreign key (guild_id) references guilds (id)
            on update cascade on delete cascade,
    constraint guild_members_users_id_fk
        foreign key (user_id) references users (id)
            on update cascade on delete cascade
);

create table if not exists guild_permissions_ordering
(
    guild_id    bigint unsigned not null,
    permissions text            not null,
    constraint guild_permissionsl_ordering_guilds_id_fk
        foreign key (guild_id) references guilds (id)
            on update cascade on delete cascade
);

create table if not exists guild_roles
(
    id                   bigint unsigned auto_increment
        primary key,
    name                 varchar(20)                           not null,
    guild_id             bigint unsigned                       not null,
    author               bigint unsigned                       null,
    timestamp            timestamp default current_timestamp() not null,
    admin                tinyint(1)                            not null,
    view_channels        tinyint(1)                            not null,
    manage_channels      tinyint(1)                            not null,
    manage_roles         tinyint(1)                            not null,
    manage_emojis        tinyint(1)                            not null,
    view_logs            tinyint(1)                            not null,
    manage_webhooks      tinyint(1)                            not null,
    manage_guild         tinyint(1)                            not null,
    connect              tinyint(1)                            not null,
    speak                tinyint(1)                            not null,
    stream_video         tinyint(1)                            not null,
    priority_speaker     tinyint(1)                            not null,
    deafen               tinyint(1)                            not null,
    move                 tinyint(1)                            not null,
    create_invites       tinyint(1)                            not null,
    change_nickname      tinyint(1)                            not null,
    manage_nicknames     tinyint(1)                            not null,
    kick_members         tinyint(1)                            not null,
    ban_members          tinyint(1)                            not null,
    moderate             tinyint(1)                            not null,
    send_messages        tinyint(1)                            not null,
    send_embeds          tinyint(1)                            not null,
    send_attachments     tinyint(1)                            not null,
    add_reactions        tinyint(1)                            not null,
    send_external_emojis tinyint(1)                            not null,
    mention_everyone     tinyint(1)                            not null,
    manage_messages      tinyint(1)                            not null,
    view_history         tinyint(1)                            not null,
    constraint guild_roles_pk_2
        unique (name, guild_id),
    constraint guild_roles_guilds_id_fk
        foreign key (guild_id) references guilds (id)
            on update cascade on delete cascade,
    constraint guild_roles_users_id_fk
        foreign key (author) references users (id)
            on update cascade
);

create table if not exists guild_member_roles
(
    member    bigint unsigned                       not null,
    role      bigint unsigned                       not null,
    timestamp timestamp default current_timestamp() not null,
    assigner  bigint unsigned                       null,
    primary key (member, role),
    constraint guild_member_roles_guild_members_id_fk
        foreign key (member) references guild_members (id)
            on update cascade on delete cascade,
    constraint guild_member_roles_guild_roles_id_fk
        foreign key (role) references guild_roles (id)
            on update cascade on delete cascade,
    constraint guild_member_roles_users_id_fk
        foreign key (assigner) references users (id)
            on update cascade
);

create table if not exists guilds_channels
(
    channel_id bigint unsigned not null
        primary key,
    guild_id   bigint unsigned not null,
    name       varchar(255)    not null,
    constraint guilds_channels_pk
        unique (guild_id, channel_id),
    constraint guilds_channels_channels_id_fk
        foreign key (channel_id) references channels (id)
            on update cascade on delete cascade,
    constraint guilds_channels_guilds_id_fk
        foreign key (guild_id) references guilds (id)
            on update cascade on delete cascade
);

create table if not exists messages
(
    id         bigint unsigned auto_increment
        primary key,
    user_id    bigint unsigned                       not null,
    channel_id bigint unsigned                       not null,
    content    text                                  not null,
    timestamp  timestamp default current_timestamp() not null,
    constraint id
        unique (id),
    constraint messages_channels_id_fk
        foreign key (channel_id) references channels (id)
            on update cascade on delete cascade,
    constraint messages_users_id_fk
        foreign key (user_id) references users (id)
);

create index if not exists messages_timestamp_index
    on messages (timestamp);

create table if not exists test_role_ordering
(
    position bigint unsigned not null,
    role     bigint unsigned not null,
    guild    bigint unsigned not null,
    constraint test_role_ordering_pk
        unique (position, role, guild),
    constraint test_role_ordering_pk_2
        unique (role),
    constraint test_role_ordering_guild_roles_id_fk
        foreign key (role) references guild_roles (id)
            on update cascade,
    constraint test_role_ordering_guilds_id_fk
        foreign key (guild) references guilds (id)
            on update cascade
);

create table if not exists text_permissions_members
(
    channel        bigint unsigned                       not null,
    member         bigint unsigned                       not null,
    timestamp      timestamp default current_timestamp() not null,
    author         bigint unsigned                       null,
    permissions_id bigint unsigned                       not null,
    primary key (member, channel),
    constraint guild_channel_permission_override_members_users_id_fk
        foreign key (author) references users (id)
            on update cascade on delete cascade,
    constraint i_rly_dont_care_rn
        foreign key (member) references guild_members (id)
            on update cascade on delete cascade,
    constraint special_long_fk
        foreign key (channel) references guilds_channels (channel_id)
            on update cascade on delete cascade,
    constraint text_permissions_member_fk
        foreign key (permissions_id) references text_channel_permissions (id)
            on update cascade on delete cascade
);

create table if not exists text_permissions_roles
(
    role        bigint unsigned                       not null,
    author      bigint unsigned                       null,
    channel     bigint unsigned                       not null,
    permissions bigint unsigned                       null,
    timestamp   timestamp default current_timestamp() not null,
    primary key (role, channel),
    constraint text_permissions_role_pk_2
        unique (permissions),
    constraint text_permissions_role_guild_roles_id_fk
        foreign key (role) references guild_roles (id)
            on update cascade on delete cascade,
    constraint text_permissions_role_guilds_channels_channel_id_fk
        foreign key (channel) references guilds_channels (channel_id)
            on update cascade on delete cascade,
    constraint text_permissions_role_text_channel_permissions_id_fk
        foreign key (permissions) references text_channel_permissions (id)
            on update cascade on delete cascade,
    constraint text_permissions_role_users_id_fk
        foreign key (author) references users (id)
            on update cascade
);

create table if not exists user_avatars
(
    user_id   bigint unsigned                       not null
        primary key,
    avatar    blob                                  not null,
    timestamp timestamp default current_timestamp() not null,
    constraint table_name_users_id_fk
        foreign key (user_id) references users (id)
            on update cascade on delete cascade
);

create table if not exists user_settings
(
    user_id bigint unsigned not null,
    `key`   varchar(255)    null,
    value   text            null,
    constraint user_settings_pk
        unique (user_id, `key`),
    constraint user_settings_users_id_fk
        foreign key (user_id) references users (id)
            on update cascade on delete cascade
);

create index if not exists userus
    on users (name, discriminator);

create table if not exists voice_channel_permissions
(
    id                 bigint unsigned auto_increment
        primary key,
    connect            tinyint(1) null,
    speak              tinyint(1) null,
    stream_video       tinyint(1) null,
    priority_speaker   tinyint(1) null,
    move               tinyint(1) null,
    deafen             tinyint(1) null,
    view_channel       tinyint(1) null,
    manage_channel     tinyint(1) null,
    manage_permissions tinyint(1) null
);

create table if not exists voice_permissions_member
(
    member      bigint unsigned                       not null,
    channel     bigint unsigned                       not null,
    permissions bigint unsigned                       not null,
    timestamp   timestamp default current_timestamp() not null,
    author      bigint unsigned                       null,
    primary key (member, channel),
    constraint voice_permissions_member_guild_members_id_fk
        foreign key (member) references guild_members (id)
            on update cascade on delete cascade,
    constraint voice_permissions_member_guilds_channels_channel_id_fk
        foreign key (channel) references guilds_channels (channel_id)
            on update cascade on delete cascade,
    constraint voice_permissions_member_voice_channel_permissions_id_fk
        foreign key (permissions) references voice_channel_permissions (id)
            on update cascade on delete cascade
);

create table if not exists voice_permissions_role
(
    role        bigint unsigned                       not null,
    channel     bigint unsigned                       not null,
    author      bigint unsigned                       null,
    timestamp   timestamp default current_timestamp() not null,
    permissions bigint unsigned                       not null,
    primary key (role, channel),
    constraint voice_permissions_role_guilds_channels_channel_id_fk
        foreign key (channel) references guilds_channels (channel_id)
            on update cascade on delete cascade,
    constraint voice_permissions_role_users_id_fk
        foreign key (author) references users (id)
            on update cascade,
    constraint voice_permissions_role_voice_channel_permissions_id_fk
        foreign key (permissions) references voice_channel_permissions (id)
            on update cascade on delete cascade
);

