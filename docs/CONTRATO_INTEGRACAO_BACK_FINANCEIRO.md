# Contrato de integração — Back financeiro → API-FINANCE-IA

Este documento define o que o **back principal / microsserviços financeiros** devem expor para a `API-FINANCE-IA` deixar de usar métricas mockadas e passar a gerar insights com **dados reais**.

A `API-FINANCE-IA` consome apenas **HTTP (JSON)**. Ela **não** deve acessar o banco transacional de outros sistemas diretamente.

---

## 1. Visão geral

| Origem | Responsabilidade |
|--------|------------------|
| **Microsserviço de transações** | Receitas, despesas, categorias, ticket médio, totais por período |
| **Microsserviço de cobranças** | Inadimplência, a vencer, vencido, recebido |
| **Microsserviço de clientes** | Ranking de clientes por receita no período |

Cada serviço pode rodar na **mesma base URL** (API Gateway) ou em URLs distintas — configurável em `application.yml` (`services.*.url`).

---

## 2. Autenticação (recomendado)

Para ambiente interno / faculdade, pode começar **sem auth** em rede local.

Para produção, recomenda-se uma das opções:

- **Header** `Authorization: Bearer <JWT>` emitido pelo auth do escritório, ou
- **Header interno** `X-Internal-Token: <segredo>` entre microsserviços.

A `API-FINANCE-IA` repassa o mesmo token do usuário quando o mobile já enviar JWT (evolução futura). Por ora, contratos abaixo assumem **somente leitura** e `tenantId` explícito na URL.

---

## 3. Convenções

- **Base path sugerido**: `/internal/v1` (rotas só para consumo interno, não expostas ao app público sem gateway).
- **Datas**: `yyyy-MM-dd` (ISO-8601 date).
- **Moeda**: valores em **número decimal** (ex.: `85400.50`), moeda implícita BRL salvo acordo futuro.
- **Tenant**: `tenantId` = identificador do escritório / conta (Long).

---

## 4. Transações — resumo agregado

### `GET /internal/v1/tenants/{tenantId}/transacoes/resumo`

**Query**

| Parâmetro | Obrigatório | Descrição |
|-----------|-------------|-----------|
| `dataInicio` | sim | Início do período |
| `dataFim` | sim | Fim do período |

**Resposta `200` — JSON**

```json
{
  "tenantId": 1,
  "dataInicio": "2026-01-01",
  "dataFim": "2026-03-31",
  "receitaTotal": 85400.0,
  "despesaTotal": 42900.0,
  "ticketMedio": 8540.0,
  "quantidadeTransacoes": 120,
  "receitaPorCategoria": { "HONORARIOS": 65000, "CONSULTORIA": 15000 },
  "despesaPorCategoria": { "ALUGUEL": 15000, "CUSTAS": 8000 }
}
```

**Erros**

- `400` — período inválido
- `404` — tenant inexistente
- `503` — indisponível

---

## 5. Cobranças — resumo (inadimplência e recebíveis)

### `GET /internal/v1/tenants/{tenantId}/cobrancas/resumo`

**Query**

| Parâmetro | Obrigatório | Descrição |
|-----------|-------------|-----------|
| `dataInicio` | sim | Início do período |
| `dataFim` | sim | Fim do período |

**Resposta `200` — JSON**

```json
{
  "tenantId": 1,
  "dataInicio": "2026-01-01",
  "dataFim": "2026-03-31",
  "inadimplenciaPercentual": 12.0,
  "valorRecebido": 50000.0,
  "valorVencido": 8000.0,
  "valorAVencer": 12000.0,
  "quantidadeTitulosAbertos": 15
}
```

Definição sugerida de **inadimplência** (alinhar com o back): valor ou títulos vencidos não pagos / valor total de títulos no período — documentar a fórmula no próprio serviço de cobranças.

---

## 6. Clientes — ranking por receita

### `GET /internal/v1/tenants/{tenantId}/clientes/ranking-receita`

**Query**

| Parâmetro | Obrigatório | Descrição |
|-----------|-------------|-----------|
| `dataInicio` | sim | Início do período |
| `dataFim` | sim | Fim do período |
| `limite` | não | Padrão `10`, máximo sugerido `50` |

**Resposta `200` — JSON**

```json
{
  "tenantId": 1,
  "dataInicio": "2026-01-01",
  "dataFim": "2026-03-31",
  "itens": [
    { "clienteId": 101, "nome": "João Silva", "valorRecebido": 25000.0 },
    { "clienteId": 102, "nome": "Maria Santos", "valorRecebido": 18000.0 }
  ]
}
```

---

## 7. KPIs consolidados (opcional — um único endpoint)

Se preferirem **um** endpoint no gateway que já agrega tudo:

### `GET /internal/v1/tenants/{tenantId}/financeiro/kpis`

Mesmas queries `dataInicio`, `dataFim`. Corpo: merge dos três contratos acima.

Nesse caso, a `API-FINANCE-IA` pode trocar os 3 Feign por **1** client `FinanceiroAgregadoClient` (evolução futura).

---

## 8. O que a API-FINANCE-IA faz com esses dados

1. Monta o **mapa `metricasBase`** (objeto serializado no prompt).
2. Chama o **Gemini** com instruções para **não inventar números** fora desse mapa.
3. Persiste o insight em `insight_financeiro`.

---

## 9. Configuração na API-FINANCE-IA

Em `application.yml`:

```yaml
services:
  transacoes:
    url: http://localhost:8080
  cobrancas:
    url: http://localhost:8080
  clientes:
    url: http://localhost:8080
features:
  usar-metricas-mock: true   # false quando o back estiver pronto
```

Com `usar-metricas-mock: false`, a API tenta os Feign; se algum falhar, pode-se definir política de fallback (hoje: manter mock só em desenvolvimento, se desejado).

---

## 10. Checklist para o time do back

- [ ] Expor os 3 GETs (ou o KPI agregado) com os campos acima
- [ ] Validar `dataInicio <= dataFim`
- [ ] Documentar no Swagger do serviço de origem
- [ ] Garantir CORS apenas no gateway do app, não necessariamente nesses endpoints internos
- [ ] Alinhar fórmula de inadimplência e categorias (enum ou string livre)
