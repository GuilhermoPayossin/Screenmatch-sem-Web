package br.com.alura.ScreenMatch.services;

public interface IConverterDados {
    <T> T obterDados(String json, Class<T> classe);
}
