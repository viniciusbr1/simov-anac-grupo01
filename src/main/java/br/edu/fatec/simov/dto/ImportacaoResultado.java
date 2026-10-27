package br.edu.fatec.simov.dto;

public record ImportacaoResultado(
        long linhasLidas, long validas, long importadas, long rejeitadas,
        long duplicadas, long ignoradasPorAmostragem,
        String mensagem, String politicaAmostragem) { }
