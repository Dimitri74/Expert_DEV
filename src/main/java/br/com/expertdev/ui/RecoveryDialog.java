package br.com.expertdev.ui;

import br.com.expertdev.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class RecoveryDialog extends JDialog {

    private enum VarianteVisual {
        COMPACTO,
        INTERMEDIARIO
    }

    // Troque para COMPACTO para testar a versão mais densa.
    private static final VarianteVisual VARIANTE_VISUAL_ATIVA = VarianteVisual.INTERMEDIARIO;

    // ─── Cores do projeto (SEM TEMA-DARK) ──────────────────────────────────
    private static final Color COR_FUNDO = new Color(245, 247, 251);
    private static final Color COR_PAINEL = new Color(255, 255, 255);
    private static final Color COR_PAINEL_TOPO = new Color(240, 244, 249);
    private static final Color COR_AZUL = new Color(79, 70, 229);
    private static final Color COR_VERMELHO_CLARO = new Color(239, 68, 68);
    private static final Color COR_AMARELO_CLARO = new Color(245, 158, 11);
    private static final Color COR_VERDE_CLARO = new Color(34, 197, 94);
    private static final Color COR_TEXTO = new Color(15, 23, 42);
    private static final Color COR_TEXTO_SUAVE = new Color(71, 85, 105);
    private static final Color COR_BORDA = new Color(203, 213, 225);

    // ─── Fontes ───────────────────────────────────────────────────────────────
    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONTE_ROTULO = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 12);

    private final AuthService authService;
    private JTextField txtIdentity;
    private JTextField txtCodigo;
    private JPasswordField txtNovaSenha;
    private JPasswordField txtConfirmarSenha;
    private JButton btnGerarCodigo;
    private JButton btnRedefinir;
    private JButton btnFechar;
    private JLabel lblStatus;

    private int larguraDialogo;
    private int alturaDialogo;
    private int paddingFormTopo;
    private int paddingFormLateral;
    private int paddingFormBase;
    private int espacamentoHorizontal;
    private int espacamentoVertical;
    private int larguraCampo;
    private int larguraBotao;

    public RecoveryDialog(Window owner, AuthService authService) {
        super(owner, "Recuperar acesso", ModalityType.APPLICATION_MODAL);
        this.authService = authService;
        aplicarVarianteVisual(VARIANTE_VISUAL_ATIVA);
        construir();
    }

    private void aplicarVarianteVisual(VarianteVisual variante) {
        if (variante == VarianteVisual.COMPACTO) {
            larguraDialogo = 620;
            alturaDialogo = 340;
            paddingFormTopo = 14;
            paddingFormLateral = 20;
            paddingFormBase = 8;
            espacamentoHorizontal = 12;
            espacamentoVertical = 8;
            larguraCampo = 250;
            larguraBotao = 146;
            return;
        }

        larguraDialogo = 660;
        alturaDialogo = 360;
        paddingFormTopo = 18;
        paddingFormLateral = 24;
        paddingFormBase = 10;
        espacamentoHorizontal = 14;
        espacamentoVertical = 10;
        larguraCampo = 270;
        larguraBotao = 154;
    }

    private void construir() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COR_FUNDO);

        // ─── Painel superior com título ────────────────────────────────────────
        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setBackground(COR_PAINEL_TOPO);
        painelTopo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA));

        JLabel lblTitulo = new JLabel("Recuperar acesso", SwingConstants.CENTER);
        lblTitulo.setFont(FONTE_TITULO);
        lblTitulo.setForeground(COR_AZUL);
        lblTitulo.setBorder(new EmptyBorder(15, 0, 15, 0));
        painelTopo.add(lblTitulo, BorderLayout.CENTER);

        // ─── Painel de formulário ──────────────────────────────────────────────
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(COR_PAINEL);
        formulario.setBorder(new EmptyBorder(paddingFormTopo, paddingFormLateral, paddingFormBase, paddingFormLateral));

        txtIdentity = criarCampoTexto();
        txtCodigo = criarCampoTexto();
        txtNovaSenha = criarCampoSenha();
        txtConfirmarSenha = criarCampoSenha();

        adicionarLinhaFormulario(formulario, 0, "Usuário ou email:", txtIdentity);
        adicionarLinhaFormulario(formulario, 1, "Código de recuperação:", txtCodigo);
        adicionarLinhaFormulario(formulario, 2, "Nova senha:", txtNovaSenha);
        adicionarLinhaFormulario(formulario, 3, "Confirmar senha:", txtConfirmarSenha);

        GridBagConstraints gbcExpansor = new GridBagConstraints();
        gbcExpansor.gridx = 2;
        gbcExpansor.gridy = 0;
        gbcExpansor.gridheight = 4;
        gbcExpansor.weightx = 1;
        gbcExpansor.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(Box.createHorizontalStrut(1), gbcExpansor);

        // ─── Painel de botões ──────────────────────────────────────────────────
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        botoes.setBackground(COR_FUNDO);
        botoes.setBorder(new EmptyBorder(8, 16, 12, 16));

        btnGerarCodigo = criarBotao("Gerar código", COR_AMARELO_CLARO);
        btnRedefinir = criarBotao("Redefinir senha", COR_VERDE_CLARO);
        btnFechar = criarBotao("Fechar", COR_VERMELHO_CLARO);

        btnGerarCodigo.addActionListener(e -> gerarCodigo());
        btnRedefinir.addActionListener(e -> redefinirSenha());
        btnFechar.addActionListener(e -> dispose());

        botoes.add(btnGerarCodigo);
        botoes.add(btnRedefinir);
        botoes.add(btnFechar);

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(FONTE_NORMAL);
        lblStatus.setForeground(COR_TEXTO_SUAVE);
        lblStatus.setBorder(new EmptyBorder(0, 20, 3, 20));

        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.setBackground(COR_FUNDO);
        painelInferior.add(lblStatus, BorderLayout.NORTH);
        painelInferior.add(botoes, BorderLayout.SOUTH);

        add(painelTopo, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);

        pack();
        setSize(new Dimension(larguraDialogo, alturaDialogo));
        setMinimumSize(new Dimension(larguraDialogo, alturaDialogo));
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> txtIdentity.requestFocusInWindow());
            }
        });
    }

    // ─── Métodos auxiliares para criação de componentes ──────────────────────

    private JTextField criarCampoTexto() {
        JTextField campo = new JTextField();
        return aplicarPadraoCampo(campo);
    }

    private JPasswordField criarCampoSenha() {
        JPasswordField campo = new JPasswordField();
        return aplicarPadraoCampo(campo);
    }

    private <T extends JTextField> T aplicarPadraoCampo(T campo) {
        campo.setFont(FONTE_NORMAL);
        campo.setBackground(new Color(248, 250, 252));
        campo.setForeground(COR_TEXTO);
        campo.setCaretColor(COR_AZUL);
        campo.setEditable(true);
        campo.setEnabled(true);
        campo.setFocusable(true);
        campo.setColumns(24);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        campo.setPreferredSize(new Dimension(larguraCampo, 34));
        campo.setMinimumSize(new Dimension(larguraCampo, 34));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        return campo;
    }

    private void adicionarLinhaFormulario(JPanel formulario, int linha, String textoLabel, JComponent campo) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = linha;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(0, 0, espacamentoVertical, espacamentoHorizontal);
        gbcLabel.weightx = 0;

        GridBagConstraints gbcCampo = new GridBagConstraints();
        gbcCampo.gridx = 1;
        gbcCampo.gridy = linha;
        gbcCampo.anchor = GridBagConstraints.WEST;
        gbcCampo.fill = GridBagConstraints.NONE;
        gbcCampo.insets = new Insets(0, 0, espacamentoVertical, 0);
        gbcCampo.weightx = 0;

        formulario.add(criarLabel(textoLabel), gbcLabel);
        formulario.add(campo, gbcCampo);
    }

    private JButton criarBotao(String texto, Color cor) {
        JButton botao = new JButton(texto);
        botao.setFont(FONTE_BOTAO);
        botao.setBackground(cor);

        // Determinar cor de texto baseado em contraste
        Color corTexto = obterCorTextoParaBotao(cor);
        botao.setForeground(corTexto);

        // Forçar renderização correta
        botao.setContentAreaFilled(true);
        botao.setOpaque(true);
        botao.setBorderPainted(true);
        botao.setBorder(BorderFactory.createLineBorder(cor, 2));
        botao.setFocusPainted(false);

        botao.setPreferredSize(new Dimension(larguraBotao, 40));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efeito hover
        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(aumentarBrilho(cor, 1.1f));
                botao.setForeground(corTexto);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(cor);
                botao.setForeground(corTexto);
            }
        });

        return botao;
    }

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_ROTULO);
        label.setForeground(COR_TEXTO);
        return label;
    }

    private Color obterCorTextoParaBotao(Color cor) {
        if (cor.equals(COR_AMARELO_CLARO) || cor.equals(COR_VERDE_CLARO) || cor.equals(COR_VERMELHO_CLARO) || cor.equals(COR_AZUL)) {
            return new Color(15, 23, 42);
        }

        // Calcular luminância percebida (fórmula WCAG)
        double luminancia = (0.299 * cor.getRed() + 0.587 * cor.getGreen() + 0.114 * cor.getBlue()) / 255.0;
        // Se luminância > 0.5, cor é clara -> texto escuro; caso contrário, texto branco
        if (luminancia > 0.5) {
            return new Color(15, 23, 42);  // Preto/Cinzento escuro
        } else {
            return Color.WHITE;  // Branco puro
        }
    }

    private Color aumentarBrilho(Color cor, float fator) {
        int r = Math.min(255, (int) (cor.getRed() * fator));
        int g = Math.min(255, (int) (cor.getGreen() * fator));
        int b = Math.min(255, (int) (cor.getBlue() * fator));
        return new Color(r, g, b);
    }

    private void gerarCodigo() {
        String identity = txtIdentity.getText() == null ? "" : txtIdentity.getText().trim();
        if (identity.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe seu usuário ou email para gerar o código.",
                    "Recuperação",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        setGeracaoEmAndamento(true);

        SwingWorker<ResultadoGeracaoCodigo, Void> worker = new SwingWorker<ResultadoGeracaoCodigo, Void>() {
            @Override
            protected ResultadoGeracaoCodigo doInBackground() {
                String codigo = authService.gerarCodigoReset(identity);
                String username = authService.obterUsernamePorIdentificador(identity);
                return new ResultadoGeracaoCodigo(username, codigo);
            }

            @Override
            protected void done() {
                setGeracaoEmAndamento(false);
                try {
                    ResultadoGeracaoCodigo resultado = get();
                    if (resultado.codigo == null || resultado.username == null) {
                        JOptionPane.showMessageDialog(RecoveryDialog.this,
                                "Usuário ou email não encontrado no sistema.",
                                "Recuperação",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    txtCodigo.setText(resultado.codigo);
                    JOptionPane.showMessageDialog(RecoveryDialog.this,
                            "Usuário encontrado: " + resultado.username + "\n"
                                    + "Código de recuperação: " + resultado.codigo + "\n"
                                    + "O código foi preenchido automaticamente para sua conveniência.\n"
                                    + "Validade: 15 minutos.",
                            "Código Gerado",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(RecoveryDialog.this,
                            "Operação interrompida. Tente novamente.",
                            "Recuperação",
                            JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ex) {
                    String mensagem = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(RecoveryDialog.this,
                            "Erro ao gerar código: " + mensagem,
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void setGeracaoEmAndamento(boolean emAndamento) {
        setCursor(emAndamento ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        btnGerarCodigo.setEnabled(!emAndamento);
        btnRedefinir.setEnabled(!emAndamento);
        btnFechar.setEnabled(!emAndamento);
        lblStatus.setText(emAndamento ? "Gerando código de recuperação..." : " ");
    }

    private void redefinirSenha() {
        String identity = txtIdentity.getText() == null ? "" : txtIdentity.getText().trim();
        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        String senha = new String(txtNovaSenha.getPassword());
        String confirmar = new String(txtConfirmarSenha.getPassword());

        if (identity.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe seu usuário ou email.",
                    "Recuperação",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe o código de recuperação.",
                    "Recuperação",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!senha.equals(confirmar)) {
            JOptionPane.showMessageDialog(this,
                    "A confirmacao da senha nao confere.",
                    "Recuperacao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String erro = authService.redefinirSenha(identity, codigo, senha);
        if (erro != null) {
            JOptionPane.showMessageDialog(this, erro, "Recuperacao", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Senha atualizada com sucesso.",
                "Recuperacao",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private static class ResultadoGeracaoCodigo {
        private final String username;
        private final String codigo;

        private ResultadoGeracaoCodigo(String username, String codigo) {
            this.username = username;
            this.codigo = codigo;
        }
    }
}

