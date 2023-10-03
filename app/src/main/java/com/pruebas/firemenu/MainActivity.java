package com.pruebas.firemenu;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.ui.AppBarConfiguration;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pruebas.firemenu.databinding.ActivityMainBinding;
import com.pruebas.firemenu.models.modelo;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private List<modelo> productos_agregados = new ArrayList<modelo>();
    ArrayAdapter<modelo> arrayAdapterProductos;

    EditText CODE, nm_prod, desc_prod, precio_prod;
    ListView listView_productos;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    modelo  productosseleccionados;


    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        CODE = findViewById(R.id.edit_codigo);
        nm_prod = findViewById(R.id.edit_nombre);
        desc_prod = findViewById(R.id.edit_descrip);
        precio_prod = findViewById(R.id.edit_precio);


        listView_productos = findViewById(R.id.list_productos);

        iniciarfirebase();

        listarProductos();

        listView_productos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                productosseleccionados = (modelo) adapterView.getItemAtPosition(position);
                CODE.setText(productosseleccionados.getCODIGO());
                nm_prod.setText(productosseleccionados.getNombre());
                desc_prod.setText(productosseleccionados.getDescrip());
                precio_prod.setText(productosseleccionados.getPrecio());
            }
        });
    }

    private void listarProductos() {
        databaseReference.child("Producto").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productos_agregados.clear();

                for (DataSnapshot productosEnFirebase : snapshot.getChildren()){
                    modelo obtenerProductos = productosEnFirebase.getValue(modelo.class);
                    productos_agregados.add(obtenerProductos);

                    arrayAdapterProductos = new ArrayAdapter<modelo>(MainActivity.this, android.R.layout.simple_list_item_1,productos_agregados);
                    listView_productos.setAdapter(arrayAdapterProductos);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void iniciarfirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        //firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        String prod_code = CODE.getText().toString();
        String prod_nm = nm_prod.getText().toString();
        String prod_desc = desc_prod.getText().toString();
        String prod_prec = precio_prod.getText().toString();


        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.icon_add) {
            if (prod_code.equals("")||prod_nm.equals("")||prod_desc.equals("")||prod_prec.equals("")){
                validar_campos();
            }else {
                modelo add_prod = new modelo();
                add_prod.setUid(UUID.randomUUID().toString());
                add_prod.setCODIGO(prod_code);
                add_prod.setNombre(prod_nm);
                add_prod.setDescrip(prod_desc);
                add_prod.setPrecio(prod_prec);

                databaseReference.child("Producto").child(add_prod.getUid()).setValue(add_prod);

                Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show();
                limpiar_campos();
                return true;
            }
        } else if (id==R.id.icon_actualizar) {
            modelo editarproducto=new modelo();
            editarproducto.setUid(productosseleccionados.getUid());
            editarproducto.setCODIGO(nm_prod.getText().toString());
            editarproducto.setDescrip(desc_prod.getText().toString());
            editarproducto.setPrecio(precio_prod.getText().toString());
            
            databaseReference.child("Producto").child(editarproducto.getUid()).setValue(editarproducto);
            limpiar_campos();
            Toast.makeText(this, "cambios guardados", Toast.LENGTH_SHORT).show();
            return true;
            
        } else if (id == R.id.icon_clear) {
            limpiar_campos();
            Toast.makeText(this, "Limpiar", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.icon_delete) {
            //String prod=productosseleccionados.getNombre().toString();
            AlertDialog.Builder mensajeeliminar = new AlertDialog.Builder(MainActivity.this);
            mensajeeliminar.setTitle("elimnar producto");
            mensajeeliminar.setMessage("¿Desea eliminar ?"+productosseleccionados.getNombre().toString());
            mensajeeliminar.setIcon(android.R.drawable.ic_dialog_alert);
            mensajeeliminar.setCancelable(false);
            mensajeeliminar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    modelo eliminarproducto= new modelo();
                    eliminarproducto.setUid(productosseleccionados.getUid());
                    databaseReference.child("Producto").child(eliminarproducto.getUid()).removeValue();
                    limpiar_campos();
                    Toast.makeText(MainActivity.this, "Eliminar", Toast.LENGTH_SHORT).show();
                }
            });
            mensajeeliminar.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            mensajeeliminar.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void limpiar_campos() {
        CODE.setText("");
        nm_prod.setText("");
        desc_prod.setText("");
        precio_prod.setText("");
    }

    private void validar_campos() {
        String val_code_prod = CODE.getText().toString();
        String val_nm_prod = nm_prod.getText().toString();
        String val_desc_prod = desc_prod.getText().toString();
        String val_prec_prod = precio_prod.getText().toString();

        if (val_code_prod.equals("")){
            CODE.setError("Obligatorio");
        } else if (val_nm_prod.equals("")) {
            nm_prod.setError("Obligatorio");
        } else if (val_desc_prod.equals("")) {
            desc_prod.setError("Obligatorio");
        } else if (val_prec_prod.equals("")) {
            precio_prod.setError("Obligatorio");
        }

    }

}