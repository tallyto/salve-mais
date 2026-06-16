# Retomada — Salve Mais

> **Comece por aqui.** Para entender a arquitetura do projeto (multi-tenant,
> camadas, segurança, billing), leia `ARQUITETURA.md` neste repositório.
> Para o frontend, leia `ARQUITETURA.md` em `/home/tallyto/projetos/salve-mais-ui`.
> Este arquivo cobre apenas: em qual fase estamos, o que falta, e lembretes
> operacionais para retomar o trabalho sem reler tudo do zero.

## Estado atual (2026-06-07)

| Frente | Estado |
| --- | --- |
| Backend — Fases 1-3 (fundação de assinatura, enforcement, Stripe) | ✅ Concluídas (`ff96bcb`, `91d5775`, `ecb5424`) |
| Backend — endpoint de catálogo de planos (`GET /api/billing/planos`) | ✅ Concluído (`b34f9d0`, v1.21.0) |
| Frontend — Fase 4 (billing/assinatura na UI) | ✅ Concluída (`c0cf958` no salve-mais-ui, v1.35.0) |
| Frontend — reforma visual "fintech" (item 1 — tema global) | ✅ Concluído (v1.38.0 no salve-mais-ui) |
| Ambiente de dev local (DevDataSeeder + docker-compose) | 🔧 Em andamento — ver seção abaixo |
| Fase 5 — Landing Page (salve-mais-page) | 🔜 Próxima fase de produtização |
| Fase 6 — Admin Backoffice (salve-mais-admin) | ⏳ Planejada |

## Frentes em aberto (em ordem de prioridade sugerida)

### 1. Ambiente de dev local — finalizar e validar

Há trabalho recente (não commitado até a sessão de 2026-06-07) para destravar
testes manuais locais, já que não existem credenciais de teste para o ambiente
de produção:

- `src/main/java/.../core/database/DevDataSeeder.java` — `@Profile("dev")`,
  cria tenant `dev.salvemais.local` + usuário `teste@salvemais.com.br`/`teste123`
  com plano Enterprise/status ATIVO ao subir a aplicação
- `docker-compose.yml` — adiciona volume persistente, healthcheck do Postgres
  e um Adminer (`localhost:8081`) para inspecionar o banco

**Para concluir:** subir com `docker-compose up`, rodar o backend com perfil
`dev` (`SPRING_PROFILES_ACTIVE=dev`), confirmar que o seeder cria o usuário e
testar o login fim a fim no frontend (`npm run local` apontando para
`localhost:8080`). Depois: commit + changelog + bump de versão.

### 2. Fase 5 — Landing Page (salve-mais-page)

Criar **novo projeto** Angular + PrimeNG (baseado em `/home/tallyto/projetos/sgtur-page`):

- Página pública com comparativo de planos e preços (consumir `GET /api/billing/planos`)
- CTA "Começar grátis" → `/register` no salve-mais-ui
- FAQ

### 3. Fase 6 — Admin Backoffice (salve-mais-admin)

Criar **novo projeto** Angular + PrimeNG (baseado em `/home/tallyto/projetos/sgtur-admin`):

- Lista de tenants com status, plano e data de vencimento
- Painel de MRR (Monthly Recurring Revenue)
- Ação de intervenção manual (trocar status, sincronizar planos Stripe)

### 4. Reforma visual "fintech" do frontend (paralelo, sem dependência)

Ver `BACKLOG_UI_PROFISSIONAL.md` no `salve-mais-ui` — item 1 (tema global) já
concluído; seguem itens 2-6 (login, navegação, telas CRUD, componentes
compartilhados, polish). Pode ser feito em paralelo às fases acima, em sessões
curtas por tela/área.

## Lembretes técnicos críticos

```bash
# SEMPRE compilar/rodar o backend com Java 21 (Java 26 quebra o plugin de compilação)
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw compile -q
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw spring-boot:run

docker-compose up        # sobe infra local (Postgres + Mailhog + Adminer)
```

### Variáveis de ambiente Stripe (produção)

```bash
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_SUCCESS_URL=https://www.salvemais.com.br/#/billing/sucesso
STRIPE_CANCEL_URL=https://www.salvemais.com.br/#/billing/cancelado
```

### Ativar os planos no Stripe

Após criar os produtos no dashboard do Stripe, cadastrar o `stripePriceId` de
cada plano (não há sincronização automática ainda):

```sql
UPDATE public.planos SET stripe_price_id = 'price_xxx' WHERE tipo = 'BASIC';
UPDATE public.planos SET stripe_price_id = 'price_yyy' WHERE tipo = 'PREMIUM';
UPDATE public.planos SET stripe_price_id = 'price_zzz' WHERE tipo = 'ENTERPRISE';
```

## Workflow padrão ao concluir cada fase/feature

1. `git add` nos arquivos da fase
2. `git commit` com mensagem `feat: fase X - ...` (ou `feat:`/`fix:`/`chore:` conforme o caso)
3. Atualizar `CHANGELOG.md` com o que foi implementado
4. Bump de versão no `pom.xml` (backend) ou `package.json` (frontend)
5. Atualizar a tabela de roadmap no `README.md` marcando a fase como concluída
   (e, se aplicável, o checklist no documento de backlog específico)

## Sessão atual

### 2026-06-15 - dashboard trends

- Objetivo: reduzir o tamanho do `DashboardService` sem mudar o contrato do controller.
- O que foi feito: criei `DashboardTrendService` e movi para ele a lógica de tendência mensal, tendência anual e variações.
- O que foi feito: `DashboardService` agora delega esses métodos e fica focado em resumo, despesas por categoria e regra 50/30/20.
- O que foi feito: adicionei `DashboardTrendServiceTest` cobrindo tendência mensal e variações sem reserva de emergência.
- Testes executados: `./mvnw test -q`
- Resultado: suíte passou.
- Próximo passo: se continuar a limpeza, separar o bloco de resumo/regra 50/30/20 do `DashboardService`.

### 2026-06-15 - dashboard overview

- Objetivo: separar o bloco de resumo e regra 50/30/20 do dashboard.
- O que foi feito: criei `DashboardOverviewService` e movi para ele `getSummary()` e `getBudgetRule()`.
- O que foi feito: `DashboardService` virou uma fachada fina que delega para `DashboardTrendService` e `DashboardOverviewService`.
- O que foi feito: adicionei `DashboardOverviewServiceTest` cobrindo resumo vazio e regra 50/30/20 sem movimento.
- Testes executados: `./mvnw test -q`
- Resultado: suíte passou.
- Próximo passo: a próxima divisão natural é revisar `DashboardService` e ver se `getExpensesByCategory()` também deve ser extraído.

### 2026-06-15 - dashboard category expenses

- Objetivo: extrair `getExpensesByCategory()` do `DashboardService`.
- O que foi feito: criei `DashboardCategoryExpenseService` e movi para ele a montagem das despesas por categoria.
- O que foi feito: removi `DashboardService` e liguei `DashboardController` e `ExportService` diretamente aos serviços específicos.
- O que foi feito: adicionei `DashboardCategoryExpenseServiceTest` cobrindo o agrupamento básico por categoria.
- Testes executados: `./mvnw test -q`
- Resultado: suíte passou.
- Próximo passo: a próxima limpeza natural do dashboard é opcional; a arquitetura funcional já está dividida entre serviços específicos.

### 2026-06-15 - conta fixa comprovantes

- Objetivo: reduzir o acoplamento do `ContaFixaService` sem mexer nos contratos dos controllers.
- O que foi feito: criei `ContaFixaComprovanteService` e movi para ele as operações de upload, listagem, download e remoção de comprovantes.
- O que foi feito: `ContaFixaService` agora só delega as operações de comprovantes para o serviço novo.
- O que foi feito: adicionei `ContaFixaComprovanteServiceTest` cobrindo os fluxos de comprovante e a validação de conta fixa inexistente.
- Testes executados: `./mvnw test -q`
- Resultado: suíte passou.
- Próximo passo: o próximo recorte natural em `ContaFixaService` é separar exportação/relatórios, se ainda for necessário.

### 2026-06-15 - relatório mensal comparativo

- Objetivo: reduzir o tamanho do `RelatorioMensalService` sem mudar os endpoints.
- O que foi feito: criei `ComparativoMensalService` e movi para ele toda a lógica de comparação entre dois relatórios mensais.
- O que foi feito: `RelatorioMensalService` agora gera o relatório base e delega o comparativo para o serviço novo.
- O que foi feito: adicionei `ComparativoMensalServiceTest` cobrindo resumo, variação e status geral.
- Testes executados: pendente nesta sessão.
- Próximo passo: validar a suíte e, se passar, commitar esta fatia antes de atacar o próximo serviço grande.

### 2026-06-15 - tenant export

- Objetivo: reduzir o acoplamento do `TenantService` extraindo a exportação de dados do tenant.
- O que foi feito: criei `TenantExportService` e movi para ele `exportarDadosTenant(...)`.
- O que foi feito: `TenantService` agora delega a exportação para o serviço novo.
- O que foi feito: atualizei a versão do artefato para `1.21.2` no `pom.xml`.
- O que foi feito: adicionei `TenantExportServiceTest` cobrindo o payload exportado.
- O que foi feito: substituí `Map` por `TenantExportDTO` e `UsuarioExportDTO` para tipar a exportação.
- Testes executados: `./mvnw test -q`
- Resultado: suíte passou.
- Próximo passo: commitar a fatia e continuar quebrando `TenantService` nas responsabilidades restantes.
