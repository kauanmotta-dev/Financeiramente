package com.financeiramente.desktop.app;

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
import com.financeiramente.core.usecase.CriarPlanejamentoMensalUseCase;
import com.financeiramente.core.usecase.ConfirmarPlanejamentoUseCase;
import com.financeiramente.core.usecase.DefinirLimiteCategoriaUseCase;
import com.financeiramente.core.usecase.DefinirComoPlanosPadraoUseCase;
import com.financeiramente.core.usecase.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.ListarLancamentosUseCase;
import com.financeiramente.core.usecase.RegistrarLancamentoUseCase;
import com.financeiramente.core.usecase.ReordenarCategoriasUseCase;
import com.financeiramente.desktop.db.JdbcDatabaseDriver;

public class AppContext {

    private static AppContext instance;

    private final JdbcDatabaseDriver databaseDriver;

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

    private AppContext() {
        this.databaseDriver = new JdbcDatabaseDriver();
        new DatabaseMigrator(databaseDriver).migrate();

        this.categoriaRepository          = new CategoriaDao(databaseDriver);
        this.lancamentoRepository         = new LancamentoDao(databaseDriver);
        this.tagRepository                = new TagDao(databaseDriver);
        this.planejamentoRepository       = new PlanejamentoDao(databaseDriver);
        this.lancamentoRecorrenteRepository = new LancamentoRecorrenteDao(databaseDriver);
        this.provisaoRepository           = new ProvisaoDao(databaseDriver);
        this.metaRepository               = new MetaDao(databaseDriver);
        this.aporteMetaRepository         = new AporteMetaDao(databaseDriver);

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
    }

    public static synchronized AppContext get() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public CategoriaRepository getCategoriaRepository()                   { return categoriaRepository; }
    public LancamentoRepository getLancamentoRepository()                 { return lancamentoRepository; }
    public TagRepository getTagRepository()                               { return tagRepository; }
    public PlanejamentoRepository getPlanejamentoRepository()             { return planejamentoRepository; }
    public LancamentoRecorrenteRepository getLancamentoRecorrenteRepository() { return lancamentoRecorrenteRepository; }
    public ProvisaoRepository getProvisaoRepository()                     { return provisaoRepository; }
    public MetaRepository getMetaRepository()                             { return metaRepository; }
    public AporteMetaRepository getAporteMetaRepository()                 { return aporteMetaRepository; }
    public JdbcDatabaseDriver getDatabaseDriver()                         { return databaseDriver; }

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
}
