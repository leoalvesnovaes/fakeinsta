package com.example.instagram.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.adapter.AdapterComentario;
import com.example.instagram.helper.ConfiguracaoFireBase;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Comentario;
import com.example.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ComentariosActivity extends AppCompatActivity {

    private EditText editComentario;
    private String idPostagem;
    private Usuario usuario;
    private RecyclerView recyclerComentarios;
    private AdapterComentario adapterComentario;
    private List<Comentario> listaComentarios = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private DatabaseReference comentariosRef;
    private ValueEventListener valueEventListenerComentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        //inicializa componentes
        editComentario = findViewById(R.id.editComentario);
        recyclerComentarios = findViewById(R.id.recyclerComentarios);

        //configuracao inicial
        usuario = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFireBase.getFirebase();

        //configuração toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Comentários");
        setSupportActionBar(toolbar);
        //botão voltar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //configura recycler view
        adapterComentario = new AdapterComentario(listaComentarios, getApplicationContext());
        recyclerComentarios.setHasFixedSize(true);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter(adapterComentario);

        //recupera o id da postagem
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            idPostagem = bundle.getString("idPostagem");
        }


    }

    private void recuperarCometnario() {

        comentariosRef = firebaseRef.child("comentarios")
                .child(idPostagem);
        valueEventListenerComentarios = comentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaComentarios.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    listaComentarios.add(ds.getValue(Comentario.class));
                }
                adapterComentario.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarCometnario();
    }

    @Override
    protected void onStop() {
        super.onStop();
        comentariosRef.removeEventListener( valueEventListenerComentarios);
    }

    public void salvarComentario(View view) {

        String textoComentario = editComentario.getText().toString();
        if (textoComentario != null && !textoComentario.equals("")) {

            Comentario comentario = new Comentario();
            comentario.setIdPostagem(idPostagem);
            comentario.setIdUsuario(usuario.getId());
            comentario.setNomeUsuario(usuario.getNome());
            comentario.setCaminhoFoto(usuario.getCaminhoFoto());
            comentario.setComentario(textoComentario);
            if (comentario.salvar()) {
                Toast.makeText(this,
                        "Comentário feito!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Insira um Comentário!", Toast.LENGTH_SHORT).show();
        }
        //limpa comentário digitado
        editComentario.setText("");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}