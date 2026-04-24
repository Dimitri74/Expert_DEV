package br.com.expertdev.pro.ui;

import br.com.expertdev.pro.model.IssueContext;
import br.com.expertdev.pro.model.PromptBundle;
import br.com.expertdev.pro.model.ChecklistResult;
import br.com.expertdev.pro.model.AiAssistantTarget;
import br.com.expertdev.pro.workflow.ProWorkflowService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Painel Pro isolado com os 3 botões principais:
 * - Abrir na IDE
 * - Copiar Prompt
 * - Aplicar Checklist
 */
public class ProAssistantPanel extends JPanel {

    private ProWorkflowService workflowService;

    // Cores padrão (alinhadas ao tema claro atual do ExpertDevGUI)
    private static final Color COR_PAINEL = new Color(250, 251, 255);
    private static final Color COR_TEXTO = new Color(15, 23, 42);
    private static final Color COR_SUCESSO = new Color(52, 211, 153);
    private static final Color COR_AVISO = new Color(250, 204, 21);
    private static final Color COR_ERRO = new Color(248, 113, 113);
    private static final Color COR_DESTAQUE = new Color(99, 102, 241);
    private static final Color COR_BOTAO_PRIMARIO = new Color(198, 205, 255);
    private static final Color COR_BOTAO_SUCESSO = new Color(209, 250, 229);
    private static final Color COR_BOTAO_AVISO = new Color(254, 240, 138);
    private static final Color COR_BORDA = new Color(203, 213, 225);
    private static final Color COR_CAMPO = new Color(255, 255, 255);

    private static final Font FONTE_ROTULO = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 11);

    // Componentes
    private JTextField txtArquivoAlvo;
    private JSpinner spinLinhaAlvo;
    private JComboBox<String> comboCategoriaProblema;
    private JComboBox<AiAssistantTarget> comboAssistenteIa;
    private JTextArea txtDescricaoProblema;
    private JButton btnAbrirNaIde;
    private JButton btnCopiarPrompt;
    private JButton btnAplicarChecklist;
    private JLabel lblStatusOperacao;
    private JTextArea txtChecklistResultado;

    private PromptBundle ultimoPromptGerado;
    private ChecklistResult ultimoChecklistResultado;

    public ProAssistantPanel() {
        this.workflowService = new ProWorkflowService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBackground(COR_PAINEL);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(COR_PAINEL);

        conteudo.add(criarSecao("Contexto técnico", criarPainelEntrada()));
        conteudo.add(Box.createVerticalStrut(8));
        conteudo.add(criarSecao("Ações", criarPainelBotoes()));
        conteudo.add(Box.createVerticalStrut(8));
        conteudo.add(criarSecao("Resultado", criarPainelResultado()));

        JScrollPane scrollPane = new JScrollPane(conteudo);
        scrollPane.setBorder(null);
        scrollPane.setBackground(COR_PAINEL);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel criarSecao(String titulo, JComponent conteudo) {
        javax.swing.border.TitledBorder bordaTitulo = BorderFactory.createTitledBorder(new LineBorder(COR_BORDA, 1), titulo);
        bordaTitulo.setTitleColor(COR_TEXTO);
        bordaTitulo.setTitleFont(FONTE_ROTULO);

        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_PAINEL);
        painel.setBorder(BorderFactory.createCompoundBorder(
                bordaTitulo,
                new EmptyBorder(5, 5, 5, 5)
        ));
        painel.add(conteudo, BorderLayout.CENTER);
        painel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return painel;
    }

    private JPanel criarPainelEntrada() {
        JPanel painel = new JPanel();
        painel.setLayout(new GridBagLayout());
        painel.setBackground(COR_PAINEL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Arquivo alvo
        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(criarLabel("Arquivo alvo:"), gbc);

        gbc.gridx = 1;
        txtArquivoAlvo = new JTextField(30);
        estilizarCampo(txtArquivoAlvo);
        txtArquivoAlvo.setToolTipText(
            "<html><b>Arquivo Alvo</b><br>" +
            "Caminho relativo do arquivo contendo o problema.<br>" +
            "<b>Exemplo:</b> src/main/java/br/com/expertdev/ui/RecoveryDialog.java<br>" +
            "<b>Padrão:</b> Use paths Unix (/) mesmo no Windows"
        );
        painel.add(txtArquivoAlvo, gbc);

        // Linha alvo
        gbc.gridx = 2;
        painel.add(criarLabel("Linha:"), gbc);

        gbc.gridx = 3;
        spinLinhaAlvo = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        estilizarSpinner(spinLinhaAlvo);
        spinLinhaAlvo.setToolTipText(
            "<html><b>Número da Linha</b><br>" +
            "Número exato da linha no arquivo onde o problema ocorre.<br>" +
            "<b>Exemplo:</b> 125<br>" +
            "<b>Intervalo:</b> 1 a 999999"
        );
        painel.add(spinLinhaAlvo, gbc);

        // Categoria
        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(criarLabel("Categoria:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        comboCategoriaProblema = new JComboBox<>(
                new String[]{
                        "UI/Layout",
                        "Auth",
                        "Build",
                        "Database",
                        "Performance",
                        "Security",
                        "Tests",
                        "Refactoring",
                        "Regressao",
                        "DB",
                        "Outro"
                }
        );
        estilizarCombo(comboCategoriaProblema);
        comboCategoriaProblema.setToolTipText(
            "<html><b>Categoria do Problema</b><br>" +
            "<b>UI/Layout:</b> Problemas de interface, dimensões, cores, alinhamento<br>" +
            "<b>Auth:</b> Login, autenticação, recuperação de senha, permissões<br>" +
            "<b>Build:</b> Compilação, dependências, Maven, estrutura de projeto<br>" +
            "<b>Database/DB:</b> Banco de dados, queries, conexões, migrações<br>" +
            "<b>Performance:</b> Lentidão, consumo de CPU/memória, gargalos<br>" +
            "<b>Security:</b> SQL injection, exposição de dados, validações<br>" +
            "<b>Tests:</b> Cobertura e qualidade de testes automatizados<br>" +
            "<b>Refactoring:</b> Redução de complexidade e melhoria de design<br>" +
            "<b>Regressao:</b> Garantia de não quebra de fluxos existentes<br>" +
            "<b>Outro:</b> Qualquer problema que não se enquadra nas categorias acima"
        );
        painel.add(comboCategoriaProblema, gbc);

        // Assistente IA alvo
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        painel.add(criarLabel("Assistente IA:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        comboAssistenteIa = new JComboBox<>(AiAssistantTarget.values());
        estilizarCombo(comboAssistenteIa);
        comboAssistenteIa.setToolTipText(
            "<html><b>Assistente IA</b><br>" +
            "Escolha o destino do prompt.<br>" +
            "<b>GitHub Copilot:</b> fluxo padrão para chat do Copilot na IDE<br>" +
            "<b>Junie IA (IntelliJ):</b> mesmo fluxo para o chat Junie no IntelliJ"
        );
        painel.add(comboAssistenteIa, gbc);

        // Descrição problema
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        painel.add(criarLabel("Descrição do problema:"), gbc);

        gbc.gridy = 4;
        txtDescricaoProblema = new JTextArea(3, 50);
        txtDescricaoProblema.setLineWrap(true);
        txtDescricaoProblema.setWrapStyleWord(true);
        estilizarTextArea(txtDescricaoProblema);
        txtDescricaoProblema.setToolTipText(
            "<html><b>Descrição do Problema</b><br>" +
            "Descreva o problema de forma clara e concisa.<br>" +
            "<b>Exemplo:</b> O botão 'Gerar Código' não está habilitado quando deveria estar.<br>" +
            "<b>Dica:</b> Inclua: o que esperava, o que acontece, e como reproduzir"
        );

        JScrollPane scroll = new JScrollPane(txtDescricaoProblema);
        scroll.setBackground(COR_PAINEL);
        painel.add(scroll, gbc);

        return painel;
    }

    private JPanel criarPainelBotoes() {
        JPanel painel = new JPanel();
        painel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        painel.setBackground(COR_PAINEL);

        // Botão Abrir na IDE
        btnAbrirNaIde = criarBotao("Abrir na IDE", COR_BOTAO_PRIMARIO);
        btnAbrirNaIde.setToolTipText("Abre o arquivo e a linha informada na IDE detectada.");
        btnAbrirNaIde.addActionListener(e -> abrirNaIde());
        painel.add(btnAbrirNaIde);

        // Botão Copiar Prompt
        btnCopiarPrompt = criarBotao("Copiar Prompt", COR_BOTAO_SUCESSO);
        btnCopiarPrompt.setToolTipText("Gera o prompt e copia para o assistente selecionado.");
        btnCopiarPrompt.addActionListener(e -> copiarPrompt());
        painel.add(btnCopiarPrompt);

        // Botão Aplicar Checklist
        btnAplicarChecklist = criarBotao("Aplicar Checklist", COR_BOTAO_AVISO);
        btnAplicarChecklist.setToolTipText("Executa a checagem rápida da categoria escolhida.");
        btnAplicarChecklist.addActionListener(e -> aplicarChecklist());
        painel.add(btnAplicarChecklist);

        return painel;
    }

    private JPanel criarPainelResultado() {
        JPanel painel = new JPanel();
        painel.setLayout(new BorderLayout(8, 8));
        painel.setBackground(COR_PAINEL);
        painel.setBorder(new EmptyBorder(2, 2, 2, 2));
        painel.setPreferredSize(new Dimension(0, 150));

        // Status
        lblStatusOperacao = new JLabel("Pronto");
        lblStatusOperacao.setForeground(COR_TEXTO);
        lblStatusOperacao.setFont(FONTE_NORMAL);
        painel.add(lblStatusOperacao, BorderLayout.NORTH);

        // Resultado checklist/prompt
        txtChecklistResultado = new JTextArea(4, 60);
        txtChecklistResultado.setEditable(false);
        estilizarTextArea(txtChecklistResultado);

        JScrollPane scroll = new JScrollPane(txtChecklistResultado);
        scroll.setBackground(COR_PAINEL);
        scroll.setBorder(new LineBorder(COR_BORDA, 1));
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    private void abrirNaIde() {
        try {
            IssueContext contexto = obterContextoDaTela();
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    workflowService.abrirNaIde(contexto);
                    return null;
                }

                protected void done() {
                    try {
                        get();
                        lblStatusOperacao.setText("✓ Arquivo aberto na IDE (" + workflowService.getIdeName() + ")");
                        lblStatusOperacao.setForeground(COR_SUCESSO);
                    } catch (Exception ex) {
                        lblStatusOperacao.setText("✗ Erro: " + ex.getMessage());
                        lblStatusOperacao.setForeground(COR_ERRO);
                    }
                }
            }.execute();
        } catch (Exception ex) {
            lblStatusOperacao.setText("✗ Erro ao preparar contexto");
            lblStatusOperacao.setForeground(COR_ERRO);
        }
    }

    private void copiarPrompt() {
        try {
            IssueContext contexto = obterContextoDaTela();
            String tipoPrompt = (String) comboCategoriaProblema.getSelectedItem();
            AiAssistantTarget assistenteAlvo = obterAssistenteSelecionado();

            ultimoPromptGerado = workflowService.copiarPrompt(contexto, tipoPrompt, assistenteAlvo);

            lblStatusOperacao.setText("✓ Prompt copiado para " + assistenteAlvo.getRotulo() +
                    " (" + ultimoPromptGerado.getTamanhoPrompt() + " caracteres)");
            lblStatusOperacao.setForeground(COR_SUCESSO);

            txtChecklistResultado.setText("Prompt pronto para envio!\n" +
                    "Destino: " + assistenteAlvo.getRotulo() + "\n" +
                    "Próximo passo: " + workflowService.getInstrucaoDestino(assistenteAlvo) + "\n\n" +
                    ultimoPromptGerado.getPromptGerado().substring(0, Math.min(300, ultimoPromptGerado.getPromptGerado().length())) + "...");
        } catch (Exception ex) {
            lblStatusOperacao.setText("✗ Erro ao copiar: " + ex.getMessage());
            lblStatusOperacao.setForeground(COR_ERRO);
        }
    }

    private AiAssistantTarget obterAssistenteSelecionado() {
        Object selecionado = comboAssistenteIa != null ? comboAssistenteIa.getSelectedItem() : null;
        if (selecionado instanceof AiAssistantTarget) {
            return (AiAssistantTarget) selecionado;
        }
        return AiAssistantTarget.COPILOT;
    }

    private void aplicarChecklist() {
        try {
            IssueContext contexto = obterContextoDaTela();
            ultimoChecklistResultado = workflowService.aplicarChecklist(contexto);

            StringBuilder sb = new StringBuilder();
            sb.append("Checklist: ").append(ultimoChecklistResultado.getCategoria()).append("\n");
            sb.append("Score: ").append(ultimoChecklistResultado.getScorePercentual()).append("%\n\n");

            for (ChecklistResult.ChecklistItem item : ultimoChecklistResultado.getItens()) {
                sb.append(item.getStatus().getLabel()).append(" ").append(item.getTitulo()).append("\n");
            }

            txtChecklistResultado.setText(sb.toString());

            if (ultimoChecklistResultado.isCompleto()) {
                lblStatusOperacao.setText("✓ Checklist completo! Score: " + ultimoChecklistResultado.getScorePercentual() + "%");
                lblStatusOperacao.setForeground(COR_SUCESSO);
            } else {
                lblStatusOperacao.setText("⚠ Checklist parcial. Items críticos pendentes: " + ultimoChecklistResultado.getItensCriticosPendentes());
                lblStatusOperacao.setForeground(COR_AVISO);
            }
        } catch (Exception ex) {
            lblStatusOperacao.setText("✗ Erro ao aplicar checklist: " + ex.getMessage());
            lblStatusOperacao.setForeground(COR_ERRO);
        }
    }

    private IssueContext obterContextoDaTela() {
        IssueContext contexto = new IssueContext();
        contexto.setArquivoAlvo(txtArquivoAlvo.getText());
        contexto.setLinhaAlvo((int) spinLinhaAlvo.getValue());
        contexto.setDescricaoProblema(txtDescricaoProblema.getText());
        contexto.setCategoria((String) comboCategoriaProblema.getSelectedItem());
        return contexto;
    }

    // Helper methods para estilização

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(COR_TEXTO);
        label.setFont(FONTE_ROTULO);
        return label;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setBackground(COR_CAMPO);
        campo.setForeground(COR_TEXTO);
        campo.setFont(FONTE_NORMAL);
        campo.setBorder(new LineBorder(COR_BORDA, 1));
        campo.setCaretColor(COR_DESTAQUE);
    }

    private void estilizarSpinner(JSpinner spinner) {
        spinner.setBackground(COR_CAMPO);
        spinner.setForeground(COR_TEXTO);
        spinner.setFont(FONTE_NORMAL);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField field = ((JSpinner.DefaultEditor) editor).getTextField();
            field.setBackground(COR_CAMPO);
            field.setForeground(COR_TEXTO);
            field.setBorder(new LineBorder(COR_BORDA, 1));
            field.setCaretColor(COR_DESTAQUE);
        }
    }

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setBackground(COR_CAMPO);
        combo.setForeground(COR_TEXTO);
        combo.setFont(FONTE_NORMAL);
        combo.setBorder(new LineBorder(COR_BORDA, 1));
    }

    private void estilizarTextArea(JTextArea area) {
        area.setBackground(COR_CAMPO);
        area.setForeground(COR_TEXTO);
        area.setFont(FONTE_NORMAL);
        area.setBorder(new LineBorder(COR_BORDA, 1));
        area.setCaretColor(COR_DESTAQUE);
    }

    private JButton criarBotao(String texto, Color cor) {
        JButton btn = new JButton(texto);
        btn.setFont(FONTE_BOTAO);
        btn.setBackground(cor);
        btn.setForeground(obterCorTexto(cor));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(aumentarBrilho(cor, 0.92f), 1));
        btn.setPreferredSize(new Dimension(160, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(aumentarBrilho(cor, 1.15f));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(cor);
            }
        });

        return btn;
    }

    private Color obterCorTexto(Color cor) {
        int luminancia = (int) (0.299 * cor.getRed() + 0.587 * cor.getGreen() + 0.114 * cor.getBlue());
        return luminancia > 128 ? Color.BLACK : Color.WHITE;
    }

    private Color aumentarBrilho(Color cor, float fator) {
        int r = Math.min(255, (int) (cor.getRed() * fator));
        int g = Math.min(255, (int) (cor.getGreen() * fator));
        int b = Math.min(255, (int) (cor.getBlue() * fator));
        return new Color(r, g, b);
    }
}
