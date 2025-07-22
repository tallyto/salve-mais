package com.tallyto.gestorfinanceiro.api.dto;

public record NotificacaoDTO(
        String tipo,
        Prioridade prioridade,
        String titulo,
        String mensagem,
        Long entidadeId,
        String tipoEntidade,
        Long diasDiferenca
) {
    
    public enum Prioridade {
        BAIXA("Baixa"),
        MEDIA("Média"),
        ALTA("Alta"),
        CRITICA("Crítica");

        private final String descricao;

        Prioridade(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum TipoNotificacao {
        CONTA_ATRASADA("Conta Atrasada"),
        CONTA_PROXIMA_VENCIMENTO("Próximo Vencimento"),
        FATURA_ATRASADA("Fatura Atrasada"),
        FATURA_PROXIMA_VENCIMENTO("Fatura Próxima Vencimento"),
        LIMITE_CARTAO_EXCEDIDO("Limite do Cartão Excedido"),
        LIMITE_CARTAO_PROXIMO("Limite do Cartão Próximo");

        private final String descricao;

        TipoNotificacao(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}
