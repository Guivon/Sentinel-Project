-- Rode no Supabase: SQL Editor > New query
create table if not exists public.analises (
  id          bigint generated always as identity primary key,
  user_id     uuid not null references auth.users(id) on delete cascade,
  app_origem  text,
  texto       text,
  suspeito    boolean not null,
  confianca   real not null,
  motivos     jsonb not null default '[]',
  criado_em   timestamptz not null default now()
);

create index if not exists analises_user_idx on public.analises (user_id, criado_em desc);

-- Segurança: cada usuário só enxerga as próprias análises.
-- O back end grava com a service_role, que ignora o RLS.
alter table public.analises enable row level security;

create policy "usuario le as proprias analises"
  on public.analises for select
  using (auth.uid() = user_id);
