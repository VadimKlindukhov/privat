create table if not exists orders (
    id uuid primary key,
    client_id uuid not null,
    amount numeric(19, 2) not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index if not exists idx_orders_client_id on orders (client_id);

-- id is the X-Idempotency-Key value itself. Inserted with order_id = NULL to CLAIM the key before the
-- order exists: a concurrent request with the same key blocks on this insert until the claiming
-- transaction commits or rolls back, instead of racing to create a duplicate order. order_id is
-- backfilled once the order is created, in the same transaction.
create table if not exists orders_idempotency (
    id uuid primary key,
    order_id uuid references orders (id),
    created_at timestamptz not null
);

-- running total of order amounts per client per calendar day, used to enforce the daily limit.
create table if not exists orders_daily_usage (
    client_id uuid not null,
    date date not null,
    amount numeric(19, 2) not null,
    primary key (client_id, date)
);

create table if not exists orders_status_history (
    order_id uuid not null references orders (id),
    status varchar(20) not null,
    note varchar(500),
    date_time timestamptz not null
);

create index if not exists idx_orders_status_history_order_id on orders_status_history (order_id);
