create type friend_request_state as enum ('accepted', 'declined', 'pending');

alter type friend_request_state owner to vasabi;

create type channel_type as enum ('friends', 'voice', 'text', 'group', 'category');

alter type channel_type owner to vasabi;


create table users
(
    id            serial
        constraint users_pk
            primary key,
    name          varchar(255)                        not null,
    discriminator varchar(5)                          not null,
    register_date timestamp default CURRENT_TIMESTAMP not null,
    hash          bytea                               not null,
    salt          bytea                               not null
);

comment on table users is 'table for storing users';

alter table users
    owner to vasabi;

create unique index "user"
    on users (name, discriminator);

create table usernames
(
    username      varchar(255) not null
        constraint usernames_pk
            primary key,
    discriminator varchar(5)   not null
);

alter table usernames
    owner to vasabi;

create table channels
(
    id            serial
        constraint channels_pk
            primary key,
    type          channel_type                        not null,
    "[timestamp]" timestamp default CURRENT_TIMESTAMP not null
);

alter table channels
    owner to vasabi;

create table friends
(
    user1      integer              not null
        constraint friends_users_id_fk
            references users
            on update cascade on delete cascade,
    user2      integer              not null
        constraint friend2_fk
            references users
            on update cascade on delete cascade,
    channel_id integer              not null
        constraint friends_channels_id_fk
            references channels
            on update cascade on delete cascade,
    friends    boolean default true not null,
    constraint friends_pk
        primary key (user1, user2)
);

alter table friends
    owner to vasabi;

create unique index friends_channel_id_uindex
    on friends (channel_id);

create table messages
(
    id            serial
        constraint messages_pk
            primary key,
    user_id       integer                             not null
        constraint messages_users_id_fk
            references users,
    channel_id    integer                             not null
        constraint messages_channels_id_fk
            references channels
            on update cascade on delete cascade,
    content       text                                not null,
    "[timestamp]" timestamp default CURRENT_TIMESTAMP not null
);

alter table messages
    owner to vasabi;

create table friend_requests
(
    id            integer              default nextval('frined_requests_id_seq'::regclass) not null
        constraint frined_requests_pk
            primary key,
    requester     integer                                                                  not null
        constraint frined_requests_users_id_fk
            references users
            on update cascade on delete cascade,
    receiver      integer                                                                  not null
        constraint frined_requests_users_id_fk_2
            references users
            on update cascade on delete cascade,
    state         friend_request_state default 'pending'::friend_request_state             not null,
    "[timestamp]" timestamp            default CURRENT_TIMESTAMP                           not null
);

alter table friend_requests
    owner to vasabi;

create table guilds
(
    id            serial
        constraint guilds_pk
            primary key,
    owner_id      integer                             not null
        constraint guilds_users_id_fk
            references users
            on update cascade on delete cascade,
    "[timestamp]" timestamp default CURRENT_TIMESTAMP not null
);

alter table guilds
    owner to vasabi;

create table guilds_channels
(
    channel_id    integer                             not null
        constraint guilds_channels_channels_id_fk
            references channels
            on update cascade on delete cascade,
    guild_id      integer                             not null
        constraint guilds_channels_guilds_id_fk
            references guilds
            on update cascade on delete cascade,
    name          varchar(255)                        not null,
    "[timestamp]" timestamp default CURRENT_TIMESTAMP not null,
    constraint guilds_channels_pk
        primary key (guild_id, channel_id)
);

alter table guilds_channels
    owner to vasabi;

create table members
(
    user_id       integer not null
        constraint members_users_id_fk
            references users
            on update cascade on delete cascade,
    guild_id      integer not null
        constraint members_guilds_id_fk
            references guilds,
    nick          varchar(255),
    "[timestamp]" time default CURRENT_TIMESTAMP,
    constraint members_pk
        primary key (user_id, guild_id)
);

alter table members
    owner to vasabi;


