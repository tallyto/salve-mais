# Sistema de Contas Fixas Recorrentes

## 📋 Visão Geral

A funcionalidade de **Contas Fixas Recorrentes** permite criar múltiplas contas fixas automaticamente com base em um padrão de recorrência. Ideal para despesas como aluguel, financiamentos, assinaturas, etc.

## 🚀 Funcionalidades

### ✅ **Criação Automática de Parcelas**
- Gera múltiplas contas fixas com base no número de parcelas definido
- Calcula automaticamente as datas de vencimento
- Numera as parcelas automaticamente (ex: "Aluguel (1/12)", "Aluguel (2/12)")

### ✅ **Tipos de Recorrência**
- **MENSAL**: A cada 1 mês
- **BIMENSAL**: A cada 2 meses  
- **TRIMESTRAL**: A cada 3 meses
- **SEMESTRAL**: A cada 6 meses
- **ANUAL**: A cada 12 meses

## 🔧 Como Usar

### **Endpoint da API**
```http
POST /api/contas/fixas/recorrente
Content-Type: application/json
```

### **Exemplo de Payload**
```json
{
  "nome": "Aluguel Apartamento",
  "categoriaId": 1,
  "contaId": 2,
  "dataInicio": "2025-01-01",
  "valor": 1200.00,
  "numeroParcelas": 12,
  "tipoRecorrencia": "MENSAL",
  "observacoes": "Aluguel do apartamento centro"
}
```

### **Resultado**
O sistema criará automaticamente 12 contas fixas:
- **Aluguel Apartamento (1/12)** - Vencimento: 01/01/2025
- **Aluguel Apartamento (2/12)** - Vencimento: 01/02/2025
- **Aluguel Apartamento (3/12)** - Vencimento: 01/03/2025
- ... (e assim por diante)

## 📝 Validações

### **Campos Obrigatórios:**
- ✅ **nome**: Nome da conta fixa (não pode estar em branco)
- ✅ **categoriaId**: ID da categoria (deve existir no banco)
- ✅ **contaId**: ID da conta (deve existir no banco)
- ✅ **dataInicio**: Data de início da primeira parcela
- ✅ **valor**: Valor de cada parcela (deve ser positivo)
- ✅ **numeroParcelas**: Quantidade de parcelas (mínimo 1)
- ✅ **tipoRecorrencia**: Tipo de recorrência (MENSAL, BIMENSAL, etc.)

### **Campos Opcionais:**
- **observacoes**: Informações adicionais sobre a conta

## 🎯 Casos de Uso

### **1. Aluguel Mensal (12 meses)**
```json
{
  "nome": "Aluguel Casa",
  "categoriaId": 1,
  "contaId": 2,
  "dataInicio": "2025-01-01",
  "valor": 1200.00,
  "numeroParcelas": 12,
  "tipoRecorrencia": "MENSAL"
}
```

### **2. Financiamento de Carro (48 parcelas mensais)**
```json
{
  "nome": "Financiamento Civic",
  "categoriaId": 3,
  "contaId": 2,
  "dataInicio": "2025-01-15",
  "valor": 890.50,
  "numeroParcelas": 48,
  "tipoRecorrencia": "MENSAL"
}
```

### **3. Seguro Anual (5 anos)**
```json
{
  "nome": "Seguro Residencial",
  "categoriaId": 4,
  "contaId": 1,
  "dataInicio": "2025-03-01",
  "valor": 1500.00,
  "numeroParcelas": 5,
  "tipoRecorrencia": "ANUAL"
}
```

### **4. Condomínio Bimensal (24 parcelas)**
```json
{
  "nome": "Taxa Condomínio",
  "categoriaId": 2,
  "contaId": 2,
  "dataInicio": "2025-01-01",
  "valor": 350.00,
  "numeroParcelas": 24,
  "tipoRecorrencia": "BIMENSAL"
}
```

## 🔄 Integração com Sistema Existente

### **Endpoints Relacionados:**
- `GET /api/contas/fixas` - Listar todas as contas fixas (incluindo as recorrentes)
- `GET /api/contas/fixas/vencidas` - Contas vencidas (incluindo parcelas em atraso)
- `PUT /api/contas/fixas/{id}` - Editar conta específica
- `DELETE /api/contas/fixas/{id}` - Excluir conta específica

### **Características das Contas Criadas:**
- ✅ Todas as parcelas são criadas como **não pagas** (pago = false)
- ✅ Cada parcela é uma conta fixa independente
- ✅ Podem ser editadas/excluídas individualmente
- ✅ Aparecem nos relatórios de contas vencidas
- ✅ Integram com o cálculo de totais

## ⚠️ Considerações Importantes

### **Gerenciamento de Parcelas:**
- Cada parcela é uma conta fixa **independente**
- Se precisar alterar todas as parcelas, deve ser feito individualmente
- Exclusão de uma parcela não afeta as demais

### **Datas de Vencimento:**
- Calculadas automaticamente baseadas no tipo de recorrência
- Respeita o calendário (ex: 31/01 → 28/02 em ano não bissexto)

### **Performance:**
- Para muitas parcelas (ex: 60+ parcelas), considere processar em lotes
- Operação é transacional (ou todas são criadas ou nenhuma)

## 🎉 Benefícios

1. **⏱️ Economia de Tempo**: Cria múltiplas contas de uma só vez
2. **🎯 Precisão**: Elimina erros de digitação em parcelas repetidas
3. **📅 Organização**: Datas calculadas automaticamente
4. **🔄 Flexibilidade**: Suporta diferentes tipos de recorrência
5. **📊 Controle**: Cada parcela pode ser gerenciada individualmente
