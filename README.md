# Financeiramente

Aplicativo de organizacao financeira pessoal com foco em controle diario de receitas, despesas, metas e cartoes de credito.

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle&logoColor=white)
![SQLite](https://img.shields.io/badge/Database-SQLite-003B57?logo=sqlite&logoColor=white)
![Status](https://img.shields.io/badge/Android-Stable-2EA043)
![Desktop](https://img.shields.io/badge/Desktop-In%20Development-F29D38)

## Navegacao Rapida

- [Visao Geral](#visao-geral)
- [Status do Projeto](#status-do-projeto)
- [Download do APK](#download-do-apk)
- [Principais Funcionalidades](#principais-funcionalidades)
- [Fluxo Recomendado](#fluxo-recomendado)
- [Diferenciais](#diferenciais)
- [Arquitetura e Stack](#arquitetura-e-stack)
- [Como Executar (Android)](#como-executar-android)
- [Gerar APK de Release](#gerar-apk-de-release)
- [Testes](#testes)
- [Roadmap](#roadmap)
- [Contribuicao](#contribuicao)
- [Observacoes](#observacoes)

---

## Visao Geral

O Financeiramente foi construido para ajudar no planejamento financeiro de forma simples e visual:

- Registro de lancamentos (receita e despesa).
- Organizacao por categorias e tags.
- Acompanhamento de metas financeiras.
- Controle de cartoes, compras, faturas e pagamentos.
- Paineis de relatorio e graficos para tomada de decisao.

## Status do Projeto

| Plataforma | Status |
| --- | --- |
| Android | Concluido e pronto para publicacao |
| Desktop (`desktop/*`) | Em desenvolvimento ativo |

> Objetivo atual: publicar na branch `main` a aplicacao sem `desktop/*`, enquanto a versao Desktop evolui separadamente.

## Download do APK

- Pagina oficial de releases: https://github.com/kauanmotta-dev/Financeiramente/releases
- Link direto do APK (apos publicar release com o nome `financeiramente-android.apk`):
  - https://github.com/kauanmotta-dev/Financeiramente/releases/latest/download/financeiramente-android.apk

## Principais Funcionalidades

### Financeiro diario

- Dashboard com saldo disponivel, saldo real, receitas e despesas do periodo.
- Lancamentos com suporte a receita e despesa.
- Etiquetas com tags personalizadas.

### Organizacao por categorias

- Categorias para receitas e despesas.
- Classificacao de despesas em essenciais e nao essenciais.
- Suporte a subcategorias.
- Personalizacao com emoji e cor.

### Metas e planejamento

- Metas com valor objetivo.
- Valor inicial opcional.
- Historico de aportes.
- Projecao de conclusao.

### Cartoes e faturas

- Cadastro de cartoes com limite, vencimento e fechamento.
- Compras no cartao: a vista, parceladas e recorrentes.
- Controle de faturas: aberta, fechada, paga e paga parcialmente.
- Pagamento e antecipacao de lancamentos de fatura.

### Relatorios e analise

- Relatorios e graficos por periodo.
- Filtros por tipo de categoria, categorias e tags.

## Fluxo Recomendado

1. Configure categorias e tags em Preferencias.
2. Cadastre seus cartoes de credito (se usar cartao).
3. Registre lancamentos diarios (receitas e despesas).
4. Crie metas e lance aportes periodicos.
5. Acompanhe Dashboard, Gastos e Graficos para ajustar seu planejamento.

## Diferenciais

- Arquitetura modular (`core`, `android`, `desktop`) para reaproveitamento de regras de negocio.
- Banco local SQLite (uso offline, sem dependencia de servidor para operacao basica).
- Regras financeiras centralizadas no `core`, reduzindo inconsistencias entre plataformas.
- UX voltada a clareza visual com chips de filtro, secoes por tipo, graficos e empty states.

## Arquitetura e Stack

### Modulos

| Modulo | Responsabilidade |
| --- | --- |
| `core/` | Dominio, repositorios, DAOs, casos de uso e migracoes de banco |
| `android/` | Interface Android (Fragments, ViewModels, Navigation, Material Design) |
| `desktop/` | Interface JavaFX (em evolucao) |

### Stack Tecnica

| Camada | Tecnologias |
| --- | --- |
| Linguagem | Java 17 |
| Build | Gradle (multi-modulo) |
| Mobile | Android SDK (minSdk 26, targetSdk 35, compileSdk 35) |
| UI | AndroidX + Material Design |
| Arquitetura UI | Navigation Component + ViewModel + LiveData |
| Banco | SQLite |
| Visualizacao | MPAndroidChart |
| Loading | Facebook Shimmer |
| Testes | JUnit 5 + Mockito |

## Requisitos

- JDK 17+
- Android Studio
- SDK Android instalado (API 35)
- Gradle Wrapper (ja incluso no projeto)

## Como Executar (Android)

### Opcao 1: Android Studio

1. Abra o projeto no Android Studio.
2. Aguarde sincronizacao do Gradle.
3. Selecione um dispositivo ou emulador.
4. Execute o modulo `android`.

### Opcao 2: Linha de comando

```bash
./gradlew :android:assembleDebug
```

No Windows (PowerShell/cmd):

```bat
gradlew.bat :android:assembleDebug
```

APK gerado em:

`android/build/outputs/apk/debug/android-debug.apk`

## Gerar APK de Release

Com assinatura configurada em `keystore.properties`:

```bash
./gradlew :android:assembleRelease
```

Saida esperada:

`android/build/outputs/apk/release/android-release.apk`

## Testes

Executar testes do modulo core:

```bash
./gradlew :core:test
```

## Roadmap

- Consolidar versao Desktop com o mesmo nivel funcional do Android.
- Evoluir experiencia de distribuicao com releases versionadas e APK pronto para download.
- Expandir cobertura de testes automatizados para cenarios de integracao e regressao.

## Contribuicao

Contribuicoes sao bem-vindas.

1. Abra uma issue descrevendo a proposta.
2. Crie uma branch para a alteracao.
3. Envie um pull request com contexto claro da mudanca.

## Observacoes

- Este repositorio ainda nao possui release publicada no GitHub; por isso o link direto do APK passa a funcionar assim que a primeira release for criada.
- O app Android trabalha com base local de dados (SQLite), priorizando uso rapido no dia a dia.
