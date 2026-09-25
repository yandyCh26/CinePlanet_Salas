package pe.edu.upeu.sysventas.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.components.ToltipCustom;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.service.IProductoService;

import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ProductoController {

    private final IProductoService ps;

    @FXML
    TextField txtNombreProducto;
    @FXML
    private TableView<Producto> tableView;
    @FXML
    Label lbnMsg;
    @FXML
    private AnchorPane miContenedor;

    ObservableList<Producto> listarProducto;

    Producto formulario;

    Long idProductoCE = 0L;

    Stage stage;

    private Validator validator;

    private final ToltipCustom ttc = new ToltipCustom();


    @FXML
    public void initialize() {

        System.out.println("ProductoController iniciado");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        TableViewHelper<Producto> tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Prod.", new ColumnInfo("idProducto", 80.0)
        );

        columns.put(
                "Nombre", new ColumnInfo("nombre", 250.0)
        );

        Consumer<Producto> updateAction = producto -> {
            editForm(producto);
            idProductoCE = producto.getIdProducto();
        };
        Consumer<Producto> deleteAction = producto -> {
            ps.delete(producto.getIdProducto());
            Stage stage = (Stage) miContenedor.getScene().getWindow();
            double w = stage.getWidth() / 1.5;
            double h = stage.getHeight() / 2;
            Toast.showToast(stage, "Se eliminó correctamente!!", 2000, w, h);
            listar();
        };

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns, updateAction, deleteAction);
        tableView.setTableMenuButtonVisible(true);
        listar();
    }


    public void listar() {

        try {
            tableView.getItems().clear();
            listarProducto = FXCollections.observableArrayList(ps.findAll());
            tableView.getItems().addAll(listarProducto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void setStage(Stage stage) {

        this.stage = stage;

        System.out.println(
                "Llego " + stage.getTitle());
    }

    @FXML
    public void validarFormulario() {

        formulario = new Producto();

        formulario.setNombre(
                txtNombreProducto.getText()
        );

        Set<ConstraintViolation<Producto>> violaciones = validator.validate(formulario);

        List<ConstraintViolation<Producto>> violacionesOrdenadas = violaciones.stream().sorted(Comparator.comparing(v -> v.getPropertyPath().toString())).toList();
        if (violacionesOrdenadas.isEmpty()) {
            procesarFormulario();
        } else {
            mostrarErroresValidacion(violacionesOrdenadas);}}
    private void mostrarErroresValidacion(
            List<ConstraintViolation<Producto>> violaciones) {

        limpiarError();
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombre", txtNombreProducto);
        LinkedHashMap<String, String> erroresOrdenados = new LinkedHashMap<>();
        final Control[] primerCtrl = {null};

        for (String campo : campos.keySet()) {
            violaciones.stream().filter(v -> v.getPropertyPath().toString().equals(campo)).findFirst().ifPresent(v -> {erroresOrdenados.put(campo, v.getMessage());
                        Control c = campos.get(campo);
                        if (c != null) {
                            ttc.marcarError(c, v.getMessage().trim());
                        }
                        if (primerCtrl[0] == null) {

                            primerCtrl[0] = c;
                        }
                    });
        }


        if (!erroresOrdenados.isEmpty()) {

            lbnMsg.setText(
                    erroresOrdenados
                            .entrySet()
                            .iterator()
                            .next()
                            .getValue()
            );

            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");

            if (primerCtrl[0] != null) {
                Platform.runLater(primerCtrl[0]::requestFocus);
            }
        }
    }


    private void procesarFormulario() {
        lbnMsg.setText("Formulario válido");
        lbnMsg.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        Stage stage = (Stage) miContenedor.getScene().getWindow();
        limpiarError();
        double w = stage.getWidth() / 1.5;
        double h = stage.getHeight() / 2;
        if (idProductoCE > 0L) {
            formulario.setIdProducto(idProductoCE);
            ps.update(idProductoCE, formulario
            );
            Toast.showToast(stage, "Se actualizó correctamente!!", 2000, w, h);
        } else {
            ps.save(formulario);
            Toast.showToast(stage, "Se guardó correctamente!!", 2000, w, h);
        }
        clearForm();
        listar();
    }
    public void editForm(Producto producto) {
        txtNombreProducto.setText(producto.getNombre());
        idProductoCE = producto.getIdProducto();
        limpiarError();
    }
    public void limpiarError() {
        ttc.limpiarCampo(txtNombreProducto);
        txtNombreProducto.getStyleClass().remove("text-field-error");
    }

    public void clearForm() {
        txtNombreProducto.clear();
        idProductoCE = 0L;
        limpiarError();
    }
}
