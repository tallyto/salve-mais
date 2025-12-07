package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.NotificacaoDTO;
import com.tallyto.gestorfinanceiro.api.dto.NotificacaoEmailResponseDTO;
import com.tallyto.gestorfinanceiro.context.TenantContext;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.entities.Usuario;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.UsuarioRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificacaoEmailSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoEmailSchedulerService.class);

    @Autowired
    private NotificacaoEmailService notificacaoEmailService;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.mail.from.name}")
    private String mailFromName;

    /**
     * Executa a cada hora para verificar se há notificações que precisam ser enviadas
     * Timezone: America/Sao_Paulo (Brasília)
     */
    @Scheduled(cron = "0 0 * * * *", zone = "America/Sao_Paulo") // Executa a cada hora no minuto 0
    public void verificarEEnviarNotificacoes() {
        logger.debug("Verificando notificações para envio...");
        
        List<NotificacaoEmailResponseDTO> notificacoesAtivas = notificacaoEmailService.obterTodasNotificacoesAtivas();
        LocalTime horaAtual = LocalTime.now();
        
        // Tolerância de 30 minutos para envio
        LocalTime horaInicio = horaAtual.minusMinutes(30);
        LocalTime horaFim = horaAtual.plusMinutes(30);
        
        for (NotificacaoEmailResponseDTO notificacao : notificacoesAtivas) {
            LocalTime horarioNotificacao = notificacao.getHorario();
            
            // Verifica se o horário está dentro da janela de envio
            if (horarioNotificacao.isAfter(horaInicio) && horarioNotificacao.isBefore(horaFim)) {
                enviarEmailNotificacao(notificacao);
            }
        }
    }

    /**
     * Envia uma notificação de teste imediatamente para o tenant especificado
     */
    public void enviarNotificacaoTeste(NotificacaoEmailResponseDTO notificacao) {
        logger.info("Enviando notificação de TESTE para o domínio: {}", notificacao.getDomain());
        enviarEmailNotificacao(notificacao, true);
    }

    private void enviarEmailNotificacao(NotificacaoEmailResponseDTO notificacao) {
        enviarEmailNotificacao(notificacao, false);
    }

    private void enviarEmailNotificacao(NotificacaoEmailResponseDTO notificacao, boolean isTeste) {
        try {
            String domain = notificacao.getDomain();
            String tipoEmail = isTeste ? "TESTE" : "programada";
            logger.info("Enviando notificação {} para o domínio: {}", tipoEmail, domain);
            
            // Buscar tenant
            Tenant tenant = tenantRepository.findByDomain(domain).orElse(null);
            if (tenant == null) {
                logger.warn("Tenant não encontrado para o domínio: {}", domain);
                return;
            }
            
            // Configurar contexto do tenant
            TenantContext.setCurrentTenant(domain);
            
            try {
                // Buscar notificações do sistema
                List<NotificacaoDTO> notificacoes = notificacaoService.obterNotificacoes();
                
                if (notificacoes.isEmpty()) {
                    if (isTeste) {
                        logger.info("Nenhuma notificação pendente para teste, enviando email de exemplo para o domínio: {}", domain);
                        // Criar notificações de exemplo para teste
                        notificacoes = criarNotificacoesExemplo();
                    } else {
                        logger.info("Nenhuma notificação pendente para o domínio: {}", domain);
                        return;
                    }
                }
                
                // Buscar usuários do tenant
                // Como estamos no contexto do tenant, o JPA já filtra automaticamente pelo schema
                List<Usuario> usuarios = usuarioRepository.findAll();
                
                if (usuarios.isEmpty()) {
                    logger.warn("Nenhum usuário encontrado para o domínio: {}", domain);
                    if (isTeste) {
                        logger.info("Enviando email de teste para o domínio do tenant como fallback");
                        // Em ambiente de teste, continuar mesmo sem usuários cadastrados
                        // Isso permite testar a configuração de email
                    } else {
                        return;
                    }
                }
                
                // Construir conteúdo do email
                String conteudoEmailHtml = construirConteudoEmailHtml(tenant, notificacoes);
                
                // Enviar email para cada usuário
                for (Usuario usuario : usuarios) {
                    String prefixo = isTeste ? "[TESTE] " : "";
                    String assunto = String.format("%sSalve Mais - Resumo Diário de Notificações (%s)", prefixo, tenant.getName());
                    enviarEmailHtml(usuario.getEmail(), assunto, conteudoEmailHtml);
                    logger.info("Email de notificação {} enviado para: {}", isTeste ? "de TESTE" : "", usuario.getEmail());
                }
                
            } finally {
                TenantContext.clear();
            }
            
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação para o domínio: {}", notificacao.getDomain(), e);
        }
    }

    private void enviarEmailHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            logger.info("Email HTML enviado para: {}, assunto: {}", to, subject);
        } catch (Exception e) {
            logger.error("Erro ao enviar email HTML para: {}, assunto: {}", to, subject, e);
        }
    }

    private String construirConteudoEmailHtml(Tenant tenant, List<NotificacaoDTO> notificacoes) {
        try {
            // Ler template HTML
            ClassPathResource resource = new ClassPathResource("templates/notificacao-diaria.html");
            String htmlTemplate = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // Contar tipos de notificações
            long contasAtrasadas = notificacoes.stream()
                    .filter(n -> "CONTA_ATRASADA".equals(n.tipo()))
                    .count();
            long contasProximasVencimento = notificacoes.stream()
                    .filter(n -> "CONTA_PROXIMA_VENCIMENTO".equals(n.tipo()))
                    .count();
            long faturasAtrasadas = notificacoes.stream()
                    .filter(n -> "FATURA_ATRASADA".equals(n.tipo()))
                    .count();
            long faturasProximasVencimento = notificacoes.stream()
                    .filter(n -> "FATURA_PROXIMA_VENCIMENTO".equals(n.tipo()))
                    .count();
            
            // Agrupar por prioridade
            List<NotificacaoDTO> notificacoesCriticas = notificacoes.stream()
                    .filter(n -> n.prioridade() == NotificacaoDTO.Prioridade.CRITICA)
                    .toList();
            List<NotificacaoDTO> notificacoesAltas = notificacoes.stream()
                    .filter(n -> n.prioridade() == NotificacaoDTO.Prioridade.ALTA)
                    .toList();
            List<NotificacaoDTO> notificacoesMedias = notificacoes.stream()
                    .filter(n -> n.prioridade() == NotificacaoDTO.Prioridade.MEDIA)
                    .toList();
            
            // Data atual formatada
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataAtual = LocalDate.now().format(formatter);
            
            // Alerta crítico se houver notificações críticas
            String alertaCritico = notificacoesCriticas.isEmpty() ? "" : 
                    "<div class=\"alerta-importante\">" +
                    "⚠️ <strong>ATENÇÃO:</strong> Você possui " + notificacoesCriticas.size() + 
                    " notificação(ões) crítica(s) que requerem ação imediata!" +
                    "</div>";
            
            // Substituir placeholders
            String htmlContent = htmlTemplate
                    .replace("{{TENANT_NAME}}", tenant.getName())
                    .replace("{{DATA_ATUAL}}", dataAtual)
                    .replace("{{TOTAL_NOTIFICACOES}}", String.valueOf(notificacoes.size()))
                    .replace("{{CONTAS_ATRASADAS}}", contasAtrasadas > 0 ? 
                            "<div class=\"resumo-item\"><strong>Contas atrasadas:</strong> <span class=\"badge\">" + contasAtrasadas + "</span></div>" : "")
                    .replace("{{CONTAS_PROXIMAS}}", contasProximasVencimento > 0 ? 
                            "<div class=\"resumo-item\"><strong>Contas próximas do vencimento:</strong> <span class=\"badge\">" + contasProximasVencimento + "</span></div>" : "")
                    .replace("{{FATURAS_ATRASADAS}}", faturasAtrasadas > 0 ? 
                            "<div class=\"resumo-item\"><strong>Faturas atrasadas:</strong> <span class=\"badge\">" + faturasAtrasadas + "</span></div>" : "")
                    .replace("{{FATURAS_PROXIMAS}}", faturasProximasVencimento > 0 ? 
                            "<div class=\"resumo-item\"><strong>Faturas próximas do vencimento:</strong> <span class=\"badge\">" + faturasProximasVencimento + "</span></div>" : "")
                    .replace("{{ALERTA_CRITICO}}", alertaCritico)
                    .replace("{{NOTIFICACOES_CRITICAS}}", construirSecaoPrioridade("⚠️ CRÍTICAS", notificacoesCriticas, "critica"))
                    .replace("{{NOTIFICACOES_ALTAS}}", construirSecaoPrioridade("🔴 ALTA PRIORIDADE", notificacoesAltas, "alta"))
                    .replace("{{NOTIFICACOES_MEDIAS}}", construirSecaoPrioridade("🟡 MÉDIA PRIORIDADE", notificacoesMedias, "media"));
            
            return htmlContent;
        } catch (Exception e) {
            logger.error("Erro ao construir conteúdo do email HTML", e);
            // Fallback para texto simples
            return construirConteudoEmailTexto(tenant, notificacoes);
        }
    }

    private String construirSecaoPrioridade(String titulo, List<NotificacaoDTO> notificacoes, String classe) {
        if (notificacoes.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"prioridade-section ").append(classe).append("\">");
        sb.append("<h3>").append(titulo).append("</h3>");
        
        for (NotificacaoDTO notif : notificacoes) {
            sb.append("<div class=\"notificacao-item\">");
            sb.append("<div class=\"notificacao-titulo\">").append(notif.titulo()).append("</div>");
            sb.append("<div class=\"notificacao-mensagem\">").append(notif.mensagem()).append("</div>");
            
            // Adicionar detalhes se disponíveis
            sb.append("<div class=\"notificacao-detalhes\">");
            sb.append("<span>📌 Tipo: ").append(formatarTipo(notif.tipo())).append("</span>");
            if (notif.diasDiferenca() != null) {
                String diasTexto = notif.diasDiferenca() == 1 ? "dia" : "dias";
                if (notif.diasDiferenca() < 0) {
                    sb.append("<span>⏰ Atrasado há ").append(Math.abs(notif.diasDiferenca())).append(" ").append(diasTexto).append("</span>");
                } else if (notif.diasDiferenca() == 0) {
                    sb.append("<span>⏰ Vence hoje</span>");
                } else {
                    sb.append("<span>⏰ Vence em ").append(notif.diasDiferenca()).append(" ").append(diasTexto).append("</span>");
                }
            }
            sb.append("</div>");
            
            sb.append("</div>");
        }
        
        sb.append("</div>");
        return sb.toString();
    }
    
    private String formatarTipo(String tipo) {
        return switch (tipo) {
            case "CONTA_ATRASADA" -> "Conta Atrasada";
            case "CONTA_PROXIMA_VENCIMENTO" -> "Conta Próxima do Vencimento";
            case "FATURA_ATRASADA" -> "Fatura Atrasada";
            case "FATURA_PROXIMA_VENCIMENTO" -> "Fatura Próxima do Vencimento";
            default -> tipo;
        };
    }

    private List<NotificacaoDTO> criarNotificacoesExemplo() {
        return List.of(
                new NotificacaoDTO(
                        "CONTA_ATRASADA",
                        NotificacaoDTO.Prioridade.CRITICA,
                        "Conta de Luz - Janeiro/2025",
                        "Esta é uma notificação de exemplo. Sua conta de luz está com 5 dias de atraso.",
                        1L,
                        "CONTA",
                        -5L
                ),
                new NotificacaoDTO(
                        "CONTA_PROXIMA_VENCIMENTO",
                        NotificacaoDTO.Prioridade.ALTA,
                        "Conta de Água - Janeiro/2025",
                        "Esta é uma notificação de exemplo. Sua conta de água vence em 2 dias.",
                        2L,
                        "CONTA",
                        2L
                ),
                new NotificacaoDTO(
                        "FATURA_PROXIMA_VENCIMENTO",
                        NotificacaoDTO.Prioridade.MEDIA,
                        "Fatura Cartão de Crédito",
                        "Esta é uma notificação de exemplo. Sua fatura vence em 5 dias.",
                        3L,
                        "FATURA",
                        5L
                )
        );
    }

    private String construirConteudoEmailTexto(Tenant tenant, List<NotificacaoDTO> notificacoes) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Olá,\n\n");
        sb.append(String.format("Segue o resumo diário de notificações de %s:\n\n", tenant.getName()));
        
        // Contar tipos de notificações
        long contasAtrasadas = notificacoes.stream()
                .filter(n -> "CONTA_ATRASADA".equals(n.tipo()))
                .count();
        long contasProximasVencimento = notificacoes.stream()
                .filter(n -> "CONTA_PROXIMA_VENCIMENTO".equals(n.tipo()))
                .count();
        long faturasAtrasadas = notificacoes.stream()
                .filter(n -> "FATURA_ATRASADA".equals(n.tipo()))
                .count();
        long faturasProximasVencimento = notificacoes.stream()
                .filter(n -> "FATURA_PROXIMA_VENCIMENTO".equals(n.tipo()))
                .count();
        
        sb.append("=== RESUMO ===\n");
        sb.append(String.format("Total de notificações: %d\n", notificacoes.size()));
        if (contasAtrasadas > 0) {
            sb.append(String.format("Contas atrasadas: %d\n", contasAtrasadas));
        }
        if (contasProximasVencimento > 0) {
            sb.append(String.format("Contas próximas do vencimento: %d\n", contasProximasVencimento));
        }
        if (faturasAtrasadas > 0) {
            sb.append(String.format("Faturas atrasadas: %d\n", faturasAtrasadas));
        }
        if (faturasProximasVencimento > 0) {
            sb.append(String.format("Faturas próximas do vencimento: %d\n", faturasProximasVencimento));
        }
        
        sb.append("\n=== DETALHES ===\n\n");
        
        // Agrupar por prioridade
        List<NotificacaoDTO> notificacoesCriticas = notificacoes.stream()
                .filter(n -> n.prioridade() == NotificacaoDTO.Prioridade.CRITICA)
                .toList();
        List<NotificacaoDTO> notificacoesAltas = notificacoes.stream()
                .filter(n -> n.prioridade() == NotificacaoDTO.Prioridade.ALTA)
                .toList();
        List<NotificacaoDTO> notificacoesMedias = notificacoes.stream()
                .filter(n -> n.prioridade() == NotificacaoDTO.Prioridade.MEDIA)
                .toList();
        
        if (!notificacoesCriticas.isEmpty()) {
            sb.append("⚠️ CRÍTICAS:\n");
            for (NotificacaoDTO notif : notificacoesCriticas) {
                sb.append(String.format("  - %s: %s\n", notif.titulo(), notif.mensagem()));
            }
            sb.append("\n");
        }
        
        if (!notificacoesAltas.isEmpty()) {
            sb.append("🔴 ALTA PRIORIDADE:\n");
            for (NotificacaoDTO notif : notificacoesAltas) {
                sb.append(String.format("  - %s: %s\n", notif.titulo(), notif.mensagem()));
            }
            sb.append("\n");
        }
        
        if (!notificacoesMedias.isEmpty()) {
            sb.append("🟡 MÉDIA PRIORIDADE:\n");
            for (NotificacaoDTO notif : notificacoesMedias) {
                sb.append(String.format("  - %s: %s\n", notif.titulo(), notif.mensagem()));
            }
            sb.append("\n");
        }
        
        sb.append("\n");
        sb.append("Acesse o sistema para mais detalhes e gerenciar suas finanças.\n\n");
        sb.append("Atenciosamente,\n");
        sb.append("Equipe Salve Mais");
        
        return sb.toString();
    }
}
