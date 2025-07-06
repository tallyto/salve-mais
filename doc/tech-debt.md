# Débitos Técnicos - Gestor Financeiro

Este documento lista os débitos técnicos identificados no projeto Gestor Financeiro, tanto no backend quanto no frontend. Estes são itens que podem ser trabalhados quando o backlog de funcionalidades não estiver pronto ou durante períodos de menor carga de desenvolvimento.

## Backend (Spring Boot)

### Testes

1. **Cobertura de Testes Insuficiente** - Prioridade: Alta
   - Problema: Apenas testes básicos de contexto estão presentes, sem testes unitários ou de integração para serviços e controllers.
   - Solução: Implementar testes unitários para services, repositories e controllers principais, começando com ao menos 50% de cobertura.
   - Benefício: Redução de regressões, maior confiabilidade e facilidade para refatorações futuras.
   - Esforço estimado: 3-5 dias de trabalho.

2. **Falta de Testes de Integração** - Prioridade: Média
   - Problema: Não existem testes que validem o fluxo completo das funcionalidades.
   - Solução: Criar testes de integração para fluxos críticos (autenticação, operações CRUD principais).
   - Esforço estimado: 2-3 dias.

### Arquitetura e Design de Código

3. **Uso excessivo de @Autowired** - Prioridade: Média
   - Problema: Injeção de dependências feita principalmente com @Autowired em campos, dificultando testes.
   - Solução: Refatorar para usar injeção via construtor, facilitando testes e explicitando dependências.
   - Esforço estimado: 1-2 dias.

4. **Padrão de Tratamento de Exceções Inconsistente** - Prioridade: Média
   - Problema: Embora exista um tratamento centralizado, algumas exceções podem não estar sendo capturadas adequadamente.
   - Solução: Revisão e padronização do tratamento de exceções em todos os controllers.
   - Esforço estimado: 1 dia.

5. **Ausência de Documentação de API Detalhada** - Prioridade: Baixa
   - Problema: Documentação Swagger/OpenAPI presente, mas com descrições limitadas.
   - Solução: Melhorar anotações OpenAPI com descrições detalhadas, exemplos e códigos de resposta.
   - Esforço estimado: 1-2 dias.

### Performance e Escalabilidade

6. **Otimização de Queries** - Prioridade: Média
   - Problema: Algumas consultas podem estar buscando mais dados que o necessário (N+1, falta de paginação).
   - Solução: Revisar queries críticas, especialmente no dashboard, e implementar paginação eficiente.
   - Esforço estimado: 2-3 dias.

7. **Implementação de Cache** - Prioridade: Baixa
   - Problema: Ausência de estratégia de cache para dados frequentemente acessados.
   - Solução: Implementar cache com Spring Cache em serviços com dados estáticos ou que mudam pouco.
   - Esforço estimado: 2 dias.

### Segurança

8. **Refresh Token** - Prioridade: Alta
   - Problema: Sistema utiliza apenas JWT sem refresh token, obrigando usuários a fazer login frequentemente.
   - Solução: Implementar mecanismo de refresh token para melhorar experiência de usuário.
   - Esforço estimado: 2-3 dias.

9. **Auditoria de Ações** - Prioridade: Baixa
   - Problema: Falta de registro detalhado de ações dos usuários para fins de segurança.
   - Solução: Implementar mecanismo de auditoria para operações críticas (financeiras, permissões).
   - Esforço estimado: 2-3 dias.

### DevOps

10. **CI/CD Automatizado** - Prioridade: Média
    - Problema: Processo de deploy manual, sujeito a erros.
    - Solução: Configurar pipeline CI/CD com GitHub Actions para testes automatizados e deploy.
    - Esforço estimado: 1-2 dias.

11. **Monitoramento em Produção** - Prioridade: Baixa
    - Problema: Falta de monitoramento adequado da aplicação em produção.
    - Solução: Configurar Spring Actuator e integrar com sistema de monitoramento.
    - Esforço estimado: 1 dia.

## Frontend (Angular)

### Testes

12. **Cobertura de Testes Insuficiente** - Prioridade: Alta
    - Problema: Poucos componentes possuem testes unitários implementados.
    - Solução: Implementar testes para componentes críticos (autenticação, dashboard, formulários principais).
    - Esforço estimado: 3-4 dias.

13. **Testes E2E Ausentes** - Prioridade: Baixa
    - Problema: Sem testes end-to-end para fluxos críticos.
    - Solução: Implementar testes E2E com Cypress ou Playwright para fluxos principais.
    - Esforço estimado: 2-3 dias.

### Arquitetura e Código

14. **Versões Diferentes de Angular Material** - Prioridade: Alta
    - Problema: Material na versão 16.x enquanto Angular está na 17.x, causando potenciais incompatibilidades.
    - Solução: Atualizar Angular Material para a versão compatível com o Angular 17.
    - Esforço estimado: 1-2 dias.

15. **Componentes com Muitas Responsabilidades** - Prioridade: Média
    - Problema: Alguns componentes estão muito grandes e com múltiplas responsabilidades.
    - Solução: Refatorar aplicando princípio de responsabilidade única, extraindo componentes menores.
    - Esforço estimado: 3-4 dias.

16. **Falta de Componentização de Elementos Comuns** - Prioridade: Média
    - Problema: Duplicação de código em componentes de formulário e listagens.
    - Solução: Criar componentes reutilizáveis para padrões comuns (formulários, tabelas, cards, etc).
    - Esforço estimado: 2-3 dias.

### Performance

17. **Lazy Loading de Módulos** - Prioridade: Média
    - Problema: A aplicação não está utilizando lazy loading para todos os módulos.
    - Solução: Implementar lazy loading para melhorar o tempo de carregamento inicial.
    - Esforço estimado: 1-2 dias.

18. **Otimização de Assets** - Prioridade: Baixa
    - Problema: Imagens e outros assets podem não estar otimizados para web.
    - Solução: Otimizar imagens, implementar lazy loading para imagens, e reduzir tamanho de bundle.
    - Esforço estimado: 1 dia.

### UX/UI

19. **Inconsistências Visuais entre Componentes** - Prioridade: Média
    - Problema: Alguns componentes não seguem o mesmo padrão visual.
    - Solução: Criar e aplicar um design system consistente com variáveis CSS/SCSS.
    - Esforço estimado: 2-3 dias.

20. **Melhorias de Acessibilidade** - Prioridade: Baixa
    - Problema: Falta de conformidade com diretrizes de acessibilidade WCAG.
    - Solução: Implementar melhorias de acessibilidade (contraste, textos alternativos, navegação por teclado).
    - Esforço estimado: 2-3 dias.

### Internacionalização

21. **Suporte a Múltiplos Idiomas** - Prioridade: Baixa
    - Problema: Aplicação está hardcoded para português.
    - Solução: Implementar i18n para permitir adição de outros idiomas no futuro.
    - Esforço estimado: 2-3 dias.

## Infraestrutura e Banco de Dados

22. **Schema Migrations para Updates Futuros** - Prioridade: Média
    - Problema: Alguns scripts de migração podem não seguir as melhores práticas.
    - Solução: Revisar e padronizar scripts de migração Flyway, garantindo idempotência.
    - Esforço estimado: 1-2 dias.

23. **Backup Automatizado de Banco de Dados** - Prioridade: Alta
    - Problema: Sem processo claro de backup e recuperação.
    - Solução: Implementar rotina automatizada de backup com retenção adequada.
    - Esforço estimado: 1 dia.

## Plano de Ação Recomendado

Para trabalhar nesses débitos técnicos de maneira eficiente, recomendamos priorizar da seguinte forma:

### Prioridade Imediata (Sprint 1)
1. Cobertura de Testes Backend (item 1)
2. Atualização do Angular Material (item 14)
3. Implementação de Refresh Token (item 8)
4. Backup Automatizado (item 23)

### Prioridade Secundária (Sprint 2)
5. Uso excessivo de @Autowired (item 3)
6. Componentes com Muitas Responsabilidades (item 15)
7. Otimização de Queries (item 6)
8. CI/CD Automatizado (item 10)

### Próximos Passos (Sprints Futuros)
9. Testes de Integração (item 2)
10. Lazy Loading de Módulos (item 17)
11. Falta de Componentização (item 16)
12. Inconsistências Visuais (item 19)

Este documento deve ser revisado e atualizado periodicamente à medida que novos débitos técnicos são identificados ou itens existentes são resolvidos.
