package com.example.instagram.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.adapter.AdapterGrid;
import com.example.instagram.helper.ConfiguracaoFireBase;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Postagem;
import com.example.instagram.model.PostagemCurtida;
import com.example.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private PostagemCurtida postagemCurtida;
    private Postagem postagemSelecionada;

    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private TextView textPuclicacoes, textSeguidores, textSeguindo;
    private GridView gridViewPerfil;
    private AdapterGrid adapterGrid;
    private String postagensCurtidas;

    private DatabaseReference firebaseref;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference postagemCurtidaRef;
    private DatabaseReference feedRef;
    private DatabaseReference postagemUsuarioRef;
    private DatabaseReference postagemSelecionadaRef;

    private ValueEventListener valueEventListenerPerfilAmigo;

    private String idUsuarioLogado;
    private List<Postagem> postagens;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        //configuracoes iniciais
        firebaseref = ConfiguracaoFireBase.getFirebase();
        usuariosRef = firebaseref.child("usuarios");
        seguidoresRef = firebaseref.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
//alteração
        postagemCurtidaRef = firebaseref.child("postagens-curtidas");
        feedRef = firebaseref.child("feed");
        postagemSelecionadaRef = firebaseref.child("postagens");


        //inicializar componentes
        inicializarComponentes();

        //configuração toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Perfil");
        setSupportActionBar(toolbar);

        //botão voltar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //recuperar usuario selecionado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            usuarioSelecionado = (Usuario) bundle.getSerializable("usuarioSelecionado");


            //configura referencia postagens usuario
            postagemUsuarioRef = ConfiguracaoFireBase.getFirebase()
                    .child("postagens")
                    .child(usuarioSelecionado.getId());


            //configura nome do usuario no toolbar
            getSupportActionBar().setTitle(usuarioSelecionado.getNome());

            //recuperar foto do usuário
            String caminhoFoto = usuarioSelecionado.getCaminhoFoto();
            if (caminhoFoto != null) {
                Uri url = Uri.parse(caminhoFoto);
                Glide.with(PerfilAmigoActivity.this)
                        .load(url)
                        .into(imagePerfil);

            }
        }

        //inicializar image loader
        inicializarImageLoader();

        //carregar as fotos das postagens de um usuario
        carregarFotosPostagem();

        //abre a foto clicada
        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Postagem postagem = postagens.get(position);
//                PostagemCurtida postagemCurtida = postagemCurtidaRef.

                Intent i = new Intent(getApplicationContext(), VisualizarPostagemActivity.class);

                i.putExtra("postagem", postagem);
//                i.putExtra("postagens-curtidas", postagensCurtidas);
                i.putExtra("usuario", usuarioSelecionado);

                startActivity(i);

            }
        });

    }

    // INSTANCIA A UNIVERSAL IMAGE LOADER
    public void inicializarImageLoader() {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);

    }


    public void carregarFotosPostagem() {

        //recupera as fotos postadas pelo usuario
        postagens = new ArrayList<>();
        postagemUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //configurar o tamanho do grid
                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                int tamanhoImagem = tamanhoGrid / 3;
                gridViewPerfil.setColumnWidth(tamanhoImagem);

                List<String> urlFotos = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Postagem postagem = ds.getValue(Postagem.class);
                    postagens.add(postagem);
                    urlFotos.add(postagem.getCaminhoFoto());
                }

                int qtdPostagem = urlFotos.size();
                textPuclicacoes.setText(String.valueOf(qtdPostagem));

                //configurar adapter para o grid
                adapterGrid = new AdapterGrid(getApplicationContext(), R.layout.grid_postagem, urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void recuperaPostagemCurtida(){

    }

    private void recuperarDadosUsuarioLogado() {

        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //recupera dados do usuario logado
                usuarioLogado = dataSnapshot.getValue(Usuario.class);

                //verifica se usuario ja esta seguindo amigo selecionado
                verificaSegueUsuarioAmigo();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void verificaSegueUsuarioAmigo() {

        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getId())
                .child(idUsuarioLogado);


        //pega o dados uma vez não é um listener que fica diferao
        seguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //já segue
                    habilitarBotaoSeguir(true);
                } else {
                    //nao segue
                    habilitarBotaoSeguir(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void habilitarBotaoSeguir(boolean segueUsuario) {
        if (segueUsuario) {
            buttonAcaoPerfil.setText("Seguindo");
        } else {
            buttonAcaoPerfil.setText("Seguir");

            //adiciona o evento para seguir usuario
            buttonAcaoPerfil.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    //salvar seguidor
                    salvarSeguidor(usuarioLogado, usuarioSelecionado);
                }
            });
        }
    }

    private void salvarSeguidor(Usuario uLogado, Usuario uAmigo) {

//        seguidores
//            id_usuario_selecionado (id amigo)
//                id_usuario_logado (id usuario logado)
//                    dados_logado

        HashMap<String, Object> dadosUsuarioLogado = new HashMap<>();
        dadosUsuarioLogado.put("nome", uLogado.getNome());
        dadosUsuarioLogado.put("caminhoFoto", uLogado.getCaminhoFoto());
        DatabaseReference seguidorRef = seguidoresRef
                .child(uAmigo.getId())
                .child(uLogado.getId());
        seguidorRef.setValue(dadosUsuarioLogado);

        //Alterar 'botao acao' para seguindo
        buttonAcaoPerfil.setText("Seguindo");
        buttonAcaoPerfil.setOnClickListener(null); //desligo o clique do botão ação

        //incrementar seguindo do usuario logado
        int seguindo = uLogado.getSeguindo() + 1;
        HashMap<String, Object> dadosSeguindo = new HashMap<>();
        dadosSeguindo.put("seguindo", seguindo);
        DatabaseReference usuarioSeguindo = usuariosRef
                .child(uLogado.getId());
        usuarioSeguindo.updateChildren(dadosSeguindo);

        //incrementar seguidores do amigo
        int seguidores = uAmigo.getSeguidores() + 1;
        HashMap<String, Object> dadosSeguidores = new HashMap<>();
        dadosSeguidores.put("seguidores", seguidores);
        DatabaseReference usuarioSeguidores = usuariosRef
                .child(uAmigo.getId());
        usuarioSeguidores.updateChildren(dadosSeguidores);
    }


    //proximo ciclo de vida da activity
    @Override
    protected void onStart() {
        super.onStart();

        //recupera dados do amigo selecionado
        recuperarDadosPerfilAmigo();

        //recupera dados usuario logado
        recuperarDadosUsuarioLogado();

    }

    //aqui matamos o listener após sair da activity
    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener(valueEventListenerPerfilAmigo);
    }

    public void recuperarDadosPerfilAmigo() {

        usuarioAmigoRef = usuariosRef.child(usuarioSelecionado.getId());
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);

                String postagens = String.valueOf(usuario.getPostagens());
                String seguindo = String.valueOf(usuario.getSeguindo());
                String seguidores = String.valueOf(usuario.getSeguidores());

                //configura valores recuperados
                textPuclicacoes.setText(postagens);
                textSeguidores.setText(seguidores);
                textSeguindo.setText(seguindo);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void inicializarComponentes() {
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        buttonAcaoPerfil.setText("Carregando");
        imagePerfil = findViewById(R.id.imagePerfil);
        textPuclicacoes = findViewById(R.id.textPublicacoes);
        textSeguidores = findViewById(R.id.textSeguidores);
        textSeguindo = findViewById(R.id.textSeguindo);
        gridViewPerfil = findViewById(R.id.gridViewPerfil);
    }

    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }


}