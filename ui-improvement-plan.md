# Plano de Melhoria Visual — Financeiramente

**Versão:** 1.0  
**Perfis:** QA · Designer UI/UX · Desenvolvedor Front-End  
**Data:** Maio 2026  
**Status:** Planejamento

---

## Diagnóstico Atual — O Que Está Ruim

> Auditoria técnica e de UX realizada sobre o código-fonte existente.

| Tela / Componente | Problema Identificado | Severidade |
|---|---|---|
| `fragment_categorias.xml` | Usa `ExpandableListView` (widget legado) sem separação de receitas/despesas | 🔴 Alta |
| `fragment_categoria_form.xml` | Spinner para selecionar tipo (Despesa/Receita) — componente antiquado | 🔴 Alta |
| `fragment_relatorios.xml` | Zero gráficos — apenas texto e listas brutas, impossível enxergar padrões | 🔴 Alta |
| `fragment_lancamento_form.xml` | `RadioGroup` com `RadioButton` para tipo — M3 tem `SegmentedButton` para isso | 🟡 Média |
| `item_meta.xml` | Usa `ProgressBar` estilo Android vanilla — sem cor, sem arredondamento, sem impacto visual | 🟡 Média |
| `fragment_dashboard.xml` | Card hero sem gradiente, sem identidade visual forte, barra de progresso genérica | 🟡 Média |
| `fragment_metas.xml` | Lista de metas sem hierarquia visual, título colado com `textSize` hardcoded | 🟡 Média |
| `fragment_lancamentos_list.xml` | Sem agrupamento por data, sem empty state visual, sem swipe actions | 🟡 Média |
| `item_lancamento.xml` | Stripe lateral de 4dp é a única diferenciação visual entre receita e despesa | 🟡 Média |
| `item_categoria_raiz.xml` | Item de categoria sem ícone, sem cor, sem hierarquia visual | 🟠 Média |
| `item_saldo_categoria.xml` | Progresso com `indicatorColor` fixo azul — não muda conforme status (verde/amarelo/vermelho) | 🟡 Média |
| Cores globais | Cores hardcoded `#4CAF50` e `#F44336` espalhadas em `fragment_relatorios.xml` — fora do design system | 🟠 Baixa |
| `drawable/` | Apenas 2 drawables. Sem ícones customizados, sem fundos gradiente, sem shapes refinados | 🟠 Baixa |
| Tema geral | Sem suporte a dark mode implementado | 🟠 Baixa |
| Estados vazios | `TextView` com texto simples — sem ilustrações, sem call-to-action visual | 🟠 Baixa |
| FAB global | Usa `@android:drawable/ic_input_add` — ícone genérico do sistema, sem consistência | 🟠 Baixa |

---

## Referências de Mercado

Apps financeiros de referência visual estudados:
- **Nubank** — uso de roxo/branco com contraste alto, tipografia bold, cards sem borda com sombra suave
- **Wise** — cards informativos com ícones coloridos por categoria, progress rings para limites
- **Mobills** — donut charts como elemento central do dashboard, cores categóricas consistentes
- **Spendee** — gradientes vibrantes no header, ilustrações em empty states, onboarding fluído
- **YNAB (You Need A Budget)** — foco na clareza do "quanto posso gastar", hierarquia visual agressiva

### Tendências UI para Finance Apps (2024–2025)
1. **Glassmorphism suave** nos cards hero do dashboard
2. **Donut chart** como visual principal de distribuição de gastos
3. **Color tokens categóricos** — cada categoria tem uma cor própria e consistente em todo o app
4. **Segmented controls** (M3 SegmentedButton) para filtros rápidos
5. **Skeleton loading** ao invés de spinners genéricos
6. **Bottom Sheet** para formulários rápidos — reduz o custo de navegação
7. **Sticky date headers** em listas de transações
8. **Progress rings** (circular) para metas — mais impactante que barras horizontais
9. **Micro-animações** nos números (counter animation ao carregar saldo)
10. **Empty states ilustrados** com CTA claro

---

## Arquitetura de Melhorias — Épicos

---

## ÉPICO 1 — Design System: Fundação Visual

> **Objetivo:** Criar uma base visual consistente que todas as outras melhorias consumam.  
> **Impacto:** Todas as telas beneficiadas.  
> **Prioridade:** 🔴 Crítica — deve ser feito PRIMEIRO.

### 1.1 — Paleta de Cores Expandida

**Arquivo:** `res/values/colors.xml`

Adicionar tokens de cores categóricas (cada categoria do app recebe uma cor própria, usada em ícones, chips e charts):

```xml
<!-- Cores Categóricas — usadas em ícones e gráficos -->
<color name="cat_moradia">#6366F1</color>       <!-- índigo -->
<color name="cat_transporte">#0EA5E9</color>    <!-- azul céu -->
<color name="cat_alimentacao">#F97316</color>   <!-- laranja -->
<color name="cat_saude">#10B981</color>         <!-- verde -->
<color name="cat_estudos">#8B5CF6</color>       <!-- roxo -->
<color name="cat_lazer">#EC4899</color>         <!-- rosa -->
<color name="cat_presentes">#F59E0B</color>     <!-- âmbar -->
<color name="cat_compras">#EF4444</color>       <!-- vermelho -->
<color name="cat_imprevistos">#64748B</color>   <!-- cinza -->
<color name="cat_provisoes">#14B8A6</color>     <!-- teal -->

<!-- Cores de containers para cada categoria -->
<color name="cat_moradia_container">#EEF2FF</color>
<color name="cat_transporte_container">#E0F2FE</color>
<color name="cat_alimentacao_container">#FFF7ED</color>
<color name="cat_saude_container">#ECFDF5</color>
<color name="cat_estudos_container">#F5F3FF</color>
<color name="cat_lazer_container">#FDF2F8</color>
<color name="cat_presentes_container">#FFFBEB</color>
<color name="cat_compras_container">#FEF2F2</color>

<!-- Gradiente do Hero Card -->
<color name="gradient_start">#1E40AF</color>   <!-- azul escuro -->
<color name="gradient_end">#2563EB</color>     <!-- azul primário -->

<!-- Status semânticos -->
<color name="status_ok">#10B981</color>
<color name="status_warning">#F59E0B</color>
<color name="status_danger">#EF4444</color>
<color name="status_ok_container">#D1FAE5</color>
<color name="status_warning_container">#FEF3C7</color>
<color name="status_danger_container">#FEE2E2</color>

<!-- Dark mode surfaces -->
<color name="dark_background">#0F172A</color>
<color name="dark_surface">#1E293B</color>
<color name="dark_surface_variant">#334155</color>
<color name="dark_on_surface">#F1F5F9</color>
```

### 1.2 — Escala Tipográfica Refinada

**Arquivo:** `res/values/type.xml`

Adicionar estilos faltantes e padronizar toda a hierarquia com font families:

```xml
<!-- Hero Display — saldo principal -->
<style name="TextAppearance.Financeiramente.HeroDisplay">
    <item name="android:textSize">48sp</item>
    <item name="android:textStyle">bold</item>
    <item name="android:letterSpacing">-0.02</item>
</style>

<!-- Display — números grandes secundários -->
<!-- Display atual: 36sp → mantém -->

<!-- Label — chips, badges, status pills -->
<style name="TextAppearance.Financeiramente.Label">
    <item name="android:textSize">11sp</item>
    <item name="android:textStyle">bold</item>
    <item name="android:letterSpacing">0.05</item>
    <item name="android:textAllCaps">true</item>
</style>

<!-- Amount — valores monetários em cards/listas -->
<style name="TextAppearance.Financeiramente.Amount">
    <item name="android:textSize">18sp</item>
    <item name="android:textStyle">bold</item>
    <item name="fontFamily">@font/inter_semibold</item>
</style>
```

> **Nota:** Considerar adicionar a fonte **Inter** (Google Fonts) como `fontFamily` padrão — otimizada para números e dashboards financeiros.

### 1.3 — Shape System

**Arquivo:** `res/values/shapes.xml` *(novo)*

```xml
<resources>
    <!-- Pills para chips e badges -->
    <style name="Shape.Financeiramente.Pill">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSize">50%</item>
    </style>

    <!-- Cards padrão -->
    <style name="Shape.Financeiramente.Card">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSize">16dp</item>
    </style>

    <!-- Cards hero grandes -->
    <style name="Shape.Financeiramente.CardLarge">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSize">24dp</item>
    </style>

    <!-- Botões -->
    <style name="Shape.Financeiramente.Button">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSize">12dp</item>
    </style>
</resources>
```

### 1.4 — Drawables Essenciais

Criar os seguintes drawables faltantes:

| Arquivo | Descrição |
|---|---|
| `drawable/gradient_hero_card.xml` | GradientDrawable do card principal do dashboard |
| `drawable/bg_category_icon.xml` | Círculo colorido de fundo para ícones de categoria |
| `drawable/bg_amount_positive.xml` | Pill verde para valores de receita |
| `drawable/bg_amount_negative.xml` | Pill vermelha/rosada para valores de despesa |
| `drawable/ic_empty_state_generic.xml` | Ilustração vetorial para telas vazias |
| `drawable/ic_empty_transactions.xml` | Ilustração para lista de lançamentos vazia |
| `drawable/ic_empty_goals.xml` | Ilustração para metas vazias |
| `drawable/shape_progress_track.xml` | Fundo arredondado para barras de progresso |

### 1.5 — Tema Dark Mode

**Arquivo:** `res/values-night/themes.xml` *(novo)*

Implementar o tema noturno completo aproveitando os tokens `dark_*` criados em 1.1.

---

## ÉPICO 2 — Dashboard: A Primeira Impressão

> **Objetivo:** Transformar o dashboard na tela mais impactante do app — o usuário deve sentir o valor em 2 segundos.  
> **Impacto:** Tela principal do app, alta visibilidade.  
> **Prioridade:** 🔴 Alta.

### 2.1 — Hero Card com Gradiente e Glassmorphism

**Arquivo:** `fragment_dashboard.xml`

**Situação atual:** Card com fundo `azul_primary_container` (azul claro) e tipografia simples.

**Proposta:**
```
┌─────────────────────────────────────┐
│  gradient: #1E40AF → #2563EB        │
│                                     │
│  Maio 2026                    👁    │
│                                     │
│  Saldo Disponível                   │
│  R$ 2.847,50               [48sp]  │
│                                     │
│  ▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░  62% usado   │
│  Gasto: R$ 1.652    Meta: R$ 4.500 │
└─────────────────────────────────────┘
```

**Mudanças técnicas:**
- Background: `GradientDrawable` com ângulo 135° entre `gradient_start` e `gradient_end`
- Saldo: usar `TextAppearance.Financeiramente.HeroDisplay` (48sp bold)
- Barra de progresso: `LinearProgressIndicator` com `trackColor` branco 20% opacidade, `indicatorColor` branco
- Botão olho (👁) para ocultar valores
- `cardCornerRadius` aumentar para 24dp
- Textos sobre o gradiente: brancos

### 2.2 — Cards de Resumo Horizontal Scrollable

Adicionar uma fila horizontal com 4 mini-cards de métricas rápidas logo abaixo do hero:

```
┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐
│ 💸     │  │ 💰     │  │ 🎯     │  │ ⚡     │
│Despesas│  │Receitas│  │Metas   │  │Provisões│
│R$1.652 │  │R$4.500 │  │3 ativas│  │R$320   │
└────────┘  └────────┘  └────────┘  └────────┘
   ← scroll horizontal →
```

**Implementação:** `HorizontalScrollView` + `LinearLayout` com 4 `MaterialCardView` de largura fixa (120dp cada), `cardCornerRadius` 16dp.

### 2.3 — Donut Chart Preview no Dashboard

Adicionar um mini donut chart (150dp×150dp) logo abaixo dos summary cards, mostrando a distribuição de gastos pelas top-5 categorias do mês.

**Biblioteca:** `MPAndroidChart` — `PieChart` com `holeRadius` 60%, sem legenda (apenas as fatias), com tooltip ao tocar.

**Acompanhado de:** Lista das top-5 categorias com dot colorido + nome + percentual, em `RecyclerView` horizontal de 1 coluna.

### 2.4 — Cards de Categoria no Dashboard Redesenhados

**Arquivo:** `item_saldo_categoria.xml`

**Situação atual:** Stripe lateral 4dp + texto + barra.

**Proposta:**
```
┌──────────────────────────────────────────┐
│  [🏠]  Moradia              R$ 320 livre │
│        R$ 880 / R$ 1.200                │
│        ▓▓▓▓▓▓▓▓▓▓▓▓░░░░   73%  ⚠️      │
└──────────────────────────────────────────┘
```

**Mudanças:**
- Ícone circular de 40dp com cor categórica de fundo (`bg_category_icon.xml`)
- Remove a stripe lateral — substitui por ícone
- `LinearProgressIndicator` com cor dinâmica: verde (<60%), amarelo (60-85%), vermelho (>85%)
- Badge de percentual no final da barra
- Ícone de alerta `⚠️` quando >85%
- `cardElevation` 0dp com `strokeWidth` 1dp `cinza_divider`

### 2.5 — FAB Estendido (Extended FAB)

**Arquivo:** `fragment_dashboard.xml`

Substituir o `FloatingActionButton` simples pelo `ExtendedFloatingActionButton` do Material 3:

```xml
<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    android:id="@+id/fab_novo_lancamento"
    android:text="Registrar"
    app:icon="@drawable/ic_add"
    ... />
```

Ao scrollar a lista, o FAB colapsa para apenas o ícone (comportamento padrão do M3).

---

## ÉPICO 3 — Gráficos e Análises (Relatórios)

> **Objetivo:** Transformar a tela de relatórios de uma lista de texto em uma ferramenta de insight visual real.  
> **Impacto:** Feature completamente nova — alto valor percebido.  
> **Prioridade:** 🔴 Alta.

### 3.1 — Adicionar Dependência MPAndroidChart

**Arquivo:** `build.gradle` (raiz do projeto)

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

**Arquivo:** `android/build.gradle`

```groovy
dependencies {
    // ...existentes...
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
}
```

### 3.2 — Filtros de Período com ChipGroup (M3)

**Situação atual:** Três `Button` com `OutlinedButton` style lado a lado.

**Proposta:** Substituir por `ChipGroup` com `singleSelection="true"` e chips de filtro no estilo M3 filter chips:

```xml
<com.google.android.material.chip.ChipGroup
    app:singleSelection="true"
    app:selectionRequired="true">
    <com.google.android.material.chip.Chip style="@style/Widget.Material3.Chip.Filter" android:text="Semana" />
    <com.google.android.material.chip.Chip style="@style/Widget.Material3.Chip.Filter" android:text="Mês" />
    <com.google.android.material.chip.Chip style="@style/Widget.Material3.Chip.Filter" android:text="Ano" />
    <com.google.android.material.chip.Chip style="@style/Widget.Material3.Chip.Filter" android:text="Personalizado" />
</com.google.android.material.chip.ChipGroup>
```

### 3.3 — Gráfico de Pizza (Distribuição por Categoria)

**Layout:** Card com título "Distribuição de Gastos" + `PieChart` de 260dp de altura.

```
┌─────────────────────────────────────┐
│  Distribuição de Gastos — Maio       │
│                                     │
│         ╭──────╮                    │
│       ╭─╯ 34% ╰─╮                  │
│      ─╯ Alimentação ╰─              │
│     ╯  R$ 1.240     ╰               │
│      ╲  Centro  ╱                   │
│       ╰────────╯                    │
│                                     │
│  ● Alimentação  34%   R$ 1.240      │
│  ● Moradia      28%   R$ 1.020      │
│  ● Transporte   18%   R$   655      │
│  ● Lazer        12%   R$   437      │
│  ● Outros        8%   R$   291      │
└─────────────────────────────────────┘
```

**Configurações do `PieChart`:**
- `holeRadius`: 55%
- Animação: `animateY(800ms, Easing.EaseInOutQuad)`
- Cores das fatias: tokens `cat_*` do Design System
- Sem legenda nativa do chart — usar `RecyclerView` customizado abaixo com dots coloridos
- Ao tocar em uma fatia: exibir nome da categoria + valor + % no centro do donut

### 3.4 — Gráfico de Barras (Evolução Mensal — Receita vs Despesa)

**Layout:** Card com título "Evolução dos Últimos 6 Meses" + `BarChart` de 200dp.

```
R$ ▲
4k │    ████
3k │████████████
2k │████████████████
1k │████████████████████
   └────────────────────→
   Dez Jan Fev Mar Abr Mai
   ■ Receita  ■ Despesa
```

**Configurações do `BarChart`:**
- Barras agrupadas: Receita (verde `#10B981`) e Despesa (vermelho `#EF4444`)
- `xAxis.position`: BOTTOM
- `yAxis.axisMinimum`: 0
- Animação: `animateXY(600ms)`
- Grid lines suaves (cinza 20% opacidade)
- Labels formatados: "R$ 3k" ao invés de "3000"

### 3.5 — Gráfico de Linha (Evolução do Saldo)

**Layout:** Card com título "Evolução do Saldo Disponível" + `LineChart` de 160dp.

- Linha suave (`cubicIntensity` 0.2) com gradiente de preenchimento abaixo
- Cor da linha: `azul_primary`
- Fill gradient: `azul_primary_container` → transparente
- Pontos nos dados: círculos brancos com borda azul
- Se saldo entrar em negativo: linha muda para `vermelho_error`

### 3.6 — Toggle de Tipo de Gráfico (Pizza ↔ Barras Horizontais)

Adicionar `SegmentedButton` acima do gráfico de distribuição para alternar entre:
- **Pizza** — melhor para ver proporções
- **Barras Hor.** — melhor para comparar valores absolutos entre categorias

### 3.7 — Redesign dos Filtros Avançados

Mover os filtros avançados (categoria, tag, tipo) para um **Bottom Sheet** de filtros, acessível por um botão "Filtrar" na AppBar. Isso limpa a tela principal dos relatórios.

---

## ÉPICO 4 — Categorias: Separação e Personalização

> **Objetivo:** Resolver o problema crítico de receitas e despesas misturadas, e elevar o nível visual das categorias.  
> **Impacto:** Usabilidade, clareza mental do usuário.  
> **Prioridade:** 🔴 Alta.

### 4.1 — Abas Despesa / Receita na Tela de Categorias

**Arquivo:** `fragment_categorias.xml`

**Situação atual:** `ExpandableListView` listando todas as categorias juntas.

**Proposta:**

```
┌─────────────────────────────────────┐
│  Categorias                    [+]  │
│  ─────────────────────────────────  │
│  [ Despesas ]     [ Receitas ]      │  ← TabLayout M3
│  ─────────────────────────────────  │
│                                     │
│  ▼ Gastos Essenciais                │
│     🏠 Moradia          R$1.200/mês │
│     🚗 Transporte       R$ 400/mês  │
│     🍔 Alimentação      R$ 800/mês  │
│                                     │
│  ▼ Gastos Não Essenciais            │
│     🎮 Lazer            R$ 300/mês  │
│     🛍️ Compras          sem limite  │
│                                     │
└─────────────────────────────────────┘
```

**Implementação:**
- Substituir `ExpandableListView` por `TabLayout` + `ViewPager2` com dois fragments:
  - `CategoriasDeDespecasFragment`
  - `CategoriasDeReceitasFragment`
- Cada aba contém um `RecyclerView` com `ExpandableAdapter` customizado (Material Design) em vez do `ExpandableListView` legado
- `TabLayout` no estilo M3 Primary Tabs

### 4.2 — Item de Categoria com Ícone Colorido

**Arquivo:** `item_categoria_raiz.xml` (redesign)

**Situação atual:** Apenas nome + tipo em texto.

**Proposta:**
```
┌──────────────────────────────────────────┐
│  [🏠]  Moradia                           │
│        3 subcategorias  •  R$1.200/mês  ▼│
└──────────────────────────────────────────┘
```

- Ícone selecionável pelo usuário (ver 4.3)
- Cor de fundo do ícone baseada na cor da categoria
- Contagem de subcategorias como subtítulo
- Limite mensal visível no item

### 4.3 — Seletor de Ícone e Cor na Criação de Categoria

**Arquivo:** `fragment_categoria_form.xml` (redesign)

**Proposta:**

```
┌─────────────────────────────────────┐
│  Nova Categoria                     │
│  ───────────────────────────────── │
│  [ Despesa ]  [ Receita ]  ← Tabs  │
│                                     │
│  Nome da categoria                  │
│  [_________________________]        │
│                                     │
│  Ícone                              │
│  [🏠][🚗][🍔][❤️][📚][🎮][🛍️][✈️]  │
│  [💰][💳][🏋️][🎵][🍕][⚡][🐕][➕]  │
│                                     │
│  Cor                                │
│  [🔵][🟢][🔴][🟠][🟣][🩵][🟡][⚫]  │
│                                     │
│  Limite Mensal (opcional)           │
│  [_________________________]        │
│                                     │
│  [        Salvar Categoria        ] │
└─────────────────────────────────────┘
```

**Mudanças técnicas:**
- Substituir `Spinner` de tipo por `TabLayout` com 2 abas ("Despesa" / "Receita")
- Grid de ícones: `RecyclerView` com `GridLayoutManager(4 colunas)` — ícones Unicode ou Material Icons
- Grid de cores: `ChipGroup` com circles coloridos (similar ao seletor de cor do Google Keep)
- Preview visual do item de categoria em tempo real (card de preview que atualiza conforme o usuário seleciona)

### 4.4 — Migração de ExpandableListView para RecyclerView

Substituir o `ExpandableListView` legado por um `RecyclerView` com múltiplos `viewType`:
- `VIEW_TYPE_HEADER` — seção de grupo (pai)
- `VIEW_TYPE_CHILD` — subcategoria (filho)

Com animação de expand/collapse usando `RecyclerView.ItemAnimator`.

---

## ÉPICO 5 — Formulário de Lançamento: Velocidade < 10s

> **Objetivo:** O registro deve ser completado em menos de 10 segundos (requisito do PRD).  
> **Impacto:** Funcionalidade core do app — diretamente ligada à retenção.  
> **Prioridade:** 🟡 Alta.

### 5.1 — SegmentedButton para Tipo (Despesa/Receita)

**Arquivo:** `fragment_lancamento_form.xml`

**Situação atual:** `RadioGroup` + `RadioButton` (visual anos 2015).

**Proposta:**
```xml
<com.google.android.material.button.MaterialButtonToggleGroup
    android:id="@+id/toggle_tipo"
    app:singleSelection="true"
    app:selectionRequired="true">

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_despesa"
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:text="💸 Despesa" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_receita"
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:text="💰 Receita" />
</com.google.android.material.button.MaterialButtonToggleGroup>
```

Quando selecionado "Despesa": botão fica com fundo `vermelho_error_container`, texto `vermelho_error`.  
Quando selecionado "Receita": botão fica com fundo `verde_success_container`, texto `verde_success`.

### 5.2 — Campo de Valor com Teclado Numérico Integrado

Adicionar um numpad customizado (Material Design) que aparece ao focar no campo de valor, no lugar do teclado do sistema. Isso reduz o tempo de entrada por:
- Ter o prefixo "R$" fixo e visível
- Ter o separador decimal como botão dedicado
- Display do valor formatado em tempo real ("R$ 1.200,00")

### 5.3 — Seletor de Categoria Visual (Grid de Chips)

**Situação atual:** `Spinner` dropdown — o usuário precisa lembrar os nomes.

**Proposta:** Grade de chips coloridos com ícone + nome, filtrada automaticamente pelo tipo (Despesa/Receita) selecionado:

```
Categoria
[🏠 Moradia] [🚗 Transp.] [🍔 Aliment.]
[❤️ Saúde]  [📚 Estudos] [🎮 Lazer]
[🛍️ Compras] [➕ Mais...]
```

Chips usam a cor da categoria como fundo (em versão clara) com ícone. Quando selecionado, o chip fica com cor sólida.

### 5.4 — Bottom Sheet para Formulário Rápido

Permitir acionar o lançamento rápido via Bottom Sheet que sobe da home, sem navegar para uma nova tela. O Bottom Sheet tem 2 modos:
- **Modo Rápido** (50% da tela): Apenas valor + categoria + descrição curta
- **Modo Completo** (90% da tela): Todos os campos, expandido ao arrastar para cima

### 5.5 — Tags com Autocomplete e Sugestões

**Situação atual:** `ChipGroup` estático.

**Proposta:**
- Campo de texto com `TextInputLayout` + chips se adicionando automaticamente
- Dropdown de sugestões com base nas tags já usadas (histórico)
- Chips removíveis com X interno

---

## ÉPICO 6 — Tela de Metas: Impacto Visual e Motivação

> **Objetivo:** Fazer a tela de metas parecer um painel de conquistas, não uma planilha.  
> **Impacto:** Engajamento e motivação do usuário.  
> **Prioridade:** 🟡 Média.

### 6.1 — Card de Meta com Progress Ring (Circular)

**Arquivo:** `item_meta.xml` (redesign completo)

**Situação atual:** `ProgressBar` horizontal + TextViews.

**Proposta:**
```
┌─────────────────────────────────────────────┐
│                                             │
│   ╭──────────╮   Reserva de Emergência      │
│   │  ╔═══╗  │   R$ 8.400 / R$ 18.000       │
│   │  ║47%║  │                               │
│   │  ╚═══╝  │   Meta: Jun/2027              │
│   ╰──────────╯   📈 +R$500/mês no ritmo     │
│                                             │
│   [   Registrar Aporte   ]                  │
│                                             │
└─────────────────────────────────────────────┘
```

**Implementação:**
- `PieChart` (donut) 80dp×80dp com apenas 1 dataset — criar componente de `CircularProgressView` customizado usando `Canvas` drawing, ou usar `PieChart` do MPAndroidChart com `holeRadius`=70% e sem label
- Percentual no centro do ring
- Cor do ring baseada no progresso: verde (<100%), azul (em dia), vermelho (atrasado)
- Projeção de conclusão calculada no `ViewModel`
- Botão "Registrar Aporte" inline no card

### 6.2 — Cabeçalho de Metas com Resumo

No topo da tela de metas, adicionar um card de resumo:

```
┌─────────────────────────────────────┐
│  Suas Metas                         │
│  5 ativas  •  Total: R$ 48.000      │
│  Progresso médio: 34%               │
│  ▓▓▓▓▓▓▓░░░░░░░░░░░░                │
└─────────────────────────────────────┘
```

### 6.3 — Ordenação e Filtro de Metas

`ChipGroup` com filtros:
- `[ Todas ]` `[ Em andamento ]` `[ Próximas da meta ]` `[ Concluídas ]`

---

## ÉPICO 7 — Lista de Lançamentos: Clareza e Velocidade

> **Objetivo:** Tornar a lista de lançamentos escaneável e acionável em segundos.  
> **Impacto:** Tela de uso frequente.  
> **Prioridade:** 🟡 Média.

### 7.1 — Agrupamento por Data com Sticky Headers

**Situação atual:** Lista plana sem agrupamento, difícil de localizar gastos.

**Proposta:**
```
HOJE — Sábado, 20 Mai          Saldo: -R$ 347
  [🍕] Pizzaria do Bairro     Alimentação  -R$ 89
  [🚌] Uber                   Transporte   -R$ 18

ONTEM — Sexta, 19 Mai
  [🛒] Mercado Extra          Alimentação  -R$ 234
  [⚡] Conta de Luz           Moradia      -R$ 127
```

**Implementação:** `ConcatAdapter` ou `RecyclerView` com múltiplos `viewType`:
- `VIEW_TYPE_DATE_HEADER` — faixa de data com saldo do dia
- `VIEW_TYPE_TRANSACTION` — item de lançamento

Sticky headers usando `ItemDecoration` customizado.

### 7.2 — Item de Lançamento Redesenhado

**Arquivo:** `item_lancamento.xml`

**Situação atual:** Stripe lateral 4dp como único diferenciador visual.

**Proposta:**
```
┌──────────────────────────────────────────────┐
│  [🍕]  Pizzaria do Bairro                    │
│        Alimentação • 19:30     -R$ 89,00     │
│        [com Sii] [lazer]                     │
└──────────────────────────────────────────────┘
```

- Ícone circular de 44dp com cor da categoria como fundo
- Remove a stripe lateral
- Tags visíveis como mini chips abaixo da descrição
- Valor: verde se receita, vermelho se despesa
- Timestamp no subtítulo

### 7.3 — Swipe Actions (Deslizar para Editar/Excluir)

Implementar `ItemTouchHelper.SimpleCallback` para:
- **Swipe direita** → Editar (ícone de lápis, fundo azul)
- **Swipe esquerda** → Excluir (ícone de lixeira, fundo vermelho) com confirmação

### 7.4 — Barra de Busca na Lista de Lançamentos

Adicionar `SearchView` ou `SearchBar` (M3) no topo da tela de lançamentos para filtrar por descrição, categoria ou valor.

---

## ÉPICO 8 — Estados Vazios: Engajamento e Orientação

> **Objetivo:** Substituir textos simples por empty states que orientam o usuário à ação.  
> **Impacto:** Primeira experiência do usuário, onboarding.  
> **Prioridade:** 🟠 Média.

### 8.1 — Empty State: Lista de Lançamentos

```
        [Ilustração vetorial: carteira vazia]

        Nenhum lançamento ainda

        Registre seu primeiro gasto ou
        receita do dia para começar.

        [ + Registrar Lançamento ]
```

### 8.2 — Empty State: Metas

```
        [Ilustração vetorial: bandeira/troféu]

        Suas metas aparecem aqui

        Defina objetivos financeiros e
        acompanhe seu progresso.

        [ + Criar Primeira Meta ]
```

### 8.3 — Empty State: Relatórios (sem dados no período)

```
        [Ilustração vetorial: gráfico vazio]

        Sem dados no período selecionado

        Tente ampliar o período ou
        registre alguns lançamentos.
```

### 8.4 — Empty State: Categorias (aba vazia)

```
        [Ilustração vetorial: pasta vazia]

        Nenhuma categoria de receita

        Crie categorias para organizar
        suas fontes de renda.

        [ + Criar Categoria ]
```

---

## ÉPICO 9 — Micro-interações e Animações

> **Objetivo:** Dar ao app uma sensação premium e responsiva.  
> **Impacto:** Percepção de qualidade do produto.  
> **Prioridade:** 🟠 Baixa (implementar por último).

### 9.1 — Animação de Counter no Saldo

Ao carregar o dashboard ou ao voltar para ele, o saldo disponível anima de 0 até o valor real em ~600ms (efeito "contador"). Implementado com `ValueAnimator`.

### 9.2 — Transição de Tela (Shared Element)

Ao tocar em um item de meta → tela de detalhe: o card da meta expande usando `ActivityTransitionManager` (Shared Element Transition).

### 9.3 — Animação de Entrada nas Listas

Itens do `RecyclerView` entram com um fade + translate para cima ao aparecer pela primeira vez. Usar `DefaultItemAnimator` customizado ou `RecyclerView.ItemAnimator`.

### 9.4 — Feedback de Sucesso ao Salvar

Após salvar um lançamento:
1. Bottom sheet fecha com animação suave
2. FAB pulsa brevemente (scale animation)
3. `Snackbar` aparece: "Lançamento salvo" + ação "Desfazer"

### 9.5 — Pull-to-Refresh no Dashboard

Adicionar `SwipeRefreshLayout` no dashboard para recarregar os dados manualmente.

---

## ÉPICO 10 — Dark Mode e Acessibilidade

> **Objetivo:** Suporte completo ao modo noturno e conformidade com WCAG AA.  
> **Impacto:** Usabilidade noturna, inclusividade.  
> **Prioridade:** 🟠 Baixa.

### 10.1 — Dark Theme Completo

Criar `res/values-night/colors.xml` e `res/values-night/themes.xml` com:
- Background: `#0F172A` (slate 900)
- Surface: `#1E293B` (slate 800)
- Cards: `#334155` (slate 700)
- Textos: `#F1F5F9` (slate 100)
- Hero card: gradiente mais escuro `#1E3A8A` → `#1D4ED8`

### 10.2 — Auditoria de Contraste (WCAG AA)

Verificar todos os pares texto/fundo contra a regra:
- Texto normal: ratio mínimo 4.5:1
- Texto grande (>18sp): ratio mínimo 3:1
- Corrigir os lugares onde `cinza_secondary` (`#64748B`) sobre fundo branco tem contraste 4.6:1 (borderline — aumentar para `#475569`)

### 10.3 — Suporte a Escala de Fonte

Verificar que os layouts não quebram com escala de fonte do sistema em 130% e 150%. Converter alturas fixas de elementos de texto para `wrap_content`.

---

## Roadmap de Implementação

```
FASE 1 — Fundação (Semana 1–2)
├── ÉPICO 1 — Design System completo
│   ├── colors.xml expandido
│   ├── type.xml refinado
│   ├── shapes.xml novo
│   └── drawables base

FASE 2 — Telas Core (Semana 3–5)
├── ÉPICO 2 — Dashboard redesenhado
├── ÉPICO 4 — Categorias com abas + ícones
└── ÉPICO 5 — Formulário de lançamento

FASE 3 — Dados e Gráficos (Semana 6–8)
├── ÉPICO 3 — Charts completos nos relatórios
└── ÉPICO 6 — Metas com progress rings

FASE 4 — Refinamento (Semana 9–10)
├── ÉPICO 7 — Lista de lançamentos
├── ÉPICO 8 — Empty states
└── ÉPICO 9 — Micro-interações

FASE 5 — Polimento (Semana 11–12)
└── ÉPICO 10 — Dark mode + acessibilidade
```

---

## Critérios de Aceitação (QA)

### Testes de Usabilidade
- [ ] Tempo de registro de lançamento: < 10 segundos
- [ ] Dashboard carrega em < 1 segundo (cold start)
- [ ] Usuário consegue entender o saldo disponível sem nenhuma leitura adicional

### Testes Visuais
- [ ] Contraste de todos os textos ≥ 4.5:1 (verificar com accessibility scanner)
- [ ] Todos os componentes funcionam em light mode E dark mode
- [ ] Layout não quebra em fonte grande (130% e 150%)
- [ ] Layout não quebra em telas pequenas (360dp de largura) nem grandes (412dp+)

### Testes de Gráficos
- [ ] Gráfico de pizza exibe corretamente com 1, 3, 5 e 10+ categorias
- [ ] Gráfico de barras exibe sem overflow de labels com nomes de meses longos
- [ ] Gráfico de linha funciona com período de 1 mês e 12 meses

### Testes de Categorias
- [ ] Aba "Despesas" exibe apenas categorias do tipo DESPESA
- [ ] Aba "Receitas" exibe apenas categorias do tipo RECEITA
- [ ] Formulário de categoria iniciado do dashboard de Despesas já vem com aba correta selecionada
- [ ] Ícone e cor são salvos e persistidos corretamente

### Testes de Regressão
- [ ] Salvar lançamento ainda funciona após redesign do formulário
- [ ] Saldo calculado no dashboard não se altera após mudanças visuais
- [ ] Todas as rotas de navegação funcionam após mudanças de layout

---

## Dependências Novas

| Biblioteca | Versão | Uso |
|---|---|---|
| `com.github.PhilJay:MPAndroidChart` | `v3.1.0` | Gráficos (Pizza, Barras, Linha) |
| `com.google.android.material:material` | `1.12.0` *(já existe)* | TabLayout, SegmentedButton, Extended FAB |
| *(opcional)* `com.airbnb.android:lottie` | `6.4.0` | Animações em empty states |
| *(opcional)* `com.google.fonts:inter` | via Google Fonts | Tipografia refinada |

---

## Notas do Designer

> **Princípio norteador:** O app deve transmitir **clareza e confiança**. Um app financeiro que parece confuso gera ansiedade. Cada tela deve responder UMA pergunta clara:
>
> - Dashboard → "Quanto posso gastar hoje?"
> - Relatórios → "Para onde foi meu dinheiro?"
> - Metas → "Quanto falta para minha próxima meta?"
> - Categorias → "Estou dentro do meu planejamento?"

> **Princípio de cor:** As cores devem ser semânticas, não decorativas. Verde = positivo = tenho dinheiro. Vermelho = negativo = estourei. Azul = informativo = veja mais. Cada pixel colorido deve significar algo.

> **Princípio de velocidade:** Cada toque a mais custa 1 usuário por semana. Nunca coloque uma ação importante a mais de 2 toques da home.
