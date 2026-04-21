import br.com.expertdev.ui.ExpertDevGUI;

public class ExpertDev {

    public static void main(String[] args) {
        // Se rodar com argumento --cli, usa o modo linha de comando (legado)
        if (args.length > 0 && "--cli".equals(args[0])) {
            ExpertDev_vr1_5.main(args);
            return;
        }
        // Modo padrão: interface gráfica Swing
        ExpertDevGUI.lancar();
    }
}

