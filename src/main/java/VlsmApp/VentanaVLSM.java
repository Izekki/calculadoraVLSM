package VlsmApp;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Arrays;
import java.util.StringTokenizer;


public class VentanaVLSM extends ScrollPane {

    private TextField ipField;
    private TextField subnetsField;
    private VBox hostsBox;
    private TextArea resultArea;

    public VentanaVLSM() {
        //TextFields
        ipField = new TextField();
        ipField.setPromptText("10.0.0.0 /8");

        subnetsField = new TextField();
        subnetsField.setPromptText("Ingrese la cantidad de subredes");
        subnetsField.setOnAction(e -> actualizarCantidadHost());
        subnetsField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Verificar si el campo está vacío antes de llamar a updateHostFields()
                if (!newValue.isEmpty()) {
                    actualizarCantidadHost();
                }
            }
        });


        hostsBox = new VBox();
        hostsBox.setSpacing(5);

        HBox HBotonones = new HBox();
        HBotonones.setSpacing(5);

        Button calcularBoton = new Button("Calcular");
        calcularBoton.setOnAction(e -> calcularSubneteo());

        Button limpiarBoton = new Button("Limpiar");
        limpiarBoton.setOnAction(e -> clearFields());

        HBotonones.getChildren().addAll(calcularBoton,limpiarBoton);
        //TextArea.
        resultArea = new TextArea();
        resultArea.setEditable(false);

        // Desactivar el Focus al iniciar
        ipField.setFocusTraversable(false);
        subnetsField.setFocusTraversable(false);
        calcularBoton.setFocusTraversable(false);
        resultArea.setFocusTraversable(false);

        //Creditos Label
        Font font = Font.font("Helvetica Neue", FontWeight.BOLD, FontPosture.ITALIC, 14);
        Label creditLabel = createLabel("Hecho por: MORALES ROMERO JULIO ALDAIR,\n"+"CORTES CARRILLO, EDGAR YAEL",Color.BLACK);
        creditLabel.setFont(font);

        // Configurar el diseño de la interfaz
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(
                createLabel("Direccion IP Principal con Mascara :", Color.BLACK), ipField,
                createLabel("Numero de subredes a crear:", Color.BLACK), subnetsField,
                createLabel("Hosts por subred:", Color.BLACK), hostsBox,
                HBotonones, resultArea,creditLabel

        );
        setBackground(new Background(new BackgroundFill(Color.rgb(178, 178, 178), null, null)));


        // Agregar el diseño a este Pane
        setContent(layout);
    }
    private Label createLabel(String text, Color color) {
        Font font = Font.font("Arial",FontWeight.BOLD,14);
        Label label = new Label(text);
        label.setTextFill(color);
        label.setFont(font);
        return label;
    }
    private void actualizarCantidadHost() {
        hostsBox.getChildren().clear();
        int numSubnets;
        try {
            numSubnets = Integer.parseInt(subnetsField.getText());
        } catch (NumberFormatException e) {
            showAlert("Número de subred inválido");
            return;
        }
        for (int i = 0; i < numSubnets; i++) {
            TextField hostField = new TextField();
            hostField.setPromptText("Host por subred " + (i + 1));
            hostsBox.getChildren().add(hostField);
        }
    }

    private void calcularSubneteo() {
        String mainIp = ipField.getText();
        int numSubnets;
        try {
            numSubnets = Integer.parseInt(subnetsField.getText());
        } catch (NumberFormatException e) {
            showAlert("Número inválido de subredes");
            return;
        }
        int[] nhosts = new int[numSubnets];
        try {
            for (int i = 0; i < numSubnets; i++) {
                TextField hostField = (TextField) hostsBox.getChildren().get(i);
                nhosts[i] = Integer.parseInt(hostField.getText());
            }
        } catch (NumberFormatException e) {
            showAlert("Recuento de hosts no válido en uno de los campos");
            return;
        }

        // Proceso IP y subneteo
        StringTokenizer tokenizer = new StringTokenizer(mainIp, ". /");
        int[] dirIp = new int[5];
        int i;
        for (i = 0; i < dirIp.length && tokenizer.hasMoreElements(); i++) {
            int aux = Integer.parseInt(tokenizer.nextToken());
            if (aux < 0) {
                aux *= -1;
            }
            if (aux > 255) {
                showAlert("Error: Un octeto excede el número máximo");
                return;
            }
            dirIp[i] = aux;
        }
        if (i == 4) {
            showAlert("Error: debe ingresar la máscara (/x)");
            return;
        } else if (i != 5) {
            showAlert("Error: la dirección IP es inválida!!!");
            return;
        }
        if (dirIp[4] == 0 || dirIp[4] > 31) {
            showAlert("Error: Máscara inválida");
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
                showAlert("ERROR: no hay suficientes direcciones");
                return;
            }
            dirsNecesarias += Math.pow(2, bitshost);
            redes[j] = new subred(IpVa, 32 - hostbits[j]);
            IpVa = redes[j].getBroadcast() + 1;

            //10.0.0.0/8
            subred elem = redes[j];
            result.append("========================================================\n")
                    .append("Subred N°").append(j + 1)
                    .append("\n\tHost requeridos: ").append(nhosts[j]).append("\n\tHost disponibles: ")
                    .append((int) (Math.pow(2, hostbits[j]) - 2)).append("\n\tDirección de subred: ")
                    .append(mostrarIp2(elem.getIpRed())).append("\n\tMáscara de /").append(elem.getNmasc()).append(": ")
                    .append(mostrarIp2(elem.getMascara())).append("\n\tRango de subredes asignadas:")
                    .append("\n\tPrimera Utilizable:")
                    .append(mostrarIp2(elem.getIpRed() + 1))
                    .append("\n\tUltima Utilizable:")
                    .append(mostrarIp2(elem.getBroadcast() - 1)).append("\n\tBroadcast: ")
                    .append(mostrarIp2(elem.getBroadcast())).append("\n\n")
                    .append("========================================================\n");

        }
        String claseRed = determinarClaseRed(dirIp);
        result.append("Clase de la red principal: ").append(claseRed).append("\n")
                .append("Total de posibles direcciones IP en la red principal: ").append(nDirsPrincipal).append("\n")
                .append("El numero total de host ocupados: ").append(dirsRequeridas).append("\n");
        resultArea.setText(result.toString());
    }

    private String determinarClaseRed(int[] dirIp) {
        int primerOcteto = dirIp[0];
        if (primerOcteto >= 0 && primerOcteto <= 127) {
            return "Clase A";
        } else if (primerOcteto >= 128 && primerOcteto <= 191) {
            return "Clase B";
        } else if (primerOcteto >= 192 && primerOcteto <= 223) {
            return "Clase C";
        } else if (primerOcteto >= 224 && primerOcteto <= 239) {
            return "Clase D (Multicast)";
        } else {
            return "Clase E (Experimental)";
        }
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
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        ipField.clear();
        subnetsField.clear();
        hostsBox.getChildren().clear();
        resultArea.clear();
    }
}
