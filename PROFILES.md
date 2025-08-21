# Perfis de Desenvolvimento

Este projeto possui dois perfis de desenvolvimento configurados para diferentes bancos de dados:

## Perfil H2 (Banco em memória/arquivo)

**Arquivo de configuração:** `application-h2.properties`

**Como usar:**
```bash
# Executar com H2
mvn quarkus:dev "-Dquarkus.profile=h2"

# Ou definir via variável de ambiente
set QUARKUS_PROFILE=h2
mvn quarkus:dev
```

**Características:**
- Banco de dados H2 com persistência em arquivo
- Arquivo do banco: `./data/api_emprestimo_agora_dev.mv.db`
- Não requer instalação de banco externo
- Ideal para desenvolvimento local rápido
- Dados são mantidos entre execuções

## Perfil PostgreSQL

**Arquivo de configuração:** `application-postgres.properties`

**Como usar:**
```bash
# Executar com PostgreSQL
mvn quarkus:dev "-Dquarkus.profile=postgres"

# Ou definir via variável de ambiente
set QUARKUS_PROFILE=postgres
mvn quarkus:dev
```

**Pré-requisitos:**
- PostgreSQL instalado e rodando na porta 5432
- Banco de dados `api_emprestimo_agora_dev` criado
- Usuário `api_emprestimo_agora` com senha `dev123456`

**Configuração do PostgreSQL:**
```sql
-- Conectar como superusuário e executar:
CREATE USER api_emprestimo_agora WITH PASSWORD 'dev123456';
CREATE DATABASE api_emprestimo_agora_dev OWNER api_emprestimo_agora;
GRANT ALL PRIVILEGES ON DATABASE api_emprestimo_agora_dev TO api_emprestimo_agora;
```

**Características:**
- Banco de dados PostgreSQL
- Melhor para desenvolvimento que simula ambiente de produção
- Suporte completo a recursos avançados do PostgreSQL
- Dados persistem independente da aplicação

## Configurações Personalizadas

Você pode personalizar as configurações editando os arquivos:
- `src/main/resources/application-h2.properties`
- `src/main/resources/application-postgres.properties`

### Alterando credenciais do PostgreSQL

Para usar credenciais diferentes, edite o arquivo `application-postgres.properties`:
```properties
quarkus.datasource.username=seu_usuario
quarkus.datasource.password=sua_senha
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/seu_banco
```

**Credenciais atuais (alinhadas com docker-compose.yml):**
- Usuário: `api_emprestimo_agora`
- Senha: `dev123456`
- Banco: `api_emprestimo_agora_dev`

## Docker Compose (Opcional)

Para facilitar o uso do PostgreSQL, você pode usar o Docker Compose já configurado no projeto:

```bash
# Subir apenas o PostgreSQL
docker-compose up postgres-dev

# Ou subir em background
docker-compose up -d postgres-dev
```

## Testando os Perfis

Após iniciar a aplicação com qualquer perfil, você pode testar os endpoints:

```bash
# Testar endpoint de volume diário
curl "http://localhost:8080/api/volume/diario?dataReferencia=2025-08-21"

# Testar endpoint de volume por período
curl "http://localhost:8080/api/volume/periodo?dataInicio=2025-08-18&dataFim=2025-08-22"
```

## Variáveis de Ambiente

Você também pode definir o perfil via variável de ambiente:

```bash
# Windows
set QUARKUS_PROFILE=h2
mvn quarkus:dev

# Linux/Mac
export QUARKUS_PROFILE=postgres
mvn quarkus:dev
```

## Troubleshooting

### Erro de conexão com PostgreSQL
- Verifique se o PostgreSQL está rodando: `pg_isready -h localhost -p 5432`
- Verifique se o banco existe: `psql -h localhost -U postgres -l`
- Verifique as credenciais no arquivo de configuração

### Problemas com H2
- Se o arquivo do banco estiver corrompido, delete a pasta `./data/`
- O H2 será recriado automaticamente na próxima execução