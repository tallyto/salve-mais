# Sistema de Deploy e Rollback

## Deploy

O script `deploy.sh` realiza o deploy versionado da aplicação:

```bash
./deploy.sh
```

### Funcionalidades do Deploy:
- Extrai a versão automaticamente do `pom.xml` (artifactId: gestor-financeiro)
- **Valida se a versão já foi deployada** - impede deploy duplicado da mesma versão
- Cria backup da imagem atual antes do deploy
- Constrói e valida a nova imagem antes de derrubar containers
- Tageia imagens com versionamento (`gestor-financeiro:1.9.1`, `gestor-financeiro:latest`)
- Mantém os 5 backups mais recentes
- Interrompe o processo se houver falha no build

## Importante: Versionamento

**Antes de cada deploy, você DEVE atualizar a versão no `pom.xml`!**

O sistema não permite fazer deploy da mesma versão duas vezes. Se você tentar fazer deploy sem alterar a versão, receberá um erro:

```
Erro: Já existe uma imagem com a versão 1.9.1!
Por favor, atualize a versão no pom.xml antes de fazer o deploy.
```

### Como atualizar a versão:

Edite o arquivo `pom.xml` e altere a tag `<version>`:

```xml
<groupId>com.tallyto</groupId>
<artifactId>gestor-financeiro</artifactId>
<version>1.9.2</version>  <!-- Incremente a versão aqui -->
```

Siga o versionamento semântico:
- **Major** (x.0.0): Mudanças incompatíveis
- **Minor** (1.x.0): Novas funcionalidades compatíveis
- **Patch** (1.9.x): Correções de bugs

## Rollback

O script `rollback.sh` permite voltar para versões anteriores:

### Listar backups disponíveis:
```bash
./rollback.sh
```

### Fazer rollback para uma versão específica:
```bash
./rollback.sh backup_1.9.1_20251120_205500
```

### Funcionalidades do Rollback:
- Lista todos os backups disponíveis
- Cria backup de segurança antes do rollback
- Solicita confirmação antes de executar
- Restaura a versão selecionada
- Mantém histórico para reverter se necessário

## Estrutura de Versionamento

### Imagens Docker:
- `gestor-financeiro:latest` - Versão atual em produção
- `gestor-financeiro:1.9.1` - Tag da versão específica
- `gestor-financeiro:backup_1.9.1_20251120_205500` - Backup com timestamp

### Diretório de Backups:
```
backups/
├── backup_1.9.1_20251120_205500.version
├── backup_1.9.0_20251120_184230.version
└── ...
```

Cada arquivo `.version` contém o número da versão do backup correspondente.

## Exemplo de Workflow

1. **Deploy de nova versão:**
   ```bash
   ./deploy.sh
   ```

2. **Se houver problema, verificar backups:**
   ```bash
   ./rollback.sh
   ```

3. **Fazer rollback:**
   ```bash
   ./rollback.sh backup_1.9.0_20251120_184230
   ```

4. **Voltar para a versão mais recente:**
   ```bash
   ./deploy.sh
   ```

## Segurança

- Scripts validam o build antes de derrubar containers
- Backups automáticos antes de qualquer alteração
- Confirmação manual necessária para rollback
- Mantém histórico dos últimos 5 deploys
