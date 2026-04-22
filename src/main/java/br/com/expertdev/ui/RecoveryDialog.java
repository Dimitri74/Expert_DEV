package br.com.expertdev.ui;

import br.com.expertdev.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RecoveryDialog extends JDialog {

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

    public RecoveryDialog(Window owner, AuthService authService) {
        super(owner, "Recuperar acesso", ModalityType.APPLICATION_MODAL);
        this.authService = authService;
        construir();
    }

    private void construir() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COR_FUNDO);

        // ─── Painel superior com título ────────────────────────────────────────
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        painelTopo.setBackground(COR_PAINEL_TOPO);
        painelTopo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA));

        JLabel lblTitulo = new JLabel("Recuperar acesso");
        lblTitulo.setFont(FONTE_TITULO);
        lblTitulo.setForeground(COR_AZUL);
        painelTopo.add(lblTitulo);

        // ─── Painel de formulário ──────────────────────────────────────────────
        JPanel formulario = new JPanel(new GridLayout(0, 2, 20, 15));
        formulario.setBackground(COR_PAINEL);
        formulario.setBorder(new EmptyBorder(25, 40, 25, 40));

        txtIdentity = criarCampoTexto();
        txtCodigo = criarCampoTexto();
        txtNovaSenha = criarCampoSenha();
        txtConfirmarSenha = criarCampoSenha();

        JLabel[] labels = {
                criarLabel("Usuário ou email:"),
                criarLabel("Código de recuperação:"),
                criarLabel("Nova senha:"),
                criarLabel("Confirmar senha:")
        };

        formulario.add(labels[0]);           formulario.add(txtIdentity);
        formulario.add(labels[1]);           formulario.add(txtCodigo);
        formulario.add(labels[2]);           formulario.add(txtNovaSenha);
        formulario.add(labels[3]);           formulario.add(txtConfirmarSenha);

        // ─── Painel de botões ──────────────────────────────────────────────────
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        botoes.setBackground(COR_FUNDO);
        botoes.setBorder(new EmptyBorder(15, 20, 20, 20));

        JButton btnGerarCodigo = criarBotao("Gerar código", COR_AMARELO_CLARO);
        JButton btnRedefinir = criarBotao("Redefinir senha", COR_VERDE_CLARO);
        JButton btnFechar = criarBotao("Fechar", COR_VERMELHO_CLARO);

        btnGerarCodigo.addActionListener(e -> gerarCodigo());
        btnRedefinir.addActionListener(e -> redefinirSenha());
        btnFechar.addActionListener(e -> dispose());

        botoes.add(btnGerarCodigo);
        botoes.add(btnRedefinir);
        botoes.add(btnFechar);

        add(painelTopo, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        setSize(750, 340);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    // ─── Métodos auxiliares para criação de componentes ──────────────────────

    private JTextField criarCampoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(FONTE_NORMAL);
        campo.setBackground(new Color(248, 250, 252));
        campo.setForeground(COR_TEXTO);
        campo.setCaretColor(COR_AZUL);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        campo.setPreferredSize(new Dimension(250, 35));
        return campo;
    }

    private JPasswordField criarCampoSenha() {
        JPasswordField campo = new JPasswordField();
        campo.setFont(FONTE_NORMAL);
        campo.setBackground(new Color(248, 250, 252));
        campo.setForeground(COR_TEXTO);
        campo.setCaretColor(COR_AZUL);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        campo.setPreferredSize(new Dimension(250, 35));
        return campo;
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

        botao.setPreferredSize(new Dimension(140, 40));
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
        String identity = txtIdentity.getText();
        String codigo = authService.gerarCodigoReset(identity);
        String username = authService.obterUsernamePorIdentificador(identity);
        if (codigo == null || username == null) {
            JOptionPane.showMessageDialog(this,
                    "Usuario/email nao encontrado.",
                    "Recuperacao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Usuario encontrado: " + username + "\n"
                        + "Codigo de recuperacao: " + codigo + "\n"
                        + "Validade: 15 minutos.",
                "Codigo gerado",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void redefinirSenha() {
        String senha = new String(txtNovaSenha.getPassword());
        String confirmar = new String(txtConfirmarSenha.getPassword());
        if (!senha.equals(confirmar)) {
            JOptionPane.showMessageDialog(this,
                    "A confirmacao da senha nao confere.",
                    "Recuperacao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String erro = authService.redefinirSenha(txtIdentity.getText(), txtCodigo.getText(), senha);
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
}

