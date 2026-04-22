package br.com.expertdev.ui;

import br.com.expertdev.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class RecoveryDialog extends JDialog {

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
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        txtIdentity = new JTextField();
        txtCodigo = new JTextField();
        txtNovaSenha = new JPasswordField();
        txtConfirmarSenha = new JPasswordField();

        formulario.add(new JLabel("Usuario ou email:"));
        formulario.add(txtIdentity);
        formulario.add(new JLabel("Codigo de recuperacao:"));
        formulario.add(txtCodigo);
        formulario.add(new JLabel("Nova senha:"));
        formulario.add(txtNovaSenha);
        formulario.add(new JLabel("Confirmar senha:"));
        formulario.add(txtConfirmarSenha);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGerarCodigo = new JButton("Gerar codigo");
        JButton btnRedefinir = new JButton("Redefinir senha");
        JButton btnFechar = new JButton("Fechar");

        btnGerarCodigo.addActionListener(e -> gerarCodigo());
        btnRedefinir.addActionListener(e -> redefinirSenha());
        btnFechar.addActionListener(e -> dispose());

        botoes.add(btnGerarCodigo);
        botoes.add(btnRedefinir);
        botoes.add(btnFechar);

        add(formulario, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        setSize(520, 250);
        setLocationRelativeTo(getOwner());
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

