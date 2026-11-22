create table if not exists public.analises (
  id          bigint generated always as identity primary key,
  user_id     uuid not null references auth.users(id) on delete cascade,
  app_origem  text,
  suspeito    boolean not null,
  confianca   real not null,
  motivos     jsonb not null default '[]',
  criado_em   timestamptz not null default now()
);

create index if not exists analises_user_idx on public.analises (user_id, criado_em desc);

alter table public.analises enable row level security;

drop policy if exists "usuario le as proprias analises" on public.analises;
create policy "usuario le as proprias analises"
  on public.analises for select
  using (auth.uid() = user_id);

create table if not exists public.denuncias (
  id          bigint generated always as identity primary key,
  user_id     uuid not null references auth.users(id) on delete cascade,
  descricao   text not null,
  regiao      text,
  criado_em   timestamptz not null default now()
);

alter table public.denuncias enable row level security;

create table if not exists public.alertas_regionais (
  id          bigint generated always as identity primary key,
  titulo      text not null,
  regiao      text not null default 'Geral',
  criado_em   timestamptz not null default now()
);

alter table public.alertas_regionais enable row level security;

alter table public.analises drop column if exists texto;
