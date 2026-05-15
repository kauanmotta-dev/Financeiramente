package com.financeiramente.desktop.app;

/**
 * Singleton de injeção de dependências manual para o módulo Desktop.
 * Instancia e expõe repositórios e casos de uso.
 */
public class AppContext {

    private static AppContext instance;

    private AppContext() {
        // Inicialização do JdbcDatabaseDriver e DAOs será adicionada no EP-03
    }

    public static synchronized AppContext get() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }
}
