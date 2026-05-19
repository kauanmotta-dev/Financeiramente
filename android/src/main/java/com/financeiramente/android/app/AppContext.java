package com.financeiramente.android.app;

import android.content.Context;

import com.financeiramente.android.db.AndroidDatabaseDriver;
import com.financeiramente.core.dao.AporteMetaDao;
import com.financeiramente.core.dao.CategoriaDao;
import com.financeiramente.core.dao.LancamentoDao;
import com.financeiramente.core.dao.LancamentoRecorrenteDao;
import com.financeiramente.core.dao.MetaDao;
import com.financeiramente.core.dao.PlanejamentoDao;
import com.financeiramente.core.dao.ProvisaoDao;
import com.financeiramente.core.dao.TagDao;
import com.financeiramente.core.db.DatabaseMigrator;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRecorrenteRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.repository.PlanejamentoRepository;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.CalcularSaldoMensalUseCase;
import com.financeiramente.core.usecase.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.CriarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.CriarPlanejamentoMensalUseCase;
import com.financeiramente.core.usecase.CriarProvisaoUseCase;
import com.financeiramente.core.usecase.ConfirmarPlanejamentoUseCase;
import com.financeiramente.core.usecase.CreditarProvisoesMensaisUseCase;
import com.financeiramente.core.usecase.DebitarProvisaoUseCase;
import com.financeiramente.core.usecase.DefinirLimiteCategoriaUseCase;
import com.financeiramente.core.usecase.DefinirComoPlanosPadraoUseCase;
import com.financeiramente.core.usecase.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.DesativarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.DesativarProvisaoUseCase;
import com.financeiramente.core.usecase.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.EditarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.EditarProvisaoUseCase;
import com.financeiramente.core.usecase.GerarLancamentosRecorrentesUseCase;
import com.financeiramente.core.usecase.ListarLancamentosUseCase;
import com.financeiramente.core.usecase.RegistrarLancamentoUseCase;
import com.financeiramente.core.usecase.ReordenarCategoriasUseCase;

public class AppContext {

    private static AppContext instance;

    private final AndroidDatabaseDriver databaseDriver;

    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final TagRepository tagRepository;
    private final PlanejamentoRepository planejamentoRepository;
    private final LancamentoRecorrenteRepository lancamentoRecorrenteRepository;
    private final ProvisaoRepository provisaoRepository;
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
    private final CriarPlanejamentoMensalUseCase criarPlanejamentoMensalUseCase;
    private final ConfirmarPlanejamentoUseCase confirmarPlanejamentoUseCase;
    private final DefinirLimiteCategoriaUseCase definirLimiteCategoriaUseCase;
    private final DefinirComoPlanosPadraoUseCase definirComoPlanosPadraoUseCase;
    private final CriarLancamentoRecorrenteUseCase criarLancamentoRecorrenteUseCase;
    private final EditarLancamentoRecorrenteUseCase editarLancamentoRecorrenteUseCase;
    private final DesativarLancamentoRecorrenteUseCase desativarLancamentoRecorrenteUseCase;
    private final GerarLancamentosRecorrentesUseCase gerarLancamentosRecorrentesUseCase;
    private final CriarProvisaoUseCase criarProvisaoUseCase;
    private final EditarProvisaoUseCase editarProvisaoUseCase;
    private final DesativarProvisaoUseCase desativarProvisaoUseCase;
    private final CreditarProvisoesMensaisUseCase creditarProvisoesMensaisUseCase;
    private final DebitarProvisaoUseCase debitarProvisaoUseCase;

    private AppContext(Context context) {
        this.databaseDriver = new AndroidDatabaseDriver(context);
        new DatabaseMigrator(databaseDriver).migrate();

        this.categoriaRepository             = new CategoriaDao(databaseDriver);
        this.lancamentoRepository            = new LancamentoDao(databaseDriver);
        this.tagRepository                   = new TagDao(databaseDriver);
        this.planejamentoRepository          = new PlanejamentoDao(databaseDriver);
        this.lancamentoRecorrenteRepository  = new LancamentoRecorrenteDao(databaseDriver);
        this.provisaoRepository              = new ProvisaoDao(databaseDriver);
        this.metaRepository                  = new MetaDao(databaseDriver);
        this.aporteMetaRepository            = new AporteMetaDao(databaseDriver);

        this.criarCategoriaUseCase      = new CriarCategoriaUseCase(categoriaRepository);
        this.editarCategoriaUseCase     = new EditarCategoriaUseCase(categoriaRepository);
        this.deletarCategoriaUseCase    = new DeletarCategoriaUseCase(categoriaRepository, lancamentoRepository);
        this.reordenarCategoriasUseCase = new ReordenarCategoriasUseCase(categoriaRepository);
        this.registrarLancamentoUseCase  = new RegistrarLancamentoUseCase(lancamentoRepository, tagRepository, provisaoRepository);
        this.editarLancamentoUseCase      = new EditarLancamentoUseCase(lancamentoRepository, tagRepository, provisaoRepository);
        this.deletarLancamentoUseCase     = new DeletarLancamentoUseCase(lancamentoRepository, provisaoRepository);
        this.listarLancamentosUseCase     = new ListarLancamentosUseCase(lancamentoRepository);
        this.calcularSaldoMensalUseCase   = new CalcularSaldoMensalUseCase(
                lancamentoRepository, planejamentoRepository, provisaoRepository, categoriaRepository);
        this.criarPlanejamentoMensalUseCase  = new CriarPlanejamentoMensalUseCase(planejamentoRepository);
        this.confirmarPlanejamentoUseCase    = new ConfirmarPlanejamentoUseCase(planejamentoRepository);
        this.definirLimiteCategoriaUseCase   = new DefinirLimiteCategoriaUseCase(planejamentoRepository);
        this.definirComoPlanosPadraoUseCase  = new DefinirComoPlanosPadraoUseCase(planejamentoRepository);
        this.criarLancamentoRecorrenteUseCase    = new CriarLancamentoRecorrenteUseCase(lancamentoRecorrenteRepository);
        this.editarLancamentoRecorrenteUseCase   = new EditarLancamentoRecorrenteUseCase(lancamentoRecorrenteRepository);
        this.desativarLancamentoRecorrenteUseCase = new DesativarLancamentoRecorrenteUseCase(lancamentoRecorrenteRepository);
        this.gerarLancamentosRecorrentesUseCase  = new GerarLancamentosRecorrentesUseCase(lancamentoRecorrenteRepository, lancamentoRepository);
        this.criarProvisaoUseCase        = new CriarProvisaoUseCase(provisaoRepository);
        this.editarProvisaoUseCase       = new EditarProvisaoUseCase(provisaoRepository);
        this.desativarProvisaoUseCase    = new DesativarProvisaoUseCase(provisaoRepository);
        this.creditarProvisoesMensaisUseCase = new CreditarProvisoesMensaisUseCase(provisaoRepository);
        this.debitarProvisaoUseCase      = new DebitarProvisaoUseCase(provisaoRepository);
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
    public PlanejamentoRepository getPlanejamentoRepository()                 { return planejamentoRepository; }
    public LancamentoRecorrenteRepository getLancamentoRecorrenteRepository() { return lancamentoRecorrenteRepository; }
    public ProvisaoRepository getProvisaoRepository()                         { return provisaoRepository; }
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
    public CriarPlanejamentoMensalUseCase getCriarPlanejamentoMensalUseCase() { return criarPlanejamentoMensalUseCase; }
    public ConfirmarPlanejamentoUseCase getConfirmarPlanejamentoUseCase()     { return confirmarPlanejamentoUseCase; }
    public DefinirLimiteCategoriaUseCase getDefinirLimiteCategoriaUseCase()   { return definirLimiteCategoriaUseCase; }
    public DefinirComoPlanosPadraoUseCase getDefinirComoPlanosPadraoUseCase() { return definirComoPlanosPadraoUseCase; }
    public CriarLancamentoRecorrenteUseCase getCriarLancamentoRecorrenteUseCase()       { return criarLancamentoRecorrenteUseCase; }
    public EditarLancamentoRecorrenteUseCase getEditarLancamentoRecorrenteUseCase()     { return editarLancamentoRecorrenteUseCase; }
    public DesativarLancamentoRecorrenteUseCase getDesativarLancamentoRecorrenteUseCase() { return desativarLancamentoRecorrenteUseCase; }
    public GerarLancamentosRecorrentesUseCase getGerarLancamentosRecorrentesUseCase()   { return gerarLancamentosRecorrentesUseCase; }
    public CriarProvisaoUseCase getCriarProvisaoUseCase()                               { return criarProvisaoUseCase; }
    public EditarProvisaoUseCase getEditarProvisaoUseCase()                             { return editarProvisaoUseCase; }
    public DesativarProvisaoUseCase getDesativarProvisaoUseCase()                       { return desativarProvisaoUseCase; }
    public CreditarProvisoesMensaisUseCase getCreditarProvisoesMensaisUseCase()         { return creditarProvisoesMensaisUseCase; }
    public DebitarProvisaoUseCase getDebitarProvisaoUseCase()                           { return debitarProvisaoUseCase; }
}
