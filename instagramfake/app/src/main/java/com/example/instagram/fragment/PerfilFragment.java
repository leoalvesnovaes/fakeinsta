package com.example.instagram.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.activity.EditarPerfilActivity;
import com.example.instagram.activity.VisualizarPostagemActivity;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class PerfilFragment extends Fragment {

    private ProgressBar progressBar;
    private CircleImageView imagePerfil;
    public GridView gridViewPerfil;
    private TextView textPuclicacoes, textSeguidores, textSeguindo;
    private Button buttonAcaoPerfil;
    private Usuario usuarioLogado;
    private AdapterGrid adapterGrid;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private ValueEventListener valueEventListenerPerfil;
    private ValueEventListener valueEventListenerCurtida;
    private DatabaseReference firebaseref;
    private DatabaseReference postagemUsuarioLogado;
    private List<Postagem> postagens;
    private List<PostagemCurtida> postagemCurtidaARRAY;
    private Usuario usuarioSelecionado;
    private DatabaseReference postagemUsuarioRef;
    private PostagemCurtida postagemCurtidaQTD;
    private DatabaseReference postagemCurtidaRef;
    private DatabaseReference postagemCurtidaREF;
    private int qtdCurtida;
    private Postagem postagem;

    public PerfilFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        //configuração inicial
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseref = ConfiguracaoFireBase.getFirebase();
        usuariosRef = firebaseref.child("usuarios");


        //configurações componentes
        inicializarComponentes(view);

        //recuperar foto do usuário
        String caminhoFoto = usuarioLogado.getCaminhoFoto();
        if (caminhoFoto != null) {
            Uri url = Uri.parse(caminhoFoto);
            Glide.with(getActivity())
                    .load(url)
                    .into(imagePerfil);
        }

        //abrir edição de Perfil
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditarPerfilActivity.class);
                startActivity(i);
            }
        });

        //configura referencia postagens usuario
        postagemUsuarioLogado = ConfiguracaoFireBase.getFirebase()
                .child("postagens")
                .child(usuarioLogado.getId());


        //inicializar image loader
        inicializarImageLoader();

        //carregar as fotos das postagens de um usuario
        carregarFotosPostagem();

        //abre a foto clicada
        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                postagem = postagens.get(position);

                postagemCurtidaRef = ConfiguracaoFireBase.getFirebase()
                        .child("postagens-curtidas")
                        .child(postagem.getId());

               recuperarPostagemCurtida();

                Intent i = new Intent(getActivity(), VisualizarPostagemActivity.class);

                i.putExtra("postagem", postagem);
                i.putExtra("usuario", usuarioLogado);
                i.putExtra("postagemCurtidaQTD", postagemCurtidaQTD);

                startActivity(i);

            }
        });

        return view;
    }

    // INSTANCIA A UNIVERSAL IMAGE LOADER
    public void inicializarImageLoader() {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getActivity())
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
        postagemUsuarioLogado.addListenerForSingleValueEvent(new ValueEventListener() {
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
                adapterGrid = new AdapterGrid(getActivity(), R.layout.grid_postagem, urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void inicializarComponentes(View view) {

        gridViewPerfil = view.findViewById(R.id.gridViewPerfil);
        progressBar = view.findViewById(R.id.progressBarPerfil);
        imagePerfil = view.findViewById(R.id.imagePerfil);
        textPuclicacoes = view.findViewById(R.id.textPublicacoes);
        textSeguidores = view.findViewById(R.id.textSeguidores);
        textSeguindo = view.findViewById(R.id.textSeguindo);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaoPerfil);
    }

    public void recuperarDadosUsuarioLogado() {

        usuarioLogadoRef = usuariosRef.child(usuarioLogado.getId());
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);

                // String postagens = String.valueOf(usuario.getPostagens());
                String seguindo = String.valueOf(usuario.getSeguindo());
                String seguidores = String.valueOf(usuario.getSeguidores());


                //configura valores recuperados
                //  textPuclicacoes.setText(postagens);
                textSeguidores.setText(seguidores);
                textSeguindo.setText(seguindo);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void recuperarPostagemCurtida() {

        valueEventListenerCurtida = postagemCurtidaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                     postagemCurtidaQTD = dataSnapshot.getValue(PostagemCurtida.class);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void recuperarFotoUsuario() {

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //recuperar foto do usuário
        String caminhoFoto = usuarioLogado.getCaminhoFoto();
        if (caminhoFoto != null) {
            Uri url = Uri.parse(caminhoFoto);
            Glide.with(getActivity())
                    .load(url)
                    .into(imagePerfil);

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        recuperarDadosUsuarioLogado();
        recuperarFotoUsuario();
    }

    @Override
    public void onStop() {
        super.onStop();

//        postagemCurtidaRef.removeEventListener(valueEventListenerCurtida);
        usuarioLogadoRef.removeEventListener(valueEventListenerPerfil);
    }

}