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
import com.financeiramente.core.usecase.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.EditarCategoriaUseCase;
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

        this.criarCategoriaUseCase    = new CriarCategoriaUseCase(categoriaRepository);
        this.editarCategoriaUseCase   = new EditarCategoriaUseCase(categoriaRepository);
        this.deletarCategoriaUseCase  = new DeletarCategoriaUseCase(categoriaRepository, lancamentoRepository);
        this.reordenarCategoriasUseCase = new ReordenarCategoriasUseCase(categoriaRepository);
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
    public ReordenarCategoriasUseCase getReordenarCategoriasUseCase()         { return reordenarCategoriasUseCase; }}
