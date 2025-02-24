
-- .mode table or .mode box;
-- .read books.sql;
--
-- to enable foreign key support:
--   pragma foreign_keys = on;
-- use with:
--   foreign key (col) references table (col)


-- Clean up before running the script.

drop table if exists Books;
drop table if exists Authors;
drop table if exists Publishers;
drop table if exists Write;

-- Create tables.

create table Books (
  isbn text primary key,
  title text,
  year integer,
  pubId integer
);

--create table Publish (
--  isbn text,
--  pubId integer
--);

create table Authors (
  authorId integer primary key,
  name text,
  yearBirth integer
);

create table Write (
  isbn string,
  authorId integer
);

create table Publishers (
  pubId integer primary key,
  name text
);

-- Fill tables.

insert into Publishers values (101, 'McMillian');
insert into Publishers values (102, 'Little Brown & Co');

insert into Books values ('0771595565', 'Rebel Angels', 1981, 101);
insert into Books values ('0316296198', 'The Magus', 1965, 102);
insert into Books values ('0670312134', 'Fifth Business', 1970, 101);

insert into Authors values (1, 'Robertson Davies', 1913);
insert into Authors values (2, 'John Fowles', 1926);

insert into Write values ('0771595565', 1);
insert into Write values ('0316296198', 2);
insert into Write values ('0670312134', 1);
