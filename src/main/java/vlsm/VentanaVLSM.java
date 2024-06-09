package vlsm;

import VlsmApp.subred;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.StringTokenizer;


public class VentanaVLSM extends Pane {

    private TextField ipField;
    private TextField subnetsField;
    private VBox hostsBox;
    private TextArea resultArea;

    public VentanaVLSM() {
        // Crear los elementos de la interfaz
        ipField = new TextField();
        ipField.setPromptText("10.0.0.0 /8");

        subnetsField = new TextField();
        subnetsField.setPromptText("Ingrese la cantidad de subredes");
        subnetsField.setOnAction(e -> updateHostFields());
        subnetsField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateHostFields();
            }
        });


        hostsBox = new VBox();
        hostsBox.setSpacing(5);

        Button calculateButton = new Button("Calcular");
        calculateButton.setOnAction(e -> calculateSubnets());

        resultArea = new TextArea();
        resultArea.setEditable(false);

        // Desactivar el Focus al iniciar
        ipField.setFocusTraversable(false);
        subnetsField.setFocusTraversable(false);
        calculateButton.setFocusTraversable(false);
        resultArea.setFocusTraversable(false);


        // Configurar el diseño de la interfaz
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(
                createLabel("Direccion IP Principal con Mascara :", Color.BLACK), ipField,
                createLabel("Numero de subredes a crear:", Color.BLACK), subnetsField,
                createLabel("Hosts por subred:", Color.BLACK), hostsBox,
                calculateButton, resultArea
        );
        setBackground(new Background(new BackgroundFill(Color.rgb(125, 125, 125), null, Insets.EMPTY)));


        // Agregar el diseño a este Pane
        getChildren().add(layout);
    }
    private Label createLabel(String text, Color color) {
        Label label = new Label(text);
        label.setTextFill(color);
        return label;
    }
    private void updateHostFields() {
        hostsBox.getChildren().clear();
        int numSubnets;
        try {
            numSubnets = Integer.parseInt(subnetsField.getText());
        } catch (NumberFormatException e) {
            return;
        }
        for (int i = 0; i < numSubnets; i++) {
            TextField hostField = new TextField();
            hostField.setPromptText("Host por subred " + (i + 1));
            hostsBox.getChildren().add(hostField);
        }
    }

    private void calculateSubnets() {
        String mainIp = ipField.getText();
        int numSubnets;
        try {
            numSubnets = Integer.parseInt(subnetsField.getText());
        } catch (NumberFormatException e) {
            resultArea.setText("Numero invalido de subred");
            return;
        }
        int[] nhosts = new int[numSubnets];
        try {
            for (int i = 0; i < numSubnets; i++) {
                TextField hostField = (TextField) hostsBox.getChildren().get(i);
                nhosts[i] = Integer.parseInt(hostField.getText());
            }
        } catch (NumberFormatException e) {
            resultArea.setText("Recuento de hosts no válido en uno de los campos");
            return;
        }

        // Process IP and subnets
        StringTokenizer tokenizer = new StringTokenizer(mainIp, ". /");
        int[] dirIp = new int[5];
        int i;
        for (i = 0; i < dirIp.length && tokenizer.hasMoreElements(); i++) {
            int aux = Integer.parseInt(tokenizer.nextToken());
            if (aux < 0) {
                aux *= -1;
            }
            if (aux > 255) {
                resultArea.setText("Error: Un octeto excede el número máximo");
                return;
            }
            dirIp[i] = aux;
        }
        if (i == 4) {
            resultArea.setText("Error: debe ingresar la máscara /máscara u olvidó un octeto");
            return;
        } else if (i != 5) {
            resultArea.setText("Error: la dirección IP es inválida!!!");
            return;
        }
        if (dirIp[4] == 0 || dirIp[4] > 31) {
            resultArea.setText("Error: Máscara inválida");
            return;
        }

        int ipPrincipal = armarInt(dirIp);
        int mascara = 0;
        for (int j = 0; j < dirIp[4]; j++) {
            mascara += 1 << 31 - j;
        }
        int redPrincipal = ipPrincipal & mascara;
        int nDirsPrincipal = ((int) Math.pow(2, 32 - dirIp[4])) - 2;

        Arrays.sort(nhosts);
        nhosts = reverse(nhosts);

        double bitshost;
        int[] hostbits = new int[nhosts.length];
        int IpVa = redPrincipal;
        subred[] redes = new subred[numSubnets];

        int dirsNecesarias = 0;
        int dirsRequeridas = 0;

        StringBuilder result = new StringBuilder();
        for (int j = 0; j < nhosts.length; j++) {
            bitshost = Math.log10(nhosts[j] + 2) / Math.log10(2);
            if ((bitshost - (int) bitshost) * Math.pow(10, 3) != 0) {
                bitshost = ((int) bitshost) + 1;
            }
            hostbits[j] = (int) bitshost;
            dirsRequeridas += nhosts[j];
            if (dirsNecesarias > nDirsPrincipal) {
                resultArea.setText("ERROR: no hay suficientes direcciones");
                return;
            }
            dirsNecesarias += Math.pow(2, bitshost);
            redes[j] = new subred(IpVa, 32 - hostbits[j]);
            IpVa = redes[j].getBroadcast() + 1;

            subred elem = redes[j];
            result.append("Subred ").append(j + 1).append("\n\tHost requeridos: ").append(nhosts[j]).append("\n\tSe pueden ubicar: ")
                    .append((int) (Math.pow(2, hostbits[j]) - 2)).append("\n\tDirección de subred: ")
                    .append(mostrarIp2(elem.getIpRed())).append("\n\tMáscara de ").append(elem.getNmasc()).append(": ")
                    .append(mostrarIp2(elem.getMascara())).append("\n\tRango asignable: ")
                    .append(mostrarIp2(elem.getIpRed() + 1)).append(" - ")
                    .append(mostrarIp2(elem.getBroadcast() - 1)).append("\n\tBroadcast: ")
                    .append(mostrarIp2(elem.getBroadcast())).append("\n\n");
        }

        result.append("Número de direcciones IP en la red principal: ").append(nDirsPrincipal).append("\n")
                .append("Se requerían: ").append(dirsRequeridas).append("\n")
                .append("Estan disponibles para hosts: ").append(dirsNecesarias - (nhosts.length * 2)).append("\n")
                .append("De todas las direcciones de la principal se está usando el: ")
                .append(((float) dirsNecesarias / (float) nDirsPrincipal) * 100).append("%\n")
                .append("De las direcciones disponibles (en todas las subredes), se usarán (con los requeridos): ")
                .append(((float) dirsRequeridas / (float) dirsNecesarias) * 100).append("%\n");

        resultArea.setText(result.toString());
    }

    private int armarInt(int[] arr) {
        int x;
        x = (int) (arr[0] << 24);
        x += (int) (arr[1] << 16);
        x += (int) (arr[2] << 8);
        x += (int) (arr[3]);
        return x;
    }

    private int[] desArmarInt(int x) {
        int[] arr = new int[4];
        for (int i = 3; i >= 0; i--) {
            if ((byte) x < 0) {
                int aux = x;
                aux = aux & 0x7F;
                arr[i] = (byte) aux;
                arr[i] = arr[i] | 0x80;
            } else
                arr[i] = (byte) x;
            x = x >>> 8;
        }
        return arr;
    }

    private int[] reverse(int[] arr) {
        int[] rev = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            rev[i] = arr[arr.length - 1 - i];
        }
        return rev;
    }

    private String mostrarIp2(int x) {
        int[] arr = desArmarInt(x);
        return arr[0] + "." + arr[1] + "." + arr[2] + "." + arr[3];
    }
}
