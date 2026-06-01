# Financeiramente

> Aplicativo pessoal de organização financeira — controle diário de receitas, despesas, metas e cartões de crédito, com dados 100% locais.

![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Build-Gradle%209.4-02303A?logo=gradle&logoColor=white)
![SQLite](https://img.shields.io/badge/Database-SQLite-003B57?logo=sqlite&logoColor=white)
![Android Status](https://img.shields.io/badge/Android-Em%20Testes-2EA043)
![Desktop Status](https://img.shields.io/badge/Desktop-Em%20Desenvolvimento-F29D38)

---

## Sobre o projeto

O **Financeiramente** nasceu como um projeto pessoal para resolver uma necessidade real: ter controle financeiro claro, sem depender de servidores externos ou assinaturas. Tudo roda localmente no dispositivo via SQLite.

O código está aberto para que a comunidade possa usar, estudar e contribuir. A versão Android está em fase de testes. A versão Desktop (JavaFX) está sendo construída em paralelo, reutilizando toda a lógica de negócio do módulo `core`.

---

## Status

| Plataforma | Status |
|---|---|
| Android (`android/`) | ✅ Funcional — em fase de testes |
| Desktop (`desktop/`) | 🚧 Em desenvolvimento ativo |

---

## Download (Android)

Os APKs são publicados na [página de releases](https://github.com/kauanmotta-dev/Financeiramente/releases).

```
https://github.com/kauanmotta-dev/Financeiramente/releases/latest/download/financeiramente-android.apk
```
---

## Funcionalidades

### Dashboard
- Saldo disponível e saldo real do período.
- Totais de receitas e despesas.
- Gráfico de pizza por categoria.

### Lançamentos
- Registro de receitas e despesas.
- Classificação por categoria e tags personalizadas (com emoji e cor).
- Lançamentos recorrentes (diário, semanal, mensal, anual) — gerados automaticamente no mês.

### Categorias
- Hierarquia de dois níveis (categoria pai + subcategorias).
- Três tipos: **Essencial**, **Não Essencial** e **Receita**.
- Limite mensal por categoria com indicador de status (verde / amarelo / vermelho).
- Onboarding com categorias pré-configuradas no primeiro uso.

### Metas financeiras
- Definição de valor objetivo com data-alvo opcional.
- Registro de aportes com histórico.
- Projeção automática de data de conclusão com base nos aportes.

### Cartões de crédito
- Cadastro de cartões com limite, dia de vencimento e dia de fechamento.
- Compras à vista, parceladas e recorrentes no cartão.
- Controle de faturas: **aberta**, **fechada**, **paga** e **paga parcialmente**.
- Antecipação de lançamentos de fatura.

### Relatórios e gráficos
- Filtros por período, tipo de categoria, categorias e tags (chips selecionáveis).
- Gráfico de barras horizontais por categoria (separando crédito e débito).
- Gráfico de evolução temporal (linha por dia, séries de gastos e receitas).
- Distribuição por tipo de categoria e por tags.

---

## Arquitetura

O projeto é **multi-módulo Gradle**, separando domínio de interface:

```
Financeiramente/
├── core/        ← Domínio puro: entidades, repositórios, DAOs, casos de uso, migrações
├── android/     ← UI Android: Fragments, ViewModels, Navigation, Material Design
└── desktop/     ← UI Desktop: JavaFX + FXML (em desenvolvimento)
```

Toda a lógica financeira fica em `core`, sem dependência de Android ou JavaFX. Isso garante que as regras de negócio sejam idênticas nas duas plataformas e facilita testes unitários isolados.

### Padrões utilizados

- **Use Case** — cada operação de negócio em sua própria classe
- **Repository + DAO** — abstração de acesso a dados desacoplada do banco
- **Builder** — construção de entidades (Lancamento, Categoria)
- **RowMapper** — interface funcional para mapear resultados de query
- **Optional** — tratamento de valores nulos sem NPE

---

## Stack técnica

| Camada | Tecnologias |
|---|---|
| Linguagem | Java 17 |
| Build | Gradle 9.4 (multi-módulo) |
| Mobile | Android SDK — minSdk 26, targetSdk 35 |
| UI Android | AndroidX · Material Design 3 · Navigation Component |
| Arquitetura Android | ViewModel + LiveData |
| Gráficos | MPAndroidChart |
| Loading | Facebook Shimmer |
| UI Desktop | JavaFX 17 + FXML |
| Banco de dados | SQLite (Android nativo · sqlite-jdbc no Desktop) |
| Testes | JUnit 5 + Mockito |

---

## Como executar

### Android — Android Studio

1. Abra o projeto no **Android Studio**.
2. Aguarde a sincronização do Gradle.
3. Selecione um dispositivo físico ou emulador (API 26+).
4. Execute o módulo `android`.

### Android — linha de comando

```bash
# Linux / macOS
./gradlew :android:assembleDebug

# Windows
gradlew.bat :android:assembleDebug
```

APK gerado em:
```
android/build/outputs/apk/debug/android-debug.apk
```

### Gerar APK de release (com keystore configurado)

Configure `keystore.properties` na raiz com os campos `storeFile`, `storePassword`, `keyAlias` e `keyPassword`, então execute:

```bash
./gradlew :android:assembleRelease
```

### Desktop

> Requer JDK 17+.

```bash
./gradlew :desktop:run
```

---

## Testes

Os testes cobrem a lógica do módulo `core` (casos de uso, validações e cálculos financeiros).

```bash
./gradlew :core:test
```

Relatório HTML gerado em:
```
core/build/reports/tests/test/index.html
```

---

## Roadmap

- [ ] Publicar primeira release Android com APK assinado
- [ ] Concluir frontend Desktop com paridade funcional ao Android
- [ ] Ampliar cobertura de testes (integração e regressão)
- [ ] Exportação de dados (CSV / PDF)

---

## Contribuindo

Contribuições são bem-vindas!

1. Abra uma **issue** descrevendo a proposta ou bug.
2. Crie uma branch a partir de `main`.
3. Envie um **pull request** com contexto claro da mudança.

Prefira mudanças pequenas e focadas. Alterações na lógica de `core` devem vir acompanhadas de testes.

---

## Licença

Distribuído sob a licença **Apache 2.0**. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.



