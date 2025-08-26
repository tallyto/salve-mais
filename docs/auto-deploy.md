# Configuração de Deploy Automático

Para configurar o deploy automático do projeto, siga os passos abaixo:

## Na VPS

1. Certifique-se de que o diretório do projeto esteja configurado corretamente:

   ```bash
   cd ~/projetos/gestor-financeiro
   git remote -v  # Verifique se a origem está apontando para o GitHub
   ```

2. Garanta que o deploy.sh tenha permissões de execução:

   ```bash
   chmod +x deploy.sh
   ```

3. Teste o script de deploy manualmente:

   ```bash
   ./deploy.sh
   ```

## No GitHub

1. Adicione os seguintes segredos no repositório:
   - `SSH_PRIVATE_KEY`: Conteúdo da chave privada SSH
   - `SSH_HOST`: Endereço IP ou domínio da VPS
   - `SSH_USER`: Nome de usuário na VPS

2. Verifique se o workflow está configurado para a branch correta (master ou main)

## Para testar

1. Faça uma pequena alteração no código
2. Envie para o GitHub:

   ```bash
   git push origin master
   ```

3. Verifique a execução do workflow na aba "Actions" do GitHub
4. Verifique se as mudanças foram aplicadas na VPS

## Solução de problemas

- **Erro de permissão**: Verifique se o usuário SSH tem permissões adequadas no diretório do projeto
- **Erro de conexão**: Verifique se a chave SSH foi adicionada corretamente nos segredos do GitHub
- **Erro no deploy**: Verifique os logs do workflow no GitHub e os logs do Docker na VPS
