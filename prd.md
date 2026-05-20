# PRD — Finanças Pessoais

**Versão:** 1.0  
**Autor:** Kauan  
**Data:** Maio 2026  
**Status:** Aprovado

---

## 1. Visão do Produto

Um assistente financeiro pessoal para uso próprio que elimina a fricção do controle de gastos — transformando o hábito de registrar lançamentos de uma tarefa pesada (planilha de Excel) em algo rápido o suficiente para acontecer no momento real do gasto.

O produto não é apenas um rastreador de gastos: é uma ferramenta de clareza financeira. O objetivo é que o usuário sempre saiba, em segundos, **quanto ainda pode gastar** e **para onde seu dinheiro está indo** — sem depender de memória ou de consultar múltiplos sistemas.

---

## 2. Problema

O controle financeiro por planilha Excel funciona como sistema de registro histórico, mas falha como ferramenta de uso no dia a dia:

- **Alta fricção no registro:** abrir notebook → encontrar aba → acessar app bancário para conferir valores → digitar. Custo alto demais para o momento do gasto.
- **Perda de contexto:** gastos registrados dias depois perdem a descrição ("Pix de R$100 para quem mesmo?"). Dados incompletos = relatórios sem valor.
- **Sem visão em tempo real:** a planilha mostra o passado, não o que ainda pode ser gasto hoje.
- **Gastos anuais invisíveis:** despesas diluídas (ex: seguro bancário ~R$58/mês) não aparecem na planilha mensal, criando surpresas financeiras.

---

## 3. Persona

**Nome:** Kauan  
**Perfil:** 20-30 anos, CLT, renda base fixa + fontes eventuais (freelance, acertos)  
**Comportamento financeiro:** Já tem categorias mentais bem definidas. Planeja o mês antes de gastar. Registra gastos com contexto descritivo. Tem metas financeiras concretas (apartamento, moto, casamento, viagem, reserva de emergência).  
**Dispositivo principal de registro:** Celular (no momento do gasto ou ao fim do dia)  
**Pagamentos predominantes:** Pix; cartão de crédito para parcelamentos e assinaturas recorrentes

**Frustrações atuais:**
- Preguiça/esquecimento de abrir a planilha
- Perda de contexto em gastos registrados tardiamente
- Não consegue ver "quanto posso gastar ainda hoje"
- Gastos anuais não aparecem no controle mensal
- Categoria "Imprevistos" usada como catch-all sem controle real

**O que mais valorizaria no app:**
- Registro em menos de 10 segundos
- Dashboard com saldo disponível imediatamente visível
- Categorias que respeitam seu modelo mental
- Metas com progresso visual

---

## 4. Objetivos do Produto

| Objetivo | Métrica de Sucesso |
|---|---|
| Reduzir fricção do registro | Tempo de lançamento < 10 segundos |
| Eliminar perda de contexto | Campo de descrição obrigatório no lançamento |
| Visibilidade em tempo real | Dashboard mostra "saldo disponível" na abertura |
| Substituir a planilha Excel | Usuário para de usar a planilha em 30 dias |
| Controle de metas | Progresso visível de todas as metas ativas |

---

## 5. Conceitos do Domínio

Antes das funcionalidades, é essencial diferenciar os quatro conceitos financeiros que o app trata de forma distinta:

| Conceito | Definição | Exemplo |
|---|---|---|
| **Gasto** | Saída de dinheiro por algo consumido | Cinema R$30 |
| **Imprevisto** | Reserva para o absolutamente inesperado | Remédio, peça do carro quebrada |
| **Provisão** | Reserva mensal para gasto periódico conhecido | R$55/mês para troca de pneu anual |
| **Meta** | Objetivo financeiro de médio/longo prazo | Juntar R$20k para casamento |

---

## 6. Funcionalidades do MVP

### 6.1 Registro de Lançamentos (Feature A)

Registro manual e rápido de receitas e despesas.

**Campos obrigatórios:** valor, categoria, descrição curta, data (padrão: hoje)  
**Campos opcionais:** tag/contexto (texto livre), subcategoria  
**Tipos:** despesa, receita  
**Requisito de UX:** tela de registro acessível em no máximo 2 toques a partir da home. O registro deve ser completado em menos de 10 segundos.

---

### 6.2 Categorias e Subcategorias (Feature B)

Sistema de categorias totalmente personalizável pelo usuário.

**Categorias padrão sugeridas no onboarding:**

```
Gastos Essenciais
  ├── Moradia
  ├── Transporte
  ├── Alimentação
  ├── Saúde & Bem-estar
  ├── Estudos
  └── Doações

Gastos Não Essenciais
  ├── Lazer
  ├── Presentes
  └── Compras & Luxo

Imprevistos
  └── (livre — reserva para o absolutamente inesperado)

Provisões
  └── (livre — reserva mensal para gastos periódicos conhecidos)
```

Usuário pode criar, renomear, excluir e reorganizar categorias e subcategorias livremente. Cada categoria/subcategoria pode ter um **limite mensal** opcional.

---

### 6.3 Tags de Contexto (Feature C)

Campo opcional em cada lançamento para adicionar contexto livre.

**Exemplos de uso:** "com Sii", "trabalho", "emergência"  
Tags são criadas livremente pelo usuário (lista não fechada).  
Relatórios podem ser filtrados por tag.  
Permite analisar, por exemplo, "quanto gastei em Lazer especificamente com minha namorada este mês" sem duplicar categorias.

---

### 6.4 Planejamento Mensal (Feature D)

Antes de cada mês, o usuário define:
- Receita esperada (salário CLT recorrente + entradas extras eventuais)
- Limite de gasto por categoria
- Valores de provisões mensais
- Valor reservado para imprevistos

**Plano padrão:** O usuário pode salvar um planejamento como "padrão". Ao iniciar um novo mês, o plano padrão é aplicado automaticamente como ponto de partida, sem necessidade de recriar do zero. O usuário pode ajustar o que precisar antes de confirmar.

O sistema calcula automaticamente o **saldo disponível planejado**:

```
Receita esperada
  − Soma de limites por categoria
  − Provisões do mês
  − Reserva de imprevistos
= Saldo disponível para gastar
```

---

### 6.5 Dashboard Principal (Feature E)

Tela inicial do app. Informações visíveis sem nenhuma navegação:

1. **Saldo disponível para gastar** (número principal, destaque visual)
2. **Gasto realizado vs. planejado** (barra de progresso global do mês)
3. **Saldo por categoria** — lista mostrando quanto resta em cada categoria com indicador visual (verde / amarelo / vermelho)
4. **Alertas** — categorias próximas ou acima do limite

Cálculo do saldo disponível em tempo real:

```
Receita realizada
  − Total gasto (lançamentos confirmados)
  − Provisões alocadas
  − Reserva de imprevistos
= Saldo disponível atual
```

---

### 6.6 Lançamentos Recorrentes (Feature F)

Lançamentos que se repetem automaticamente todo mês (ou em período configurado).

**Exemplos:** Academia (R$X/mês), YouTube Music, salário CLT.  
O usuário configura uma vez. O sistema cria o lançamento automaticamente no período definido. Lançamentos recorrentes de despesa aparecem como "pendentes de confirmação" — o usuário confirma ou ajusta o valor real quando ocorre.

---

### 6.7 Provisões Mensais (Feature G)

Permite alocar mensalmente uma fração de gastos que ocorrem anualmente ou esporadicamente — mas que são **previsíveis e esperados**.

**Exemplo:** Troca de pneu da moto R$660/ano → provisão de R$55/mês.  
**Exemplo:** Seguro bancário R$700/ano → provisão de R$58,33/mês.

Quando o gasto real ocorre, o usuário registra um lançamento vinculado à provisão. O sistema desconta do saldo acumulado da provisão. Isso torna visível mensalmente um gasto que antes aparecia como "surpresa".

---

### 6.8 Metas Financeiras (Feature H)

Registro e acompanhamento de objetivos financeiros de médio/longo prazo.

**Campos por meta:** nome, valor total objetivo, valor atual acumulado, data alvo (opcional), descrição.

**Metas iniciais do usuário:**
- Reserva de emergência (objetivo: 6 meses de gastos mensais)
- Casamento
- Viagem de fim de ano
- Quitar moto financiada
- Quitar apartamento financiado

O usuário registra aportes nas metas manualmente. O dashboard de metas exibe:
- Barra de progresso por meta
- Percentual concluído
- Projeção de data de conclusão no ritmo atual de aportes

---

### 6.9 Relatórios (Feature I)

Visualizações dos dados financeiros por período.

**Filtros:** período (semana, mês, intervalo customizado), categoria, subcategoria, tag.

**Tipos de gráfico:** usuário escolhe entre pizza (distribuição por categoria) ou barras (evolução temporal).

**Visões disponíveis:**
- Gastos por categoria no período
- Receita vs. despesa mês a mês
- Evolução do saldo disponível
- Histórico de lançamentos (lista com filtros e busca)
- Progresso de todas as metas

---

## 7. Fora do MVP — Pós-MVP Obrigatório

| Feature | Versão | Descrição |
|---|---|---|
| **J — Financiamentos** | v2 | Módulo para registrar dívidas (apartamento, moto) com saldo devedor, taxa de juros e parcelas restantes |
| **K — Cartão de Crédito** | v2 | Modo lazy (lança fatura total) + modo detalhado (quebra fatura por categoria sem double-counting) |
| **L — Investimentos** | v2 | Registro de aportes por ativo/corretora, acompanhamento de rendimento ao longo do tempo |
| **M — Milhas** | v2 | Sub-módulo dentro de Investimentos para controle de compra/venda/lucro em arbitragem de milhas |
| **N — Decisor Financeiro** | v3 | Análise comparativa: vale quitar dívida X ou investir? (juros da dívida vs. rendimento esperado) |

---

## 8. Requisitos Não Funcionais

- **Mobile-first:** app mobile como plataforma principal de registro
- **Performance:** tela de registro carrega em < 1 segundo
- **Offline-first (desejável no MVP):** registro de lançamentos deve funcionar sem internet
- **Segurança:** dados armazenados localmente, sem autenticação (uso pessoal)

---

## 9. Fora de Escopo (Projeto Inteiro)

- Integração com Open Finance / sincronização bancária automática
- Contas compartilhadas / multi-usuário colaborativo
- Declaração de imposto de renda
- Gestão financeira empresarial

---

## 10. Histórico de Versões

| Versão | Data | Descrição |
|---|---|---|
| 0.1 | Mai 2026 | Draft inicial — discovery completo |
| 1.0 | Mai 2026 | Aprovado pelo autor com ajustes finais |
