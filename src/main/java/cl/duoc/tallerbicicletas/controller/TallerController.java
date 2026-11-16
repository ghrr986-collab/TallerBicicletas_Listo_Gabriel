package cl.duoc.tallerbicicletas.controller;

import cl.duoc.tallerbicicletas.dao.BicicletaDAO;
import cl.duoc.tallerbicicletas.model.Bicicleta;
import cl.duoc.tallerbicicletas.model.BicicletaElectrica;
import cl.duoc.tallerbicicletas.model.BicicletaMontanya;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TallerController {
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtAanio;
    @FXML private TextField txtPeso;


    @FXML private TitledPane panelElectrico;
    @FXML private TextField txtAutonomia;
    @FXML private CheckBox chckBateriaCertificada;
    @FXML private CheckBox chkGarantiaExtendida;

    @FXML private TitledPane panelMontanya;
    @FXML private TextField txtSuspensiones;


    @FXML private TableView <Bicicleta> tablaBicicletas;
    @FXML private TableColumn <Bicicleta, String> colTipo;
    @FXML private TableColumn <Bicicleta, String> colCodigo;
    @FXML private TableColumn <Bicicleta, String> colAanio;
    @FXML private TableColumn <Bicicleta, String> colPeso;
    @FXML private TableColumn <Bicicleta, String> colCosto;

    @FXML private Label lblMensaje;


    private BicicletaDAO dao;
    private ObservableList<Bicicleta> listaBicicletas;
    private DecimalFormat formatoPesos;
    private DecimalFormat formatoPeso;

    @FXML
    public void initialize() {
       dao = new BicicletaDAO();
       listaBicicletas = FXCollections.observableArrayList();

       DecimalFormatSymbols simbolosPesos = new DecimalFormatSymbols();
       simbolosPesos.setDecimalSeparator('.');
       formatoPesos = new DecimalFormat("$#, ##0", simbolosPesos);

       DecimalFormatSymbols simbolosPeso = new DecimalFormatSymbols(Locale.US);
       formatoPeso = new DecimalFormat("0.0", simbolosPeso);

       cmbTipo.getItems().addAll("Bicicleta Electrica", "Bicicleta Montaña");

       panelElectrico.setDisable(true);
        panelMontanya.setDisable(true);

        configurarTabla();
        cargarDatos();
    }

    private void configurarTabla() {

        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo()));
        colCodigo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCodigo()));
        colAanio.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getAnioFabricacion())));
        colPeso.setCellValueFactory(cellData -> new SimpleStringProperty(formatoPeso.format(cellData.getValue().getPeso())));
        colCosto.setCellValueFactory(cellData -> new SimpleStringProperty(formatoPesos.format(cellData.getValue().calcularCostoMantencion())));

        tablaBicicletas.setItems(listaBicicletas);
    }

    private void cargarDatos() {
        try {
            listaBicicletas.setAll(dao.cargarTodas());
        }catch (IOException e){
            mostrarError("Error al leer archivos Json de almacenimiento");
        }
    }

    @FXML
    void cambiarTipo(ActionEvent event) {
        String tipo = cmbTipo.getValue();
        if("Bicicleta Electrica".equals(tipo)) {
            panelElectrico.setDisable(false);
            panelMontanya.setDisable(true);
            txtSuspensiones.clear();
        } else if ("Bicicleta Montaña".equals(tipo)) {
            panelMontanya.setDisable(false);
            panelElectrico.setDisable(true);
            txtAutonomia.clear();
            chckBateriaCertificada.setDisable(false);
            chkGarantiaExtendida.setDisable(true);
        }

    }

    @FXML
    void RegistrarBicicleta(ActionEvent event) {
        String tipo = cmbTipo.getValue();
        if(tipo == null || tipo.isBlank()){
            mostrarError("Tipo: debe seleccionar un tipo de bicicleta");
            return;
        }
        String codigo = txtCodigo.getText();
        if(codigo == null || codigo.trim().isEmpty()){
            mostrarError("Codigo no puede estar vacio");
            return;
        }
        codigo = codigo.trim();

        int anio;
        try{
            anio = Integer.parseInt(txtAanio.getText().trim());
        }catch (NumberFormatException e){
        mostrarError("Año de fabricacion ");
        return;
        }

        double peso;
        try{
            peso = Double.parseDouble(txtPeso.getText().trim().replace(',','.'));
        }catch (NumberFormatException e){
            mostrarError("Peso: peso tiene que ser un numero valido ");
            return;
        }
        try {
           Bicicleta nuevaBicicleta;

           if ("Bicicleta Electrica".equals(tipo)) {
               int autonomia;
               try{
                   autonomia = Integer.parseInt(txtAutonomia.getText().trim());
               }catch (NumberFormatException e){
                   mostrarError("Autonomia: debe ser un numero valido");
                   return;
               }

               boolean batCertificada = chckBateriaCertificada.isSelected();
               boolean garantia = chkGarantiaExtendida.isSelected();



               BicicletaElectrica electrica = new BicicletaElectrica(codigo, anio, peso, autonomia, batCertificada);
               if(garantia){
                   electrica.activarGarantiaExtendida();
               }
                nuevaBicicleta = electrica;
           }else{
               int suspension;
               try{
                   suspension = Integer.parseInt(txtSuspensiones.getText().trim());
               }catch (NumberFormatException e){
                   mostrarError("Suspensiones: debe ser un numero valido");
                   return;
               }

               nuevaBicicleta = new BicicletaMontanya(codigo, anio, peso, suspension);

           }
                listaBicicletas.add(nuevaBicicleta);
                dao.guardarTodas(listaBicicletas);
                limpiarFormulario();
                mostrarExito("Bicicleta" + codigo + "("+ tipo+") registrada");


        }catch (IllegalArgumentException e){
            mostrarError(e.getMessage());
        }catch (IOException e){
            listaBicicletas.remove(listaBicicletas.size()-1);
            mostrarError("Erro al guardar los datos en Json");
        }
    }
    @FXML
    void eliminarBicicleta(ActionEvent event) {
        Bicicleta seleccionada = tablaBicicletas.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mostrarError("Debes seleccionar una bicicleta para eliminarla");
            return;
        }

        listaBicicletas.remove(seleccionada);
        try{
            dao.guardarTodas(listaBicicletas);
            mostrarExito("Bicicleta" + seleccionada.getCodigo() + "eliminada");
        }catch (IOException e){
            listaBicicletas.add(seleccionada);
            mostrarError("Error al eliminar");
        }
    }

    private void limpiarFormulario(){
        cmbTipo.setValue(null);
        txtCodigo.clear();
        txtPeso.clear();
        txtAanio.clear();
        txtAutonomia.clear();
        chckBateriaCertificada.setSelected(false);
        chkGarantiaExtendida.setSelected(false);
        txtSuspensiones.clear();
        panelElectrico.setDisable(true);
        panelMontanya.setDisable(true);
    }

    private void mostrarExito(String mensaje){
        lblMensaje.setTextFill(Color.GREEN);
        lblMensaje.setText(mensaje);
    }

    private void mostrarError(String mensaje){
        lblMensaje.setTextFill(Color.RED);
        lblMensaje.setText(mensaje);
    }



}
