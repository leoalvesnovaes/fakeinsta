package com.example.instagram.activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.model.Postagem;
import com.example.instagram.model.PostagemCurtida;
import com.example.instagram.model.Usuario;
import com.google.firebase.database.DatabaseReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class VisualizarPostagemActivity extends AppCompatActivity {

    private TextView textQtdCurtidasPostagem, textDescricaoPostagem, textPerfilPostagem;
    private ImageView imagePostagemSelecionada, visualizarComentario;
    private CircleImageView imagePerfilPostagem;

    private DatabaseReference postagemCurtida;
    private PostagemCurtida postagemCurtidaQTD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_postagem);

        //inicializar os componentes
        inicializarComponentes();

        //configuração toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Visualizar Postagem");
        setSupportActionBar(toolbar);

        //botão voltar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //recuparar os dados da activity perfillamigo
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Postagem postagem = (Postagem) bundle.getSerializable("postagem");
            Usuario usuario = (Usuario) bundle.getSerializable("usuario");
            PostagemCurtida postagemCurtida = (PostagemCurtida) bundle.getSerializable("postagemCurtidaQTD");

            //exibe dados de usuario
            Uri uri = Uri.parse(usuario.getCaminhoFoto());
            Glide.with(VisualizarPostagemActivity.this)
                    .load(uri)
                    .into(imagePerfilPostagem);
            textPerfilPostagem.setText(usuario.getNome());

            //exibe dados da postagem
            Uri uriPostagem = Uri.parse(postagem.getCaminhoFoto());
            Glide.with(VisualizarPostagemActivity.this)
                    .load(uriPostagem)
                    .into(imagePostagemSelecionada);
            textDescricaoPostagem.setText(postagem.getDescricao());
            textQtdCurtidasPostagem.setText(postagemCurtida.getQtdCurtidas());


        }


    }


    public void inicializarComponentes() {
        textQtdCurtidasPostagem = findViewById(R.id.textQtdCurtidasPostagem);
        textDescricaoPostagem = findViewById(R.id.textDescricaoPostagem);
        textPerfilPostagem = findViewById(R.id.textPerfilPostagem);
        imagePostagemSelecionada = findViewById(R.id.imagePostagemSelecionada);
        imagePerfilPostagem = findViewById(R.id.imagePerfilPostagem);
        visualizarComentario = findViewById(R.id.imageComentarioFeed);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}