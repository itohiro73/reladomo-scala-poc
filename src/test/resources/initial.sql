drop table if exists parent_object;

create table parent_object
(
    id int not null,
    name varchar(255) not null,
    in_at timestamp not null,
    out_at timestamp not null
);

alter table parent_object add constraint parent_object_pk primary key (id, out_at);

drop table if exists bitemporal_child_object;

create table bitemporal_child_object
(
    id int not null,
    name varchar(255) not null,
    state int,
    parent_object_id int,
    from_at timestamp not null,
    thru_at timestamp not null,
    in_at timestamp not null,
    out_at timestamp not null
);

alter table bitemporal_child_object add constraint bitemporal_child_object_pk primary key (id, thru_at, out_at);

create index bitemporal_child_object_idx0 on bitemporal_child_object(parent_object_id, thru_at, out_at);

