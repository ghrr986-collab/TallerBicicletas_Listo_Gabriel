package cl.duoc.tallerbicicletas.dao;
import cl.duoc.tallerbicicletas.model.Bicicleta;
import cl.duoc.tallerbicicletas.model.BicicletaElectrica;
import cl.duoc.tallerbicicletas.model.BicicletaMontanya;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BicicletaDAO {
    private static final String ARCHIVO_ELECTRICAS = "bicicletas_electricas.json";
    private static final String ARCHIVO_MONTANYAS = "bicicletas_montanyas.json";
    private Gson gson;

    public BicicletaDAO() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public List<Bicicleta> cargarTodas()  throws IOException {
        List<Bicicleta> todas  = new ArrayList<>();



        try (FileReader reader = new FileReader(ARCHIVO_ELECTRICAS)) {
            Type tipolista = new TypeToken<List<BicicletaElectrica>>() {}.getType();
            List<BicicletaElectrica> electricas = gson.fromJson(reader, tipolista);
            if(electricas != null){
                todas.addAll(electricas);
            }

        }catch (IOException e){
            System.out.println("Error al cargar");

        }

        try (FileReader reader = new FileReader(ARCHIVO_MONTANYAS)) {
            Type tipoListaMontanya = new TypeToken<List<BicicletaMontanya>>() {}.getType();
            List<BicicletaMontanya> montanyas = gson.fromJson(reader, tipoListaMontanya);
            if(montanyas != null){
                todas.addAll(montanyas);
            }
        }catch (IOException e){
            System.out.println("Error al cargar");
        }

        return todas;


    }

    public void guardarTodas(List<Bicicleta> todas) throws IOException {
        List<BicicletaElectrica> electricas = new ArrayList();
        List<BicicletaMontanya> montanyas = new ArrayList();

        for (Bicicleta b : todas)   {
            if(b instanceof BicicletaElectrica){
                electricas.add((BicicletaElectrica) b);
            }else if(b instanceof BicicletaMontanya){
                montanyas.add((BicicletaMontanya) b);
            }
        }


        try(FileWriter writer = new FileWriter(ARCHIVO_ELECTRICAS)) {
            gson.toJson(electricas, writer);

        }

        try(FileWriter writer = new FileWriter(ARCHIVO_MONTANYAS)) {
            gson.toJson(montanyas, writer);
        }
    }

}
