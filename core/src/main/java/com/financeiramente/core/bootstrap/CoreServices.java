package com.financeiramente.core.bootstrap;

import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.fatura.PagarFaturaUseCase;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;
import com.financeiramente.core.usecase.categoria.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.ReordenarCategoriasUseCase;
import com.financeiramente.core.usecase.cartao.CriarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.cartao.DesativarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.cartao.EditarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.GerarCobrancasRecorrentesCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.ListarLancamentosUseCase;
import com.financeiramente.core.usecase.lancamento.ListarComprasCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.CancelarRecorrenciaCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;
import com.financeiramente.core.usecase.meta.CalcularProjecaoMetaUseCase;
import com.financeiramente.core.usecase.meta.CalcularTotalAportesMesUseCase;
import com.financeiramente.core.usecase.meta.CriarMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarAporteMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarMetaUseCase;
import com.financeiramente.core.usecase.meta.EditarMetaUseCase;
import com.financeiramente.core.usecase.meta.RegistrarAporteMetaUseCase;
import com.financeiramente.core.usecase.relatorio.GerarRelatorioUseCase;
import com.financeiramente.core.usecase.saldo.CalcularSaldoDashboardUseCase;


public class CoreServices {

    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final TagRepository tagRepository;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;
    private final CartaoCreditoRepository cartaoRepository;
    private final FaturaRepository faturaRepository;
    private final CompraCartaoRepository compraCartaoRepository;

    private final CriarCategoriaUseCase criarCategoriaUseCase;
    private final EditarCategoriaUseCase editarCategoriaUseCase;
    private final DeletarCategoriaUseCase deletarCategoriaUseCase;
    private final ReordenarCategoriasUseCase reordenarCategoriasUseCase;
    private final RegistrarLancamentoUseCase registrarLancamentoUseCase;
    private final EditarLancamentoUseCase editarLancamentoUseCase;
    private final DeletarLancamentoUseCase deletarLancamentoUseCase;
    private final ListarLancamentosUseCase listarLancamentosUseCase;
    private final CalcularSaldoDashboardUseCase calcularSaldoDashboardUseCase;
    private final CalcularTotalAportesMesUseCase calcularTotalAportesMesUseCase;
    private final CriarMetaUseCase criarMetaUseCase;
    private final EditarMetaUseCase editarMetaUseCase;
    private final DeletarMetaUseCase deletarMetaUseCase;
    private final RegistrarAporteMetaUseCase registrarAporteMetaUseCase;
    private final DeletarAporteMetaUseCase deletarAporteMetaUseCase;
    private final CalcularProjecaoMetaUseCase calcularProjecaoMetaUseCase;
    private final GerarRelatorioUseCase gerarRelatorioUseCase;
    private final CriarCartaoCreditoUseCase criarCartaoCreditoUseCase;
    private final EditarCartaoCreditoUseCase editarCartaoCreditoUseCase;
    private final DesativarCartaoCreditoUseCase desativarCartaoCreditoUseCase;
    private final ResolverFaturaParaLancamentoUseCase resolverFaturaParaLancamentoUseCase;
    private final RegistrarCompraCartaoUseCase registrarCompraCartaoUseCase;
    private final DeletarCompraCartaoUseCase deletarCompraCartaoUseCase;
    private final GerarCobrancasRecorrentesCartaoUseCase gerarCobrancasRecorrentesCartaoUseCase;
    private final CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase;
    private final ListarComprasCartaoUseCase listarComprasCartaoUseCase;
    private final RegistrarLancamentoCartaoUseCase registrarLancamentoCartaoUseCase;
    private final AtualizarStatusFaturasUseCase atualizarStatusFaturasUseCase;
    private final PagarFaturaUseCase pagarFaturaUseCase;

    CoreServices(
            CategoriaRepository categoriaRepository,
            LancamentoRepository lancamentoRepository,
            TagRepository tagRepository,
            MetaRepository metaRepository,
            AporteMetaRepository aporteMetaRepository,
            CartaoCreditoRepository cartaoRepository,
            FaturaRepository faturaRepository,
            CompraCartaoRepository compraCartaoRepository,
            CriarCategoriaUseCase criarCategoriaUseCase,
            EditarCategoriaUseCase editarCategoriaUseCase,
            DeletarCategoriaUseCase deletarCategoriaUseCase,
            ReordenarCategoriasUseCase reordenarCategoriasUseCase,
            RegistrarLancamentoUseCase registrarLancamentoUseCase,
            EditarLancamentoUseCase editarLancamentoUseCase,
            DeletarLancamentoUseCase deletarLancamentoUseCase,
            ListarLancamentosUseCase listarLancamentosUseCase,
            CalcularSaldoDashboardUseCase calcularSaldoDashboardUseCase,
            CalcularTotalAportesMesUseCase calcularTotalAportesMesUseCase,
            CriarMetaUseCase criarMetaUseCase,
            EditarMetaUseCase editarMetaUseCase,
            DeletarMetaUseCase deletarMetaUseCase,
            RegistrarAporteMetaUseCase registrarAporteMetaUseCase,
            DeletarAporteMetaUseCase deletarAporteMetaUseCase,
            CalcularProjecaoMetaUseCase calcularProjecaoMetaUseCase,
            GerarRelatorioUseCase gerarRelatorioUseCase,
            CriarCartaoCreditoUseCase criarCartaoCreditoUseCase,
            EditarCartaoCreditoUseCase editarCartaoCreditoUseCase,
            DesativarCartaoCreditoUseCase desativarCartaoCreditoUseCase,
            ResolverFaturaParaLancamentoUseCase resolverFaturaParaLancamentoUseCase,
            RegistrarCompraCartaoUseCase registrarCompraCartaoUseCase,
            DeletarCompraCartaoUseCase deletarCompraCartaoUseCase,
            GerarCobrancasRecorrentesCartaoUseCase gerarCobrancasRecorrentesCartaoUseCase,
            CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase,
            ListarComprasCartaoUseCase listarComprasCartaoUseCase,
            RegistrarLancamentoCartaoUseCase registrarLancamentoCartaoUseCase,
            AtualizarStatusFaturasUseCase atualizarStatusFaturasUseCase,
            PagarFaturaUseCase pagarFaturaUseCase) {
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.tagRepository = tagRepository;
        this.metaRepository = metaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
        this.cartaoRepository = cartaoRepository;
        this.faturaRepository = faturaRepository;
        this.compraCartaoRepository = compraCartaoRepository;
        this.criarCategoriaUseCase = criarCategoriaUseCase;
        this.editarCategoriaUseCase = editarCategoriaUseCase;
        this.deletarCategoriaUseCase = deletarCategoriaUseCase;
        this.reordenarCategoriasUseCase = reordenarCategoriasUseCase;
        this.registrarLancamentoUseCase = registrarLancamentoUseCase;
        this.editarLancamentoUseCase = editarLancamentoUseCase;
        this.deletarLancamentoUseCase = deletarLancamentoUseCase;
        this.listarLancamentosUseCase = listarLancamentosUseCase;
        this.calcularSaldoDashboardUseCase = calcularSaldoDashboardUseCase;
        this.calcularTotalAportesMesUseCase = calcularTotalAportesMesUseCase;
        this.criarMetaUseCase = criarMetaUseCase;
        this.editarMetaUseCase = editarMetaUseCase;
        this.deletarMetaUseCase = deletarMetaUseCase;
        this.registrarAporteMetaUseCase = registrarAporteMetaUseCase;
        this.deletarAporteMetaUseCase = deletarAporteMetaUseCase;
        this.calcularProjecaoMetaUseCase = calcularProjecaoMetaUseCase;
        this.gerarRelatorioUseCase = gerarRelatorioUseCase;
        this.criarCartaoCreditoUseCase = criarCartaoCreditoUseCase;
        this.editarCartaoCreditoUseCase = editarCartaoCreditoUseCase;
        this.desativarCartaoCreditoUseCase = desativarCartaoCreditoUseCase;
        this.resolverFaturaParaLancamentoUseCase = resolverFaturaParaLancamentoUseCase;
        this.registrarCompraCartaoUseCase = registrarCompraCartaoUseCase;
        this.deletarCompraCartaoUseCase = deletarCompraCartaoUseCase;
        this.gerarCobrancasRecorrentesCartaoUseCase = gerarCobrancasRecorrentesCartaoUseCase;
        this.cancelarRecorrenciaCartaoUseCase = cancelarRecorrenciaCartaoUseCase;
        this.listarComprasCartaoUseCase = listarComprasCartaoUseCase;
        this.registrarLancamentoCartaoUseCase = registrarLancamentoCartaoUseCase;
        this.atualizarStatusFaturasUseCase = atualizarStatusFaturasUseCase;
        this.pagarFaturaUseCase = pagarFaturaUseCase;
    }

    public CategoriaRepository getCategoriaRepository() { return categoriaRepository; }
    public LancamentoRepository getLancamentoRepository() { return lancamentoRepository; }
    public TagRepository getTagRepository() { return tagRepository; }
    public MetaRepository getMetaRepository() { return metaRepository; }
    public AporteMetaRepository getAporteMetaRepository() { return aporteMetaRepository; }
    public CartaoCreditoRepository getCartaoRepository() { return cartaoRepository; }
    public FaturaRepository getFaturaRepository() { return faturaRepository; }
    public CompraCartaoRepository getCompraCartaoRepository() { return compraCartaoRepository; }

    public CriarCategoriaUseCase getCriarCategoriaUseCase() { return criarCategoriaUseCase; }
    public EditarCategoriaUseCase getEditarCategoriaUseCase() { return editarCategoriaUseCase; }
    public DeletarCategoriaUseCase getDeletarCategoriaUseCase() { return deletarCategoriaUseCase; }
    public ReordenarCategoriasUseCase getReordenarCategoriasUseCase() { return reordenarCategoriasUseCase; }
    public RegistrarLancamentoUseCase getRegistrarLancamentoUseCase() { return registrarLancamentoUseCase; }
    public EditarLancamentoUseCase getEditarLancamentoUseCase() { return editarLancamentoUseCase; }
    public DeletarLancamentoUseCase getDeletarLancamentoUseCase() { return deletarLancamentoUseCase; }
    public ListarLancamentosUseCase getListarLancamentosUseCase() { return listarLancamentosUseCase; }
    public CalcularSaldoDashboardUseCase getCalcularSaldoDashboardUseCase() { return calcularSaldoDashboardUseCase; }
    public CalcularTotalAportesMesUseCase getCalcularTotalAportesMesUseCase() { return calcularTotalAportesMesUseCase; }
    public CriarMetaUseCase getCriarMetaUseCase() { return criarMetaUseCase; }
    public EditarMetaUseCase getEditarMetaUseCase() { return editarMetaUseCase; }
    public DeletarMetaUseCase getDeletarMetaUseCase() { return deletarMetaUseCase; }
    public RegistrarAporteMetaUseCase getRegistrarAporteMetaUseCase() { return registrarAporteMetaUseCase; }
    public DeletarAporteMetaUseCase getDeletarAporteMetaUseCase() { return deletarAporteMetaUseCase; }
    public CalcularProjecaoMetaUseCase getCalcularProjecaoMetaUseCase() { return calcularProjecaoMetaUseCase; }
    public GerarRelatorioUseCase getGerarRelatorioUseCase() { return gerarRelatorioUseCase; }
    public CriarCartaoCreditoUseCase getCriarCartaoCreditoUseCase() { return criarCartaoCreditoUseCase; }
    public EditarCartaoCreditoUseCase getEditarCartaoCreditoUseCase() { return editarCartaoCreditoUseCase; }
    public DesativarCartaoCreditoUseCase getDesativarCartaoCreditoUseCase() { return desativarCartaoCreditoUseCase; }
    public ResolverFaturaParaLancamentoUseCase getResolverFaturaParaLancamentoUseCase() { return resolverFaturaParaLancamentoUseCase; }
    public RegistrarCompraCartaoUseCase getRegistrarCompraCartaoUseCase() { return registrarCompraCartaoUseCase; }
    public DeletarCompraCartaoUseCase getDeletarCompraCartaoUseCase() { return deletarCompraCartaoUseCase; }
    public GerarCobrancasRecorrentesCartaoUseCase getGerarCobrancasRecorrentesCartaoUseCase() {
        return gerarCobrancasRecorrentesCartaoUseCase;
    }
    public CancelarRecorrenciaCartaoUseCase getCancelarRecorrenciaCartaoUseCase() {
        return cancelarRecorrenciaCartaoUseCase;
    }
    public ListarComprasCartaoUseCase getListarComprasCartaoUseCase() { return listarComprasCartaoUseCase; }
    public RegistrarLancamentoCartaoUseCase getRegistrarLancamentoCartaoUseCase() { return registrarLancamentoCartaoUseCase; }
    public AtualizarStatusFaturasUseCase getAtualizarStatusFaturasUseCase() { return atualizarStatusFaturasUseCase; }
    public PagarFaturaUseCase getPagarFaturaUseCase() { return pagarFaturaUseCase; }
}
