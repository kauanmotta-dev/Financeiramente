# Plano Cirurgico - Remover Responsabilidades de Backend do Frontend

## Objetivo
Mover regras de negocio, orquestracao de aplicacao e acesso a persistencia para o `core`, deixando `android` e `desktop` apenas com responsabilidades de apresentacao.

## Definicao de pronto
- Nenhuma `Fragment`, `ViewModel` Android ou `Controller` Desktop acessa `Repository` diretamente.
- Nenhum modulo de frontend instancia `Dao`, `DatabaseDriver` ou executa `DatabaseMigrator`.
- Regras de negocio (filtro, agregacao, classificacao, calculo de alerta, periodos) ficam em `usecase`.
- Frontend consome apenas DTOs/use cases de aplicacao.

## Diagnostico atual (fora do core)

### Infraestrutura e composicao (critico)
1. `android` e `desktop` compoem dependencias de dominio e persistencia.
   - Arquivos: `android/src/main/java/com/financeiramente/android/app/AppContext.java`, `desktop/src/main/java/com/financeiramente/desktop/app/AppContext.java`
2. Drivers de banco vivem no frontend.
   - Arquivos: `android/src/main/java/com/financeiramente/android/db/AndroidDatabaseDriver.java`, `desktop/src/main/java/com/financeiramente/desktop/db/JdbcDatabaseDriver.java`
3. Migracao e recuperacao de banco executadas no frontend.
   - Arquivos: `android/src/main/java/com/financeiramente/android/app/AppContext.java`, `desktop/src/main/java/com/financeiramente/desktop/app/AppContext.java`

### Regra de negocio no frontend (critico)
1. Regras de filtro/agregacao de relatorio no Fragment.
   - Arquivo: `android/src/main/java/com/financeiramente/android/ui/relatorios/RelatoriosFragment.java`
2. Regras de alerta e score do dashboard no Fragment.
   - Arquivo: `android/src/main/java/com/financeiramente/android/ui/dashboard/DashboardFragment.java`
3. Regras de consolidacao hierarquica por categoria no ViewModel.
   - Arquivo: `android/src/main/java/com/financeiramente/android/viewmodel/GastosCategoriaViewModel.java`
4. Regras de periodo e historico mensal no ViewModel.
   - Arquivo: `android/src/main/java/com/financeiramente/android/viewmodel/RelatoriosViewModel.java`
5. Regras de validacao/normalizacao de dominio em formularios.
   - Arquivos: `android/src/main/java/com/financeiramente/android/ui/configuracoes/TagsFragment.java`, `android/src/main/java/com/financeiramente/android/ui/categorias/CategoriaFormFragment.java`, `desktop/src/main/java/com/financeiramente/desktop/ui/MetasController.java`, `desktop/src/main/java/com/financeiramente/desktop/ui/CategoriasController.java`

### Leitura direta de repositorio em UI/ViewModel (alto)
- Android: `CartaoListViewModel`, `FaturaListViewModel`, `FaturaDetalheViewModel`, `LancamentoFormViewModel`, `LancamentosListViewModel`, `MetasViewModel`, `MetaDetalheViewModel`, `RelatoriosViewModel`, `CategoriasViewModel`, `DashboardFragment`, `TagsFragment`.
- Desktop: `RelatoriosController`, `MetasController`, `CategoriasController`, `LancamentoFormController`.

---

## Estrategia de migracao
Aplicar por fatias verticais, reduzindo risco:
1. Fechar acesso direto a `Repository` no frontend.
2. Introduzir use cases de consulta (query use cases) para leitura.
3. Mover calculos de regra para use cases retornando DTO de tela.
4. Centralizar bootstrap/persistencia em camada de infraestrutura compartilhada (nao na UI).

---

## Backlog tecnico (arquivo por arquivo)

## P0 - Bloqueadores arquiteturais

1. `android/src/main/java/com/financeiramente/android/app/AppContext.java`
- Remover exposicao de `get*Repository()`.
- Manter apenas factories/provedores de use cases.
- Extrair composicao para `core` (ex.: `CoreBootstrap`), frontend apenas consome o bootstrap.

2. `desktop/src/main/java/com/financeiramente/desktop/app/AppContext.java`
- Mesmo ajuste do Android.
- Remover `get*Repository()` e acesso de controllers a repositorio.

3. `android/src/main/java/com/financeiramente/android/db/AndroidDatabaseDriver.java`
4. `desktop/src/main/java/com/financeiramente/desktop/db/JdbcDatabaseDriver.java`
- Mover para um modulo de infraestrutura (sugestao: `core-infra` ou `data`), fora da camada de apresentacao.

5. `core/src/main/java/com/financeiramente/core/bootstrap/` (novo)
- Criar `CoreBootstrap` com metodos:
  - `createAndroidServices(context)`
  - `createDesktopServices(path)`
- Encapsular migracao e montagem de dependencias.

## P1 - Dashboard

6. `android/src/main/java/com/financeiramente/android/ui/dashboard/DashboardFragment.java`
- Remover acesso a `FaturaRepository`.
- Remover calculo de niveis de alerta e score de metas do Fragment.

7. `core/src/main/java/com/financeiramente/core/usecase/dashboard/ListarProximasFaturasDashboardUseCase.java` (novo)
- Entrada: limite opcional.
- Saida: lista pronta para exibicao no card de proximas faturas.

8. `core/src/main/java/com/financeiramente/core/usecase/dashboard/CalcularAvisosDashboardUseCase.java` (novo)
- Entrada: contexto mensal (receitas, limites essenciais, percentual metas).
- Saida: DTO com nivel e mensagem de cada aviso.

9. `core/src/main/java/com/financeiramente/core/usecase/dashboard/DashboardResumoResult.java` (novo)
- DTO unificado para tela (saldo, progresso, avisos e proximas faturas).

## P1 - Relatorios

10. `android/src/main/java/com/financeiramente/android/ui/relatorios/RelatoriosFragment.java`
- Remover `aplicarFiltrosLocais`, `matches*`, `encontrarCategoriaRaiz`.
- Fragment vira somente binding e eventos de filtro.

11. `android/src/main/java/com/financeiramente/android/viewmodel/RelatoriosViewModel.java`
- Remover leitura de `CategoriaRepository`, `TagRepository`, `LancamentoRepository`.
- ViewModel consome apenas use cases.

12. `core/src/main/java/com/financeiramente/core/usecase/relatorio/FiltrarRelatorioComTaxonomiaUseCase.java` (novo)
- Faz filtros por tipo de categoria, categoria raiz e tags.
- Retorna `RelatorioResult` ja consolidado.

13. `core/src/main/java/com/financeiramente/core/usecase/relatorio/CarregarDadosFiltroRelatorioUseCase.java` (novo)
- Carrega categorias/tags para os chips.

14. `core/src/main/java/com/financeiramente/core/usecase/relatorio/GerarHistoricoMensalRelatorioUseCase.java` (novo)
- Gera receitas/despesas e labels dos ultimos N meses.

## P1 - Gastos por categoria

15. `android/src/main/java/com/financeiramente/android/viewmodel/GastosCategoriaViewModel.java`
- Remover consolidacao de arvore e limites efetivos.
- Remover classificacao por tipo.

16. `core/src/main/java/com/financeiramente/core/usecase/categoria/ConsolidarGastosCategoriaMesUseCase.java` (novo)
- Entrada: ano/mes.
- Saida: secoes por tipo + nos hierarquicos + totais.

17. `core/src/main/java/com/financeiramente/core/usecase/categoria/GastosCategoriaMesResult.java` (novo)
- DTO de apresentacao para Android/Desktop.

## P1 - Cartao/Fatura

18. `android/src/main/java/com/financeiramente/android/viewmodel/CartaoListViewModel.java`
- Trocar `CartaoCreditoRepository` e `FaturaRepository` por use cases:
  - `ListarCartoesAtivosUseCase`
  - `CalcularLimiteUtilizadoCartaoUseCase`

19. `android/src/main/java/com/financeiramente/android/viewmodel/FaturaListViewModel.java`
- Trocar `FaturaRepository` por `ListarFaturasPorCartaoUseCase`.

20. `android/src/main/java/com/financeiramente/android/viewmodel/FaturaDetalheViewModel.java`
- Trocar leitura direta por `BuscarFaturaDetalheUseCase`.
- Pagamento continua em use case, sem queries auxiliares no ViewModel.

21. `core/src/main/java/com/financeiramente/core/usecase/fatura/BuscarFaturaDetalheUseCase.java` (novo)
22. `core/src/main/java/com/financeiramente/core/usecase/fatura/ListarFaturasPorCartaoUseCase.java` (novo)
23. `core/src/main/java/com/financeiramente/core/usecase/cartao/ListarCartoesAtivosUseCase.java` (novo)
24. `core/src/main/java/com/financeiramente/core/usecase/cartao/CalcularLimiteUtilizadoCartaoUseCase.java` (novo)

## P2 - Categorias, metas, tags e lancamentos

25. `android/src/main/java/com/financeiramente/android/ui/configuracoes/TagsFragment.java`
- Remover CRUD direto de `TagRepository`.
- Criar e usar:
  - `CriarTagUseCase`
  - `EditarTagUseCase`
  - `ExcluirTagUseCase`
  - `ListarTagsUseCase`

26. `android/src/main/java/com/financeiramente/android/ui/categorias/CategoriaFormFragment.java`
- Remover `buscarPorId` direto para herdar cor/icone/tipo.
- Criar use case de carga para edicao:
  - `CarregarCategoriaParaEdicaoUseCase`

27. `desktop/src/main/java/com/financeiramente/desktop/ui/RelatoriosController.java`
- Remover repositorios diretos de categoria/tag.
- Consumir use cases de filtros/relatorio.

28. `desktop/src/main/java/com/financeiramente/desktop/ui/MetasController.java`
- Remover `metaRepository` e `aporteRepository` diretos.
- Criar/use cases de leitura:
  - `ListarMetasAtivasUseCase`
  - `ListarAportesPorMetaUseCase`
  - `BuscarMetaPorIdUseCase`

29. `desktop/src/main/java/com/financeiramente/desktop/ui/CategoriasController.java`
- Remover acesso direto ao repository no carregamento da arvore.
- Criar `ListarArvoreCategoriasUseCase`.

30. `desktop/src/main/java/com/financeiramente/desktop/ui/LancamentoFormController.java`
- Remover `getCategoriaRepository().listarTodas()`.
- Criar `ListarCategoriasCompativeisComTipoUseCase`.

---

## Ordem de execucao sugerida (sprints)

### Sprint 1 (P0)
1. Introduzir `CoreBootstrap` no core.
2. Mover migracao/composicao para bootstrap.
3. Encerrar `get*Repository()` em `AppContext` Android/Desktop.

### Sprint 2 (P1 Dashboard + Fatura/Cartao)
1. Criar use cases de dashboard/fatura/cartao.
2. Refatorar `DashboardFragment`, `CartaoListViewModel`, `FaturaListViewModel`, `FaturaDetalheViewModel`.

### Sprint 3 (P1 Relatorios + Gastos)
1. Criar use cases de consulta e consolidacao.
2. Refatorar `RelatoriosFragment`, `RelatoriosViewModel`, `GastosCategoriaViewModel`.

### Sprint 4 (P2 restante)
1. Tags, categorias, metas e lancamentos desktop/android.
2. Padronizacao final e limpeza de APIs antigas.

---

## Checklist de governanca para nao regredir
1. Regra: frontend nao importa `repository` nem `dao`.
2. Regra: frontend nao chama `DatabaseMigrator`.
3. Regra: qualquer calculo de negocio novo entra como use case no `core`.
4. Teste de arquitetura (grep no CI) para bloquear imports proibidos.

## Verificacao automatizavel
- Bloquear no CI ocorrencias de:
  - `import com.financeiramente.core.repository.` em `android/src/main/java` e `desktop/src/main/java`.
  - `new .*Dao\(` fora do core.
  - `new DatabaseMigrator` fora do core.

## Resultado esperado
Com esse plano, o frontend fica fino (UI + estado de tela), o core volta a concentrar comportamento de negocio e a evolucao do backend fica mais segura, testavel e previsivel.