--
--  Licensed to the Apache Software Foundation (ASF) under one or more
--  contributor license agreements.  See the NOTICE file distributed with
--  this work for additional information regarding copyright ownership.
--  The ASF licenses this file to You under the Apache License, Version 2.0
--  (the "License"); you may not use this file except in compliance with
--  the License.  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--  See the License for the specific language governing permissions and
--  limitations under the License.
--
-- DayTrader3 schema, ported verbatim from daytrader3-ee6-ejb/META-INF/daytrader.sql.
-- Kept as an explicit script (ddl-auto=none) so the legacy table shape is preserved:
-- HOLDING_HOLDINGID is a plain, non-unique indexed column (no UNIQUE / FK constraint),
-- which lets both the originating buy order and a later sell order reference the same
-- holding -- behavior Hibernate's @OneToOne DDL generation would otherwise forbid.

create table if not exists holdingejb
  (purchaseprice decimal(10, 2),
   holdingid integer not null,
   quantity double not null,
   purchasedate timestamp,
   account_accountid integer,
   quote_symbol varchar(250),
   optLock integer,
   constraint pk_holdingejb primary key (holdingid));

create table if not exists accountprofileejb
  (address varchar(250),
   passwd varchar(250),
   userid varchar(250) not null,
   email varchar(250),
   creditcard varchar(250),
   fullname varchar(250),
   optLock integer,
   constraint pk_accountprofile2 primary key (userid));

create table if not exists quoteejb
  (low decimal(10, 2),
   open1 decimal(10, 2),
   volume double not null,
   price decimal(10, 2),
   high decimal(10, 2),
   companyname varchar(250),
   symbol varchar(250) not null,
   change1 double not null,
   optLock integer,
   constraint pk_quoteejb primary key (symbol));

create table if not exists keygenejb
  (keyval integer not null,
   keyname varchar(250) not null,
   constraint pk_keygenejb primary key (keyname));

create table if not exists accountejb
  (creationdate timestamp,
   openbalance decimal(10, 2),
   logoutcount integer not null,
   balance decimal(10, 2),
   accountid integer not null,
   lastlogin timestamp,
   logincount integer not null,
   PROFILE_USERID VARCHAR(250),
   optLock integer,
   constraint pk_accountejb primary key (accountid));

create table if not exists orderejb
  (orderfee decimal(10, 2),
   completiondate timestamp,
   ordertype varchar(250),
   orderstatus varchar(250),
   price decimal(10, 2),
   quantity double not null,
   opendate timestamp,
   orderid integer not null,
   account_accountid integer,
   quote_symbol varchar(250),
   holding_holdingid integer,
   optLock integer,
   constraint pk_orderejb primary key (orderid));

create index if not exists profile_userid on accountejb(profile_userid);
create index if not exists account_accountid on holdingejb(account_accountid);
create index if not exists account_accountidt on orderejb(account_accountid);
create index if not exists holding_holdingid on orderejb(holding_holdingid);
create index if not exists orderstatus on orderejb(orderstatus);
create index if not exists ordertype on orderejb(ordertype);
