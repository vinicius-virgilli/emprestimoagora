# Guia dos Serviços Docker - Empréstimo Agora

Este documento explica todos os serviços disponíveis no `docker-compose.yml` e como utilizá-los.

## 📋 Visão Geral dos Serviços

### 🗄️ PostgreSQL (postgres-dev)
**Banco de dados principal da aplicação**
- **Imagem**: `postgres:15-alpine`
- **Porta**: `5432`
- **Database**: `api_emprestimo_agora_dev`
- **Usuário**: `api_emprestimo_agora`
- **Senha**: `dev123456`
- **Volume**: Dados persistem em `postgres_data`
- **Health Check**: Verifica se o banco está respondendo

### 🗃️ Azurite (azurite)
**Emulador do Azure Storage para desenvolvimento local**
- **Imagem**: `mcr.microsoft.com/azure-storage/azurite`
- **Portas**:
  - `10000`: Blob Service (armazenamento de arquivos)
  - `10001`: Queue Service (filas de mensagens)
  - `10002`: Table Service (armazenamento NoSQL)
- **Volume**: Dados persistem em `azurite_data`
- **Profiles**: `dev`, `storage`

### 📡 Event Hub Simulator (eventhub-simulator)
**Emulador do Azure Event Hub para streaming de eventos**
- **Imagem**: `mcr.microsoft.com/azure-messaging/eventhubs-emulator:latest`
- **Portas**:
  - `5672`: AMQP (protocolo de mensageria)
  - `9093`: Kafka (protocolo de streaming)
- **Volume**: Dados persistem em `eventhub_data`
- **Profiles**: `dev`, `eventhub`
- **Uso**: Telemetria, logs de auditoria, eventos de simulação

### 🔧 PgAdmin (pgadmin)
**Interface web para administração do PostgreSQL**
- **Imagem**: `dpage/pgadmin4:latest`
- **Porta**: `8081`
- **Email**: `admin@api-emprestimo-agora.com`
- **Senha**: `admin123`
- **Volume**: Configurações persistem em `pgadmin_data`
- **Profiles**: `admin`, `dev`

## 🚀 Como Usar

### Cenários de Uso

#### 1. Desenvolvimento Básico (apenas PostgreSQL)
```bash
# Iniciar apenas o banco de dados
docker-compose up -d postgres-dev
```

#### 2. Desenvolvimento Completo
```bash
# Iniciar todos os serviços de desenvolvimento
docker-compose --profile dev up -d
```

#### 3. Desenvolvimento com Interface de Administração
```bash
# Iniciar com PgAdmin incluído
docker-compose --profile dev --profile admin up -d
```

#### 4. Apenas Simuladores Azure
```bash
# Iniciar apenas Azurite e EventHub
docker-compose --profile storage --profile eventhub up -d
```

#### 5. Ambiente Completo
```bash
# Iniciar todos os serviços
docker-compose --profile dev --profile admin --profile storage --profile eventhub up -d
```

### Comandos Úteis

#### Verificar Status dos Serviços
```bash
# Ver todos os containers
docker-compose ps

# Ver logs de um serviço específico
docker-compose logs postgres-dev
docker-compose logs eventhub-simulator

# Seguir logs em tempo real
docker-compose logs -f postgres-dev
```

#### Gerenciar Serviços
```bash
# Parar todos os serviços
docker-compose down

# Parar e remover volumes (CUIDADO: apaga dados)
docker-compose down -v

# Reiniciar um serviço específico
docker-compose restart postgres-dev

# Reconstruir e iniciar
docker-compose up -d --build
```

#### Limpeza
```bash
# Remover containers parados
docker-compose down

# Remover volumes não utilizados
docker volume prune

# Remover tudo (containers, volumes, redes)
docker-compose down -v --remove-orphans
```

## 🔗 URLs de Acesso

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **Aplicação** | http://localhost:8080 | - |
| **PostgreSQL** | localhost:5432 | `api_emprestimo_agora` / `dev123456` |
| **PgAdmin** | http://localhost:8081 | `admin@api-emprestimo-agora.com` / `admin123` |
| **Azurite Blob** | http://localhost:10000 | Account: `devstoreaccount1` |
| **Azurite Queue** | http://localhost:10001 | Key: `Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==` |
| **Azurite Table** | http://localhost:10002 | - |
| **EventHub AMQP** | amqp://localhost:5672 | - |
| **EventHub Kafka** | localhost:9093 | - |

## 📊 Volumes Persistentes

Todos os dados são armazenados em volumes Docker nomeados:

- `api_emprestimo_agora_postgres_data`: Dados do PostgreSQL
- `api_emprestimo_agora_azurite_data`: Dados do Azurite
- `api_emprestimo_agora_eventhub_data`: Dados do Event Hub
- `api_emprestimo_agora_pgadmin_data`: Configurações do PgAdmin

### Backup dos Volumes
```bash
# Backup do PostgreSQL
docker run --rm -v api_emprestimo_agora_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup.tar.gz -C /data .

# Restaurar backup
docker run --rm -v api_emprestimo_agora_postgres_data:/data -v $(pwd):/backup alpine tar xzf /backup/postgres_backup.tar.gz -C /data
```

## 🔧 Configuração da Aplicação

As configurações da aplicação para conectar aos serviços estão em:
- `src/main/resources/application-dev.properties`

### Strings de Conexão
```properties
# PostgreSQL
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/api_emprestimo_agora_dev
quarkus.datasource.username=api_emprestimo_agora
quarkus.datasource.password=dev123456

# Event Hub
eventhub.connection.string=Endpoint=sb://localhost:5672/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fake-key-for-development

# Azurite (se necessário)
azure.storage.connection-string=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:10000/devstoreaccount1;
```

## 🐛 Troubleshooting

### Problemas Comuns

#### PostgreSQL não inicia
```bash
# Verificar logs
docker-compose logs postgres-dev

# Verificar se a porta está em uso
netstat -an | findstr :5432

# Remover volume e recriar
docker-compose down -v
docker-compose up -d postgres-dev
```

#### Event Hub não conecta
```bash
# Verificar se o container está rodando
docker-compose ps eventhub-simulator

# Verificar logs
docker-compose logs eventhub-simulator

# Testar conectividade
telnet localhost 5672
```

#### Azurite não responde
```bash
# Verificar status
docker-compose ps azurite

# Testar Blob Service
curl http://localhost:10000/devstoreaccount1
```

### Health Checks
```bash
# PostgreSQL
docker-compose exec postgres-dev pg_isready -U api_emprestimo_agora -d api_emprestimo_agora_dev

# Verificar conectividade de rede
docker-compose exec postgres-dev ping eventhub-simulator
```

## 📝 Notas Importantes

1. **Profiles**: Use profiles para iniciar apenas os serviços necessários
2. **Volumes**: Os dados persistem entre reinicializações
3. **Rede**: Todos os serviços estão na mesma rede Docker
4. **Health Checks**: PostgreSQL tem verificação automática de saúde
5. **Restart Policy**: Serviços reiniciam automaticamente em caso de falha

## 🔄 Atualizações

Para atualizar as imagens:
```bash
# Baixar versões mais recentes
docker-compose pull

# Recriar containers com novas imagens
docker-compose up -d --force-recreate
```