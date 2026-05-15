package com.financeiramente.android.app;

import android.content.Context;

/**
 * Singleton de injeção de dependências manual para o módulo Android.
 * Instancia e expõe repositórios e casos de uso.
 */
public class AppContext {

    private static AppContext instance;

    private AppContext(Context context) {
        // Inicialização do DatabaseDriver e DAOs será adicionada no EP-03
    }

    public static synchronized AppContext get(Context context) {
        if (instance == null) {
            instance = new AppContext(context.getApplicationContext());
        }
        return instance;
    }
}
