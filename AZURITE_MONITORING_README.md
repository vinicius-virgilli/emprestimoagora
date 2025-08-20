# Monitoramento do Azurite - EventHub Messages

Este guia explica como usar a coleção Postman para acessar as APIs do Azurite e visualizar as mensagens que chegam via EventHub.

## 📋 Pré-requisitos

1. **Docker Compose rodando** com os serviços:
   ```bash
   docker-compose --profile dev --profile eventhub up -d
   ```

2. **Postman instalado** ou use o Postman Web

3. **Portas disponíveis**:
   - `10000` - Blob Storage (onde ficam as mensagens)
   - `10001` - Queue Storage
   - `10002` - Table Storage

## 🚀 Como Importar a Coleção

1. Abra o Postman
2. Clique em **Import**
3. Selecione o arquivo `Azurite_Storage_Postman_Collection.json`
4. A coleção será importada com todas as variáveis configuradas

## 🔧 Variáveis Configuradas

A coleção já vem com as seguintes variáveis pré-configuradas:

| Variável | Valor | Descrição |
|----------|-------|----------|
| `azurite_host` | `localhost` | Host do Azurite |
| `blob_port` | `10000` | Porta do Blob Storage |
| `queue_port` | `10001` | Porta do Queue Storage |
| `table_port` | `10002` | Porta do Table Storage |
| `account_name` | `devstoreaccount1` | Conta padrão do Azurite |
| `account_key` | `Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==` | Chave padrão do Azurite |

## 📁 Estrutura da Coleção

### 1. **Blob Storage** 📦
Onde ficam armazenadas as mensagens do EventHub:

- **List Containers**: Lista todos os containers
- **List Blobs in Container**: Lista blobs no container `eventhub-data`
- **Download Blob (Message)**: Baixa o conteúdo de uma mensagem específica

### 2. **Queue Storage** 📬
Para verificar filas de mensagens:

- **List Queues**: Lista todas as filas
- **Get Queue Messages**: Obtém mensagens de uma fila específica

### 3. **Table Storage** 📊
Para metadados e informações estruturadas:

- **List Tables**: Lista todas as tabelas
- **Query Table Entities**: Consulta entidades de uma tabela

### 4. **EventHub Monitoring** 🔍
Endpoints específicos para monitorar mensagens:

- **Check EventHub Container**: Verifica container `eventhub`
- **Check Simulacao Container**: Verifica container `simulacao`

## 🎯 Como Monitorar Mensagens do EventHub

### Passo 1: Verificar Containers Disponíveis
```http
GET http://localhost:10000/devstoreaccount1?comp=list
```

### Passo 2: Listar Mensagens no Container EventHub
```http
GET http://localhost:10000/devstoreaccount1/eventhub-data?restype=container&comp=list
```

### Passo 3: Baixar Conteúdo de uma Mensagem
```http
GET http://localhost:10000/devstoreaccount1/eventhub-data/{nome-do-blob}
```

## 🧪 Testando o Fluxo Completo

1. **Execute uma simulação** via API:
   ```bash
   curl -X POST http://localhost:8080/api/simulacao/processar \
     -H "Content-Type: application/json" \
     -d '{"valorDesejado": 10000, "prazo": 12}'
   ```

2. **Liste os containers** para ver onde a mensagem foi armazenada:
   - Use: `Blob Storage > List Containers`

3. **Verifique o container específico**:
   - Use: `EventHub Monitoring > Check EventHub Container`
   - Ou: `EventHub Monitoring > Check Simulacao Container`

4. **Baixe a mensagem**:
   - Use: `Blob Storage > Download Blob (Message)`
   - Substitua `{{blob_name}}` pelo nome encontrado na listagem

## 🔍 Containers Esperados

Após executar simulações, você deve encontrar containers como:

- `eventhub` - Mensagens gerais do EventHub
- `simulacao` - Mensagens específicas de simulação
- `eventhub-data` - Dados do emulador EventHub

## 📝 Formato das Mensagens

As mensagens do EventHub geralmente são armazenadas em formato JSON:

```json
{
  "idSimulacao": "1234567890123",
  "codigoProduto": 1,
  "valorDesejado": 10000,
  "prazo": 12,
  "taxaJuros": 0.017900000,
  "timestamp": "2024-01-20T10:00:00Z",
  "resultados": {
    "sac": [...],
    "price": [...]
  }
}
```

## 🚨 Troubleshooting

### Problema: "Connection refused"
**Solução**: Verifique se o Azurite está rodando:
```bash
docker ps | grep azurite
```

### Problema: "Container not found"
**Solução**: Execute algumas simulações primeiro para gerar mensagens:
```bash
curl -X POST http://localhost:8080/api/simulacao/processar \
  -H "Content-Type: application/json" \
  -d '{"valorDesejado": 5000, "prazo": 24}'
```

### Problema: "No blobs found"
**Solução**: 
1. Verifique se o EventHub está configurado corretamente
2. Confirme se as mensagens estão sendo enviadas pela aplicação
3. Verifique os logs do container EventHub:
   ```bash
   docker logs emprestimoagora-eventhub
   ```

## 🔗 URLs Úteis

- **Azurite Blob Storage**: http://localhost:10000
- **Azurite Queue Storage**: http://localhost:10001
- **Azurite Table Storage**: http://localhost:10002
- **Kafdrop (EventHub UI)**: http://localhost:9000
- **API EmprestimoAgora**: http://localhost:8080

## 📚 Referências

- [Azurite Documentation](https://docs.microsoft.com/en-us/azure/storage/common/storage-use-azurite)
- [Azure Storage REST API](https://docs.microsoft.com/en-us/rest/api/storageservices/)
- [Azure EventHub Emulator](https://docs.microsoft.com/en-us/azure/event-hubs/)

---

💡 **Dica**: Use o Kafdrop (http://localhost:9000) para uma visualização mais amigável das mensagens do EventHub em tempo real!