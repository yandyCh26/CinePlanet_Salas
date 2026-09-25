package pe.edu.upeu.sysventas.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.model.Sala;
import pe.edu.upeu.sysventas.service.ISalaService;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class SalaController {

    private final ISalaService ss;

    @FXML
    private TableView<Sala> tableView;

    @FXML
    private AnchorPane miContenedor;

    @FXML
    private TextField txtBuscar;

    private ObservableList<Sala> listarSala;

    private FilteredList<Sala> salasFiltradas;

    private Validator validator;

    private Stage stage;


    // ============================================================
    // INICIALIZAR
    // ============================================================

    @FXML
    public void initialize() {

        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();

        TableViewHelper<Sala> tableViewHelper =
                new TableViewHelper<>();

        LinkedHashMap<String, ColumnInfo> columns =
                new LinkedHashMap<>();

        columns.put(
                "ID",
                new ColumnInfo("idSala", 70.0)
        );

        columns.put(
                "N° SALA",
                new ColumnInfo("numero", 100.0)
        );

        columns.put(
                "CAPACIDAD",
                new ColumnInfo("capacidad", 120.0)
        );

        columns.put(
                "TIPO",
                new ColumnInfo("tipo", 150.0)
        );

        Consumer<Sala> updateAction =
                sala -> abrirFormulario(sala);

        Consumer<Sala> deleteAction =
                sala -> eliminarSala(sala);

        tableViewHelper.addColumnsInOrderWithSize(
                tableView,
                columns,
                updateAction,
                deleteAction
        );

        tableView.setTableMenuButtonVisible(true);

        listar();
    }


    // ============================================================
    // LISTAR
    // ============================================================

    public void listar() {

        try {

            listarSala =
                    FXCollections.observableArrayList(
                            ss.findAll()
                    );

            salasFiltradas =
                    new FilteredList<>(
                            listarSala,
                            p -> true
                    );

            tableView.setItems(salasFiltradas);

            configurarBusqueda();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al listar las salas",
                    e
            );
        }
    }


    // ============================================================
    // BUSQUEDA AUTOMATICA
    // ============================================================

    private void configurarBusqueda() {

        if (txtBuscar == null || salasFiltradas == null) {
            return;
        }

        txtBuscar.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    String texto =
                            newValue == null
                                    ? ""
                                    : newValue.trim().toLowerCase();

                    salasFiltradas.setPredicate(
                            sala -> {

                                if (texto.isEmpty()) {
                                    return true;
                                }

                                return String.valueOf(
                                        sala.getNumero()
                                ).contains(texto)

                                        ||

                                        String.valueOf(
                                                sala.getCapacidad()
                                        ).contains(texto)

                                        ||

                                        (
                                                sala.getTipo() != null
                                                        &&
                                                        sala.getTipo()
                                                                .toLowerCase()
                                                                .contains(texto)
                                        );
                            }
                    );
                }
        );
    }


    // ============================================================
    // BOTON BUSCAR
    // ============================================================

    @FXML
    public void buscarSala() {

        String texto =
                txtBuscar.getText()
                        .trim()
                        .toLowerCase();

        salasFiltradas.setPredicate(
                sala -> {

                    if (texto.isEmpty()) {
                        return true;
                    }

                    return String.valueOf(
                            sala.getNumero()
                    ).contains(texto)

                            ||

                            String.valueOf(
                                    sala.getCapacidad()
                            ).contains(texto)

                            ||

                            (
                                    sala.getTipo() != null
                                            &&
                                            sala.getTipo()
                                                    .toLowerCase()
                                                    .contains(texto)
                            );
                }
        );
    }


    // ============================================================
    // NUEVA SALA
    // ============================================================

    @FXML
    public void nuevaSala() {

        abrirFormulario(null);
    }


    // ============================================================
    // EDITAR SALA
    // ============================================================

    @FXML
    public void editarSala() {

        Sala salaSeleccionada =
                tableView
                        .getSelectionModel()
                        .getSelectedItem();

        if (salaSeleccionada == null) {

            mostrarAlerta(
                    "Selecciona una sala",
                    "Debes seleccionar una sala de la tabla para editarla."
            );

            return;
        }

        abrirFormulario(salaSeleccionada);
    }


    // ============================================================
    // ELIMINAR SALA
    // ============================================================

    @FXML
    public void eliminarSala() {

        Sala salaSeleccionada =
                tableView
                        .getSelectionModel()
                        .getSelectedItem();

        if (salaSeleccionada == null) {

            mostrarAlerta(
                    "Selecciona una sala",
                    "Debes seleccionar una sala de la tabla para eliminarla."
            );

            return;
        }

        eliminarSala(salaSeleccionada);
    }


    // ============================================================
    // ELIMINAR SALA SELECCIONADA
    // ============================================================

    private void eliminarSala(Sala sala) {

        Alert confirmacion =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacion.setTitle(
                "Eliminar sala"
        );

        confirmacion.setHeaderText(
                "¿Deseas eliminar la sala "
                        + sala.getNumero()
                        + "?"
        );

        confirmacion.setContentText(
                "Esta acción eliminará el registro."
        );

        if (
                confirmacion.showAndWait()
                        .orElse(ButtonType.CANCEL)
                        == ButtonType.OK
        ) {

            ss.delete(
                    sala.getIdSala()
            );

            Stage ventana =
                    (Stage) miContenedor
                            .getScene()
                            .getWindow();

            Toast.showToast(
                    ventana,
                    "Sala eliminada correctamente",
                    2000,
                    ventana.getWidth() / 1.5,
                    ventana.getHeight() / 2
            );

            listar();
        }
    }


    // ============================================================
    // FORMULARIO NUEVA / EDITAR SALA
    // ============================================================

    private void abrirFormulario(Sala salaExistente) {

        Dialog<Sala> dialog =
                new Dialog<>();

        dialog.setTitle(
                salaExistente == null
                        ? "Nueva sala"
                        : "Editar sala"
        );

        dialog.setHeaderText(
                salaExistente == null
                        ? "Registrar una nueva sala"
                        : "Modificar datos de la sala"
        );


        // --------------------------------------------------------
        // BOTONES DEL DIALOGO
        // --------------------------------------------------------

        ButtonType guardarButton =
                new ButtonType(
                        salaExistente == null
                                ? "Guardar"
                                : "Actualizar",
                        ButtonBar.ButtonData.OK_DONE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        guardarButton,
                        ButtonType.CANCEL
                );


        // --------------------------------------------------------
        // CAMPOS
        // --------------------------------------------------------

        TextField txtNumero =
                new TextField();

        TextField txtCapacidad =
                new TextField();

        TextField txtTipo =
                new TextField();


        txtNumero.setPromptText(
                "Ej. 1"
        );

        txtCapacidad.setPromptText(
                "Ej. 100"
        );

        txtTipo.setPromptText(
                "Ej. 2D, 3D, XD"
        );



        if (salaExistente != null) {

            txtNumero.setText(
                    String.valueOf(
                            salaExistente.getNumero()
                    )
            );

            txtCapacidad.setText(
                    String.valueOf(
                            salaExistente.getCapacidad()
                    )
            );

            txtTipo.setText(
                    salaExistente.getTipo()
            );
        }


        GridPane grid =
                new GridPane();

        grid.setHgap(12);
        grid.setVgap(12);

        grid.setPadding(
                new javafx.geometry.Insets(20)
        );


        grid.add(
                new Label("N° Sala:"),
                0,
                0
        );

        grid.add(
                txtNumero,
                1,
                0
        );


        grid.add(
                new Label("Capacidad:"),
                0,
                1
        );

        grid.add(
                txtCapacidad,
                1,
                1
        );


        grid.add(
                new Label("Tipo:"),
                0,
                2
        );

        grid.add(
                txtTipo,
                1,
                2
        );


        dialog.getDialogPane()
                .setContent(grid);


//=======================================================================================
        dialog.setResultConverter(
                dialogButton -> {

                    if (dialogButton != guardarButton) {
                        return null;
                    }
                    try {
                        Integer numero =
                                Integer.parseInt(
                                        txtNumero
                                                .getText()
                                                .trim()
                                );
                        Integer capacidad =
                                Integer.parseInt(
                                        txtCapacidad
                                                .getText()
                                                .trim()
                                );

                        String tipo =
                                txtTipo
                                        .getText()
                                        .trim();
                        Sala sala =
                                Sala.builder()
                                        .idSala(
                                                salaExistente == null
                                                        ? null
                                                        : salaExistente.getIdSala()
                                        )
                                        .numero(numero)
                                        .capacidad(capacidad)
                                        .tipo(tipo)
                                        .build();



                        Set<ConstraintViolation<Sala>>
                                errores =
                                validator.validate(sala);


                        if (!errores.isEmpty()) {

                            String mensaje =
                                    errores.stream()
                                            .sorted(
                                                    Comparator.comparing(
                                                            e ->
                                                                    e.getPropertyPath()
                                                                            .toString()
                                                    )
                                            )
                                            .map(
                                                    ConstraintViolation::getMessage
                                            )
                                            .distinct()
                                            .reduce(
                                                    "",
                                                    (a, b) ->
                                                            a.isEmpty()
                                                                    ? b
                                                                    : a + "\n" + b
                                            );


                            mostrarAlerta(
                                    "Datos incorrectos",
                                    mensaje
                            );

                            return null;
                        }


                        // Si todo está correcto
                        // devolver la Sala
                        return sala;


                    } catch (NumberFormatException e) {

                        mostrarAlerta(
                                "Datos incorrectos",
                                "Número de sala y capacidad deben ser valores numéricos."
                        );

                        return null;
                    }
                }
        );


        dialog.showAndWait()
                .ifPresent(sala -> {

                    Stage ventana =
                            (Stage) miContenedor
                                    .getScene()
                                    .getWindow();




                    if (salaExistente == null) {

                        ss.save(sala);

                        Toast.showToast(
                                ventana,
                                "Sala registrada correctamente",
                                2000,
                                ventana.getWidth() / 1.5,
                                ventana.getHeight() / 2
                        );


                    }

                    else {

                        ss.update(
                                salaExistente.getIdSala(),
                                sala
                        );

                        Toast.showToast(
                                ventana,
                                "Sala actualizada correctamente",
                                2000,
                                ventana.getWidth() / 1.5,
                                ventana.getHeight() / 2
                        );
                    }


                    // Actualizar tabla
                    listar();
                });
    }


    private void mostrarAlerta(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(titulo);

        alert.setHeaderText(null);

        alert.setContentText(mensaje);

        alert.showAndWait();
    }




    public void setStage(Stage stage) {

        this.stage = stage;
    }
}
