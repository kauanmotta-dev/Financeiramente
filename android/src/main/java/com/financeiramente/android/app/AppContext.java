package com.financeiramente.android.app;

import android.content.Context;
import android.util.Log;

import com.financeiramente.android.db.AndroidDatabaseDriver;
import com.financeiramente.core.dao.AporteMetaDao;
import com.financeiramente.core.dao.CategoriaDao;
import com.financeiramente.core.dao.LancamentoDao;
import com.financeiramente.core.dao.LancamentoRecorrenteDao;
import com.financeiramente.core.dao.MetaDao;
import com.financeiramente.core.dao.TagDao;
import com.financeiramente.core.db.DatabaseMigrator;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRecorrenteRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.CalcularSaldoMensalUseCase;
import com.financeiramente.core.usecase.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.CriarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.CalcularProjecaoMetaUseCase;
import com.financeiramente.core.usecase.CriarMetaUseCase;
import com.financeiramente.core.usecase.DeletarAporteMetaUseCase;
import com.financeiramente.core.usecase.DesativarMetaUseCase;
import com.financeiramente.core.usecase.EditarMetaUseCase;
import com.financeiramente.core.usecase.RegistrarAporteMetaUseCase;
import com.financeiramente.core.usecase.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.DesativarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.EditarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.GerarLancamentosRecorrentesUseCase;
import com.financeiramente.core.usecase.GerarRelatorioUseCase;
import com.financeiramente.core.usecase.ListarLancamentosUseCase;
import com.financeiramente.core.usecase.RegistrarLancamentoUseCase;
import com.financeiramente.core.usecase.ReordenarCategoriasUseCase;

public class AppContext {

    private static final String TAG = "AppContext";
    private static final String DB_NAME = "financeiramente.db";

    private static AppContext instance;

    private final AndroidDatabaseDriver databaseDriver;

    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final TagRepository tagRepository;
    private final LancamentoRecorrenteRepository lancamentoRecorrenteRepository;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;

    private final CriarCategoriaUseCase criarCategoriaUseCase;
    private final EditarCategoriaUseCase editarCategoriaUseCase;
    private final DeletarCategoriaUseCase deletarCategoriaUseCase;
    private final ReordenarCategoriasUseCase reordenarCategoriasUseCase;
    private final RegistrarLancamentoUseCase registrarLancamentoUseCase;
    private final EditarLancamentoUseCase editarLancamentoUseCase;
    private final DeletarLancamentoUseCase deletarLancamentoUseCase;
    private final ListarLancamentosUseCase listarLancamentosUseCase;
    private final CalcularSaldoMensalUseCase calcularSaldoMensalUseCase;
    private final CriarLancamentoRecorrenteUseCase criarLancamentoRecorrenteUseCase;
    private final EditarLancamentoRecorrenteUseCase editarLancamentoRecorrenteUseCase;
    private final DesativarLancamentoRecorrenteUseCase desativarLancamentoRecorrenteUseCase;
    private final GerarLancamentosRecorrentesUseCase gerarLancamentosRecorrentesUseCase;
    private final CriarMetaUseCase criarMetaUseCase;
    private final EditarMetaUseCase editarMetaUseCase;
    private final DesativarMetaUseCase desativarMetaUseCase;
    private final RegistrarAporteMetaUseCase registrarAporteMetaUseCase;
    private final DeletarAporteMetaUseCase deletarAporteMetaUseCase;
    private final CalcularProjecaoMetaUseCase calcularProjecaoMetaUseCase;
    private final GerarRelatorioUseCase gerarRelatorioUseCase;

    private AppContext(Context context) {
        this.databaseDriver = inicializarDriverComRecuperacao(context);

        this.categoriaRepository             = new CategoriaDao(databaseDriver);
        this.lancamentoRepository            = new LancamentoDao(databaseDriver);
        this.tagRepository                   = new TagDao(databaseDriver);
        this.lancamentoRecorrenteRepository  = new LancamentoRecorrenteDao(databaseDriver);
        this.metaRepository                  = new MetaDao(databaseDriver);
        this.aporteMetaRepository            = new AporteMetaDao(databaseDriver);

        this.criarCategoriaUseCase      = new CriarCategoriaUseCase(categoriaRepository);
        this.editarCategoriaUseCase     = new EditarCategoriaUseCase(categoriaRepository);
        this.deletarCategoriaUseCase    = new DeletarCategoriaUseCase(categoriaRepository, lancamentoRepository);
        this.reordenarCategoriasUseCase = new ReordenarCategoriasUseCase(categoriaRepository);
        this.registrarLancamentoUseCase  = new RegistrarLancamentoUseCase(lancamentoRepository, tagRepository, categoriaRepository);
        this.editarLancamentoUseCase      = new EditarLancamentoUseCase(lancamentoRepository, tagRepository, categoriaRepository);
        this.deletarLancamentoUseCase     = new DeletarLancamentoUseCase(lancamentoRepository);
        this.listarLancamentosUseCase     = new ListarLancamentosUseCase(lancamentoRepository);
        this.calcularSaldoMensalUseCase   = new CalcularSaldoMensalUseCase(
                lancamentoRepository, categoriaRepository);
        this.criarLancamentoRecorrenteUseCase    = new CriarLancamentoRecorrenteUseCase(lancamentoRecorrenteRepository, categoriaRepository);
        this.editarLancamentoRecorrenteUseCase   = new EditarLancamentoRecorrenteUseCase(lancamentoRecorrenteRepository);
        this.desativarLancamentoRecorrenteUseCase = new DesativarLancamentoRecorrenteUseCase(lancamentoRecorrenteRepository);
        this.gerarLancamentosRecorrentesUseCase  = new GerarLancamentosRecorrentesUseCase(lancamentoRecorrenteRepository, lancamentoRepository);
        this.criarMetaUseCase            = new CriarMetaUseCase(metaRepository);
        this.editarMetaUseCase           = new EditarMetaUseCase(metaRepository);
        this.desativarMetaUseCase        = new DesativarMetaUseCase(metaRepository);
        this.registrarAporteMetaUseCase  = new RegistrarAporteMetaUseCase(metaRepository, aporteMetaRepository);
        this.deletarAporteMetaUseCase    = new DeletarAporteMetaUseCase(metaRepository, aporteMetaRepository);
        this.calcularProjecaoMetaUseCase = new CalcularProjecaoMetaUseCase(metaRepository);
        this.gerarRelatorioUseCase       = new GerarRelatorioUseCase(lancamentoRepository, categoriaRepository, tagRepository);
    }

    private AndroidDatabaseDriver inicializarDriverComRecuperacao(Context context) {
        try {
            AndroidDatabaseDriver driver = new AndroidDatabaseDriver(context);
            new DatabaseMigrator(driver).migrate();
            return driver;
        } catch (Exception firstFailure) {
            // If Android restores an old DB backup, migration can fail at startup.
            Log.w(TAG, "Falha ao iniciar banco; tentando recriar base local", firstFailure);
            context.deleteDatabase(DB_NAME);
            try {
                AndroidDatabaseDriver driver = new AndroidDatabaseDriver(context);
                new DatabaseMigrator(driver).migrate();
                return driver;
            } catch (Exception secondFailure) {
                throw new IllegalStateException("Nao foi possivel inicializar o banco de dados", secondFailure);
            }
        }
    }

    public static synchronized AppContext get(Context context) {
        if (instance == null) {
            instance = new AppContext(context.getApplicationContext());
        }
        return instance;
    }

    public CategoriaRepository getCategoriaRepository()                       { return categoriaRepository; }
    public LancamentoRepository getLancamentoRepository()                     { return lancamentoRepository; }
    public TagRepository getTagRepository()                                   { return tagRepository; }
    public LancamentoRecorrenteRepository getLancamentoRecorrenteRepository() { return lancamentoRecorrenteRepository; }
    public MetaRepository getMetaRepository()                                 { return metaRepository; }
    public AporteMetaRepository getAporteMetaRepository()                     { return aporteMetaRepository; }

    public CriarCategoriaUseCase getCriarCategoriaUseCase()                   { return criarCategoriaUseCase; }
    public EditarCategoriaUseCase getEditarCategoriaUseCase()                 { return editarCategoriaUseCase; }
    public DeletarCategoriaUseCase getDeletarCategoriaUseCase()               { return deletarCategoriaUseCase; }
    public ReordenarCategoriasUseCase getReordenarCategoriasUseCase()         { return reordenarCategoriasUseCase; }
    public RegistrarLancamentoUseCase getRegistrarLancamentoUseCase()         { return registrarLancamentoUseCase; }
    public EditarLancamentoUseCase getEditarLancamentoUseCase()               { return editarLancamentoUseCase; }
    public DeletarLancamentoUseCase getDeletarLancamentoUseCase()             { return deletarLancamentoUseCase; }
    public ListarLancamentosUseCase getListarLancamentosUseCase()             { return listarLancamentosUseCase; }
    public CalcularSaldoMensalUseCase getCalcularSaldoMensalUseCase()         { return calcularSaldoMensalUseCase; }
    public CriarLancamentoRecorrenteUseCase getCriarLancamentoRecorrenteUseCase()       { return criarLancamentoRecorrenteUseCase; }
    public EditarLancamentoRecorrenteUseCase getEditarLancamentoRecorrenteUseCase()     { return editarLancamentoRecorrenteUseCase; }
    public DesativarLancamentoRecorrenteUseCase getDesativarLancamentoRecorrenteUseCase() { return desativarLancamentoRecorrenteUseCase; }
    public GerarLancamentosRecorrentesUseCase getGerarLancamentosRecorrentesUseCase()   { return gerarLancamentosRecorrentesUseCase; }
    public CriarMetaUseCase getCriarMetaUseCase()                                       { return criarMetaUseCase; }
    public EditarMetaUseCase getEditarMetaUseCase()                                     { return editarMetaUseCase; }
    public DesativarMetaUseCase getDesativarMetaUseCase()                               { return desativarMetaUseCase; }
    public RegistrarAporteMetaUseCase getRegistrarAporteMetaUseCase()                   { return registrarAporteMetaUseCase; }
    public DeletarAporteMetaUseCase getDeletarAporteMetaUseCase()                       { return deletarAporteMetaUseCase; }
    public CalcularProjecaoMetaUseCase getCalcularProjecaoMetaUseCase()                 { return calcularProjecaoMetaUseCase; }
    public GerarRelatorioUseCase getGerarRelatorioUseCase()                             { return gerarRelatorioUseCase; }
}
